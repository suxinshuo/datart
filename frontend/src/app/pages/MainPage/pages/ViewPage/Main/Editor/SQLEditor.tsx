/**
 * Datart
 *
 * Copyright 2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { message } from 'antd';
import useI18NPrefix from 'app/hooks/useI18NPrefix';
import classnames from 'classnames';
import { CommonFormTypes } from 'globalConstants';
import debounce from 'lodash/debounce';
import { language } from 'monaco-editor/esm/vs/basic-languages/sql/sql';
import React,
  { memo,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';
import { useHotkeys } from 'react-hotkeys-hook';
import MonacoEditor, { monaco } from 'react-monaco-editor';
import { useDispatch, useSelector } from 'react-redux';
import styled from 'styled-components/macro';
import { FONT_SIZE_BASE } from 'styles/StyleConstants';
import { selectThemeKey } from 'styles/theme/slice/selectors';
import { RootState } from 'types';
import { getInsertedNodeIndex } from 'utils/utils';
import { CacheDialog } from '../../components/CacheDialog';
import { ViewStatus, ViewViewModelStages } from '../../constants';
import { EditorContext } from '../../EditorContext';
import { SaveFormContext } from '../../SaveFormContext';
import { useViewSlice } from '../../slice';
import {
  selectCurrentEditingViewAttr,
  selectCurrentEditingView,
  selectViews,
} from '../../slice/selectors';
import {
  getEditorProvideCompletionItems,
  runSql,
  saveView,
} from '../../slice/thunks';
import { deleteSqlFromCache, isNewView } from '../../utils';

// Text selected when "value" prop changes issue
// https://github.com/react-monaco-editor/react-monaco-editor/issues/325

export const SQLEditor = memo(() => {
  const { actions } = useViewSlice();
  const dispatch = useDispatch();
  const { editorInstance,
    editorCompletionItemProviderRef,
    setEditor,
    initActions,
  } = useContext(EditorContext);
  const { showSaveForm } = useContext(SaveFormContext);
  const id = useSelector<RootState>(state =>
    selectCurrentEditingViewAttr(state, { name: 'id' }),
  ) as string;
  const script = useSelector<RootState>(state =>
    selectCurrentEditingViewAttr(state, { name: 'script' }),
  ) as string;
  const stage = useSelector<RootState>(state =>
    selectCurrentEditingViewAttr(state, { name: 'stage' }),
  ) as ViewViewModelStages;
  const status = useSelector<RootState>(state =>
    selectCurrentEditingViewAttr(state, { name: 'status' }),
  ) as ViewStatus;
  const theme = useSelector(selectThemeKey);
  const viewsData = useSelector(selectViews);
  const cacheConflict = useSelector<RootState>(state =>
    selectCurrentEditingViewAttr(state, { name: 'cacheConflict' }),
  ) as boolean;
  const cacheExpired = useSelector<RootState>(state =>
    selectCurrentEditingViewAttr(state, { name: 'cacheExpired' }),
  ) as boolean;
  const cacheData = useSelector<RootState>(state =>
    selectCurrentEditingViewAttr(state, { name: 'cacheData' }),
  ) as {
    script: string;
    name: string;
    sourceId: string;
    updatedAt: number;
    viewId: string;
  } | undefined;
  const currentView = useSelector(selectCurrentEditingView) as any;
  const t = useI18NPrefix('view.editor');
  const tView = useI18NPrefix('view');

  // Cache Dialog state
  const [cacheDialogVisible, setCacheDialogVisible] = useState(false);

  // 智能执行功能状态管理
  const [sqlFragments, setSqlFragments] = useState<string[]>([]);
  const [selectedFragmentIndex, setSelectedFragmentIndex] = useState(0);

  // 定义SQL片段类型，包含内容和位置信息
  interface SqlFragment {
    sql: string;
    start: number;
    end: number;
  }

  // 智能执行功能：使用行级分析识别SQL片段并记录位置
  const getSqlFragments = useCallback(
    (editor: monaco.editor.IStandaloneCodeEditor): SqlFragment[] => {
      const model = editor.getModel();
      if (!model) return [];

      const SQL_START_KEYWORDS = [
        'select',
        'insert',
        'update',
        'delete',
        'show',
        'describe',
        'desc',
        'explain',
        'with',
        'create',
        'alter',
        'drop',
        'truncate',
      ];

      // 判断是否为SQL起始行
      const isSqlStart = (line: string): boolean => {
        const trimmed = line.trim().toLowerCase();
        if (!trimmed) return false;
        if (trimmed.startsWith('--') || trimmed.startsWith('#')) return false;
        return SQL_START_KEYWORDS.some(
          kw => trimmed.startsWith(kw + ' ') || trimmed === kw,
        );
      };

      const fullSql = model.getValue();
      const linesCount = model.getLineCount();
      let bufferLines: string[] = [];
      let bufferStartOffset: number = 0;
      let parenDepth = 0;
      let cteMode = false;
      const fragments: SqlFragment[] = [];

      for (let lineNum = 1; lineNum <= linesCount; lineNum++) {
        const lineContent = model.getLineContent(lineNum);
        const trimmed = lineContent.trim();
        const lowerTrimmed = trimmed.toLowerCase();

        // 计算当前行在完整SQL中的起始位置
        const lineStartOffset = model.getOffsetAt({
          lineNumber: lineNum,
          column: 1,
        });
        const lineEndOffset = model.getOffsetAt({
          lineNumber: lineNum,
          column: lineContent.length + 1,
        });

        // 如果缓冲区为空，记录当前行作为缓冲区的起始位置
        if (bufferLines.length === 0) {
          bufferStartOffset = lineStartOffset;
        }

        // 检查当前行是否为新SQL片段的开始
        const isNewStatementStart = isSqlStart(lineContent);

        // 特殊处理WITH语句：如果WITH语句不是当前buffer的一部分，应该作为新片段的开始
        const isWithStatement = lowerTrimmed.startsWith('with');

        // 检查缓冲区中是否已包含WITH关键字
        const bufferContainsWith = bufferLines.some(line =>
          line.toLowerCase().trim().startsWith('with'),
        );

        // 计算当前行的括号变化
        let currentLineParenChange = 0;
        for (let i = 0; i < lineContent.length; i++) {
          const ch = lineContent[i];
          if (ch === '(') currentLineParenChange++;
          else if (ch === ')') currentLineParenChange--;
        }

        // 更新括号深度（在处理拆分逻辑之前）
        const updatedParenDepth = parenDepth + currentLineParenChange;
        const finalParenDepth = Math.max(0, updatedParenDepth); // 确保不会为负数

        // 判断是否需要拆分：遇到SQL起始关键字且括号深度为0（基于前序行的括号深度）
        // 并且缓冲区中不包含WITH关键字，或者当前行是新的WITH语句
        if (
          isNewStatementStart &&
          bufferLines.length > 0 &&
          parenDepth === 0 &&
          !bufferContainsWith &&
          (!cteMode || isWithStatement)
        ) {
          // 保存当前片段
          const fragmentSql = bufferLines.join('\n').trim();
          const fragmentStart = bufferStartOffset;
          // 计算片段结束位置：当前行的起始位置
          const fragmentEnd = lineStartOffset;

          if (fragmentSql) {
            fragments.push({
              sql: fragmentSql,
              start: fragmentStart,
              end: fragmentEnd,
            });
          }
          // 重置缓冲区和状态
          bufferLines = [lineContent];
          bufferStartOffset = lineStartOffset;
          cteMode = isWithStatement;
        }
        // 分号结束SQL片段（基于前序行的括号深度）
        else if (trimmed.endsWith(';') && parenDepth === 0) {
          // 保存当前片段
          bufferLines.push(lineContent);
          const fragmentSql = bufferLines.join('\n').trim();
          const fragmentStart = bufferStartOffset;
          // 计算片段结束位置：当前行的结束位置
          const fragmentEnd = lineEndOffset;

          if (fragmentSql) {
            fragments.push({
              sql: fragmentSql,
              start: fragmentStart,
              end: fragmentEnd,
            });
          }
          // 重置缓冲区和状态
          bufferLines = [];
          bufferStartOffset = 0;
          cteMode = false;
        } else {
          // 添加当前行到缓冲区
          bufferLines.push(lineContent);

          // WITH + CTE 处理
          if (isWithStatement && bufferLines.length === 1) {
            cteMode = true;
          }
        }

        // 更新括号深度
        parenDepth = finalParenDepth;
      }

      // 处理最后一个片段
      if (bufferLines.length > 0) {
        const fragmentSql = bufferLines.join('\n').trim();
        if (fragmentSql) {
          const fragmentStart = bufferStartOffset;
          // 最后一个片段的结束位置是完整SQL的长度
          const fragmentEnd = fullSql.length;
          fragments.push({
            sql: fragmentSql,
            start: fragmentStart,
            end: fragmentEnd,
          });
        }
      }

      return fragments;
    },
    [],
  );

  // 智能执行功能：处理确认执行逻辑
  const handleRunCurrentFragment = useCallback(
    (fragmentIndex?: number, fragments?: SqlFragment[]) => {
      // 使用传入的fragmentIndex或当前状态值
      const currentIndex =
        fragmentIndex !== undefined ? fragmentIndex : selectedFragmentIndex;

      let selectedFragment: string;
      let fragmentStart: number;
      let fragmentEnd: number;

      if (fragments && fragments[currentIndex]) {
        // 如果传入了包含位置信息的片段数组，直接使用预存的位置
        const fragmentObj = fragments[currentIndex];
        selectedFragment = fragmentObj.sql;
        fragmentStart = fragmentObj.start;
        fragmentEnd = fragmentObj.end;
      } else {
        // 兼容逻辑，防止没有起止位置，但应该不会执行到这里
        selectedFragment = sqlFragments[currentIndex];
        const model = editorInstance?.getModel();
        if (!model) return;
        const fullSql = model.getValue();
        fragmentStart = fullSql.indexOf(selectedFragment);
        fragmentEnd = fragmentStart + selectedFragment.length;
      }

      const model = editorInstance?.getModel();
      if (model) {
        // 设置选中范围
        const startPosition = model.getPositionAt(fragmentStart);
        const endPosition = model.getPositionAt(fragmentEnd);
        editorInstance?.setSelection({
          startLineNumber: startPosition.lineNumber,
          startColumn: startPosition.column,
          endLineNumber: endPosition.lineNumber,
          endColumn: endPosition.column,
        });

        // 执行SQL
        dispatch(runSql({ id, isFragment: true }));
      }
    },
    [sqlFragments, selectedFragmentIndex, editorInstance, dispatch, id],
  );

  const run = useCallback(() => {
    // Check if view is currently being saved, if so, don't allow running SQL
    if (stage === ViewViewModelStages.Saving) {
      message.warning(tView('saveInProgress'));
      return;
    }

    // 获取选中内容
    const selection = editorInstance?.getSelection();
    const isSelectionEmpty = selection && selection.isEmpty();
    const fragment = editorInstance?.getModel()?.getValueInRange(selection!);

    // 如果有选中内容，保持现有逻辑
    if (fragment && !isSelectionEmpty) {
      dispatch(runSql({ id, isFragment: true }));
      return;
    }

    // 智能执行逻辑：无选中内容时，自动识别SQL片段
    if (!editorInstance) return;

    const fragments = getSqlFragments(editorInstance);

    // 无SQL片段，直接返回
    if (fragments.length === 0) return;

    // 只有一个SQL片段，直接执行
    if (fragments.length === 1) {
      // 直接使用第一个片段的位置信息
      setSqlFragments(fragments.map(f => f.sql));
      setSelectedFragmentIndex(0);
      handleRunCurrentFragment(0, fragments);
      return;
    }

    // 多个SQL片段，需要定位光标位置
    const position = editorInstance.getPosition();
    if (!position) return;

    const model = editorInstance.getModel();
    const offset = model?.getOffsetAt(position) || 0;

    // 找到光标所在的片段
    let currentFragmentIndex = 0;
    for (let i = 0; i < fragments.length; i++) {
      const { start, end } = fragments[i];
      // 如果光标在当前片段范围内，选择当前片段
      if (offset >= start && offset <= end) {
        currentFragmentIndex = i;
        break;
      }
      // 如果光标在当前片段之前，选择前一个片段
      if (offset < start) {
        currentFragmentIndex = Math.max(0, i - 1);
        break;
      }
      // 如果是最后一个片段，且光标在其之后，选择最后一个片段
      if (i === fragments.length - 1) {
        currentFragmentIndex = i;
        break;
      }
    }

    // 直接执行当前光标最近的SQL片段
    setSqlFragments(fragments.map(f => f.sql));
    setSelectedFragmentIndex(currentFragmentIndex);
    handleRunCurrentFragment(currentFragmentIndex, fragments);
  }, [
    dispatch,
    id,
    editorInstance,
    stage,
    tView,
    getSqlFragments,
    handleRunCurrentFragment,
  ]);

  const save = useCallback(
    (resolve?) => {
      dispatch(saveView({ resolve }));
    },
    [dispatch],
  );

  const callSave = useCallback(() => {
    // if (
    //   status !== ViewStatus.Archived &&
    //   stage === ViewViewModelStages.Saveable
    // ) {
    // 忽略是否可保存状态, 任何时候都可以保存
    if (status !== ViewStatus.Archived) {
      if (isNewView(id)) {
        showSaveForm({
          type: CommonFormTypes.Edit,
          visible: true,
          parentIdLabel: t('folder'),
          initialValues: {
            name: '',
            parentId: '',
            config: {},
          },
          onSave: (values, onClose) => {
            let index = getInsertedNodeIndex(values, viewsData);

            dispatch(
              actions.changeCurrentEditingView({
                ...values,
                parentId: values.parentId || null,
                index,
              }),
            );
            save(() => {
              onClose();
              message.success(t('saveSuccess'));
            });
          },
        });
      } else {
        save(() => {
          message.success(t('saveSuccess'));
        });
      }
    }
  }, [dispatch, actions, status, id, save, showSaveForm, viewsData, t]);

  const editorWillMount = useCallback(
    editor => {
      editor.languages.register({ id: 'sql' });
      editor.languages.setMonarchTokensProvider('sql', language);
      dispatch(
        getEditorProvideCompletionItems({
          resolve: getItems => {
            const providerRef = editor.languages.registerCompletionItemProvider(
              'sql',
              {
                provideCompletionItems: getItems,
              },
            );
            if (editorCompletionItemProviderRef) {
              editorCompletionItemProviderRef.current = providerRef;
            }
          },
        }),
      );
    },
    [dispatch, editorCompletionItemProviderRef],
  );

  const editorDidMount = useCallback(
    (editor: monaco.editor.IStandaloneCodeEditor) => {
      setEditor(editor);
      // Removing the tooltip on the read-only editor
      // https://github.com/microsoft/monaco-editor/issues/1742
      const messageContribution = editor.getContribution(
        'editor.contrib.messageController',
      );
      editor.onDidChangeCursorSelection(e => {
        dispatch(
          actions.changeCurrentEditingView({
            fragment: editor.getModel()?.getValueInRange(e.selection),
          }),
        );
      });
      editor.onDidAttemptReadOnlyEdit(() => {
        (messageContribution as any).showMessage(
          t('readonlyTip'),
          editor.getPosition(),
        );
      });
    },
    [setEditor, dispatch, actions, t],
  );

  useEffect(() => {
    editorInstance?.layout();
    return () => {
      editorInstance?.dispose();
      editorCompletionItemProviderRef?.current?.dispose();
    };
  }, [editorInstance, editorCompletionItemProviderRef]);

  useEffect(() => {
    return () => {
      setEditor(void 0);
    };
  }, [setEditor]);

  useEffect(() => {
    initActions({ onRun: run, onSave: callSave });
  }, [initActions, run, callSave]);

  useEffect(() => {
    editorInstance?.addCommand(
      monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter,
      run,
    );
    editorInstance?.addCommand(
      monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_S,
      callSave,
    );
  }, [editorInstance, run, callSave]);

  useHotkeys(
    'ctrl+enter,command+enter',
    () => {
      run();
    },
    [run],
  );

  useHotkeys(
    'ctrl+s,command+s',
    e => {
      e.preventDefault();
      callSave();
    },
    [dispatch, callSave],
  );

  // Show cache dialog when cacheConflict or cacheExpired is true
  // Prioritize cacheConflict if both are true
  useEffect(() => {
    if (cacheConflict || cacheExpired) {
      setCacheDialogVisible(true);
    }
  }, [cacheConflict, cacheExpired]);

  // Handle use local cache
  const handleUseLocalCache = useCallback(() => {
    if (cacheData) {
      dispatch(actions.changeCurrentEditingView({
        script: cacheData.script,
        sourceId: cacheData.sourceId
      }));
      // Clear cache conflict flag
      dispatch(actions.changeCurrentEditingView({ cacheConflict: false }));
    }
  }, [dispatch, actions, cacheData]);

  // Handle use remote data (discard cache)
  const handleUseRemoteData = useCallback(() => {
    // Get original server data from view model
    const originalServerData = currentView?.originalServerData;
    if (originalServerData) {
      // Update to use server data
      dispatch(actions.changeCurrentEditingView({
        script: originalServerData.script,
        sourceId: originalServerData.sourceId,
        touched: false,
      }));
    }
    // Clear browser cache
    deleteSqlFromCache(id);
    // Clear cache conflict and expired flags
    dispatch(actions.changeCurrentEditingView({
      cacheConflict: false,
      cacheExpired: false,
      cacheData: undefined,
      originalServerData: undefined,
    }));
  }, [dispatch, actions, id, currentView?.originalServerData]);

  // Handle save cache
  const handleSaveCache = useCallback(() => {
    if (cacheData) {
      // First close the dialog and clear cache flags to prevent re-showing
      setCacheDialogVisible(false);
      dispatch(actions.changeCurrentEditingView({
        cacheConflict: false,
        cacheExpired: false,
        cacheData: undefined,
      }));
      // Then update script and save the view
      dispatch(actions.changeCurrentEditingView({ script: cacheData.script }));
      save();
    }
  }, [dispatch, actions, cacheData, save]);

  const debouncedEditorChange = useMemo(() => {
    const editorChange = script => {
      dispatch(actions.changeCurrentEditingView({ script }));
    };
    return debounce(editorChange, 200);
  }, [dispatch, actions]);

  return (
    <>
      <EditorWrapper
        className={classnames({
          archived: status === ViewStatus.Archived,
        })}
      >
        <MonacoEditor
          value={script}
          language="sql"
          theme={`vs-${theme}`}
          options={{
            fontSize: FONT_SIZE_BASE * 0.875,
            minimap: { enabled: false },
            readOnly: status === ViewStatus.Archived,
          }}
          onChange={debouncedEditorChange}
          editorWillMount={editorWillMount}
          editorDidMount={editorDidMount}
        />
      </EditorWrapper>
      <CacheDialog
        visible={cacheDialogVisible}
        onCancel={() => {
          setCacheDialogVisible(false);
          // When cancel is clicked, just close the dialog but keep the cache
          dispatch(actions.changeCurrentEditingView({
            cacheConflict: false,
            cacheExpired: false,
            cacheData: undefined,
          }));
        }}
        onUseLocalCache={handleUseLocalCache}
        onUseRemoteData={handleUseRemoteData}
        onSave={handleSaveCache}
        cacheConflict={cacheConflict}
        cacheExpired={cacheExpired}
        cacheUpdatedAt={cacheData?.updatedAt}
        remoteUpdatedAt={new Date((currentView as any)?.updateTime?.replace(' ', 'T') + '+08:00').getTime()}
      />
    </>
  );
});

const EditorWrapper = styled.div`
  position: relative;
  flex: 1;
  min-height: 0;

  &.archived {
    .view-lines {
      * {
        color: ${p => p.theme.textColorDisabled};
      }
    }
  }
`;
