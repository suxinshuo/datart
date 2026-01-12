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
import {
  CalendarOutlined,
  DatabaseOutlined,
  FieldStringOutlined,
  MoreOutlined,
  NumberOutlined,
  SearchOutlined,
  TableOutlined,
} from '@ant-design/icons';
import {
  Button,
  Col,
  Dropdown,
  Input,
  Menu,
  Modal,
  Radio,
  Row,
  Space,
  TreeDataNode,
} from 'antd';
import { Tree } from 'app/components';
import { DataViewFieldType } from 'app/constants';
import useI18NPrefix from 'app/hooks/useI18NPrefix';
import useResizeObserver from 'app/hooks/useResizeObserver';
import { useSearchAndExpand } from 'app/hooks/useSearchAndExpand';
import { updateBy } from 'app/utils/mutation';
import { DEFAULT_DEBOUNCE_WAIT } from 'globalConstants';
import { memo, useCallback, useContext, useEffect, useState } from 'react';
import { monaco } from 'react-monaco-editor';
import { useDispatch, useSelector } from 'react-redux';
import styled from 'styled-components/macro';
import { SPACE_MD, SPACE_XS } from 'styles/StyleConstants';
import { RootState } from 'types';
import { EditorContext } from '../../EditorContext';
import {
  selectCurrentEditingViewAttr,
  selectDatabaseSchemaLoading,
  selectSourceDatabaseSchemas,
} from '../../slice/selectors';
import { getEditorProvideCompletionItems } from '../../slice/thunks';
import { DatabaseSchema } from '../../slice/types';
import { buildAntdTreeNodeModel } from '../../utils';
import Container from './Container';

export const Resource = memo(() => {
  const t = useI18NPrefix('view.resource');
  const dispatch = useDispatch();
  const { editorInstance, editorCompletionItemProviderRef } =
    useContext(EditorContext);
  const isDatabaseSchemaLoading = useSelector(selectDatabaseSchemaLoading);
  const sourceId = useSelector<RootState>(state =>
    selectCurrentEditingViewAttr(state, { name: 'sourceId' }),
  ) as string;
  const databaseSchemas = useSelector(state =>
    selectSourceDatabaseSchemas(state, { id: sourceId }),
  );
  const [databaseTreeModel, setDatabaseTreeModel] = useState<TreeDataNode[]>(
    [],
  );

  const { height, ref: treeWrapperRef } = useResizeObserver({
    refreshMode: 'debounce',
    refreshRate: 200,
  });

  const buildTableNode = useCallback((database: DatabaseSchema) => {
    const children =
      database?.tables?.map(table => {
        return buildTableColumnNode([database.dbName], table);
      }) || [];

    return buildAntdTreeNodeModel([], database.dbName, children, false);
  }, []);

  const buildTableColumnNode = (ancestors: string[] = [], table) => {
    const children =
      table?.columns?.map(column => {
        return Object.assign(
          buildAntdTreeNodeModel(
            ancestors.concat(table.tableName),
            column?.name[0],
            [],
            true,
          ),
          { type: column?.type },
        );
      }) || [];
    return buildAntdTreeNodeModel(ancestors, table.tableName, children, false);
  };

  useEffect(() => {
    setDatabaseTreeModel((databaseSchemas || []).map(buildTableNode));
  }, [buildTableNode, databaseSchemas]);

  const { filteredData, onExpand, debouncedSearch, expandedRowKeys } =
    useSearchAndExpand(
      databaseTreeModel,
      (keywords, data) => {
        // Check if search keyword contains '.'
        if (keywords.includes('.')) {
          const [dbName, tableName] = keywords.split('.');
          const nodeValue = data['value'] as string[];

          // For database nodes (level 1)
          if (nodeValue.length === 1) {
            return nodeValue[0] === dbName;
          }

          // For table nodes (level 2) or column nodes (level 3)
          if (nodeValue.length >= 2) {
            return (
              nodeValue[0] === dbName &&
              (tableName === '' || nodeValue[1].includes(tableName))
            );
          }

          return false;
        }

        return (data.title as string).includes(keywords);
      },
      DEFAULT_DEBOUNCE_WAIT,
      true,
    );

  useEffect(() => {
    if (databaseTreeModel && editorCompletionItemProviderRef) {
      editorCompletionItemProviderRef.current?.dispose();
      dispatch(
        getEditorProvideCompletionItems({
          sourceId,
          resolve: getItem => {
            editorCompletionItemProviderRef.current =
              monaco.languages.registerCompletionItemProvider('sql', {
                provideCompletionItems: getItem,
              });
          },
        }),
      );
    }
  }, [dispatch, sourceId, databaseTreeModel, editorCompletionItemProviderRef]);

  const renderIcon = useCallback(({ value, type }) => {
    if (Array.isArray(value)) {
      switch (value.length) {
        case 1:
          return <DatabaseOutlined />;
        case 2:
          return <TableOutlined />;
        case 3:
          switch (type as DataViewFieldType) {
            case DataViewFieldType.STRING:
              return <FieldStringOutlined />;
            case DataViewFieldType.NUMERIC:
              return <NumberOutlined />;
            case DataViewFieldType.DATE:
              return <CalendarOutlined />;
          }
      }
    }
  }, []);

  const sortListOrTree = useCallback((list, sortType, sortKey) => {
    return updateBy(list, draft => {
      draft.sort((a, b) => {
        let aname = a[sortKey],
          bname = b[sortKey];
        return String(aname).localeCompare(String(bname));
      });
      draft.forEach(item => {
        if (item.children) {
          item.children = sortListOrTree(item.children, sortType, sortKey);
        }
      });
    });
  }, []);

  const handleMenuClick = useCallback(
    ({ key }) => {
      if (key === 'byNameSort') {
        setDatabaseTreeModel(sortListOrTree(databaseTreeModel, key, 'title'));
      } else {
        setDatabaseTreeModel((databaseSchemas || []).map(buildTableNode));
      }
    },
    [databaseTreeModel, sortListOrTree, databaseSchemas, buildTableNode],
  );

  const getOverlays = useCallback(() => {
    return (
      <Menu onClick={handleMenuClick}>
        <Menu.SubMenu key="sort" title={t('sortType')}>
          <Menu.Item key="byNameSort">{t('byName')}</Menu.Item>
          <Menu.Item key="byOriginalFieldSort">{t('byField')}</Menu.Item>
        </Menu.SubMenu>
      </Menu>
    );
  }, [handleMenuClick, t]);

  const onDragStart = useCallback(
    info => {
      const { event, node } = info;

      // 获取完整的表信息，包括字段列表
      let tableInfo = {
        title: node.title,
        value: node.value,
        type: node.type,
        columns: [],
      };

      // 如果是表节点，查找其字段信息
      if (Array.isArray(node.value) && node.value.length === 2) {
        const [dbName, tableName] = node.value;
        const database = databaseSchemas?.find(db => db.dbName === dbName);
        if (database) {
          const table = database.tables?.find(t => t.tableName === tableName);
          if (table) {
            tableInfo.columns = table.columns?.map(col => col.name[0]) || [];
          }
        }
      }

      event.dataTransfer.setData(
        'application/sql-drag',
        JSON.stringify(tableInfo),
      );
      event.dataTransfer.effectAllowed = 'copy';
    },
    [databaseSchemas],
  );

  useEffect(() => {
    // Add global drag over handler to allow dropping into editor
    const handleGlobalDragOver = e => {
      e.preventDefault();
      e.dataTransfer.dropEffect = 'copy';
    };

    const handleGlobalDrop = e => {
      const dragData = e.dataTransfer.getData('application/sql-drag');
      if (dragData && editorInstance) {
        e.preventDefault();
        const dragInfo = JSON.parse(dragData);
        const { title, value, columns } = dragInfo;
        let insertText = title;

        // Handle different node types
        if (Array.isArray(value)) {
          switch (value.length) {
            case 1: // Database
              insertText = `\`${title}\``;
              // 直接插入
              insertIntoEditor(insertText);
              break;
            case 2: // Table - 显示弹窗选择填充方式
              const [dbName, tableName] = value;

              // 使用ref管理选择值
              let fillOption = 'tableOnly';

              // 弹出选择窗口
              Modal.confirm({
                title: t('fillMode'),
                content: (
                  <Radio.Group
                    defaultValue="tableOnly"
                    onChange={e => (fillOption = e.target.value)}
                    buttonStyle="solid"
                    style={{ width: '100%' }}
                  >
                    <Radio.Button
                      value="tableOnly"
                      style={{ width: '100%', marginBottom: 10 }}
                    >
                      {t('dbTable')}
                    </Radio.Button>
                    <Radio.Button value="withSelect" style={{ width: '100%' }}>
                      {t('selectStatement')}
                    </Radio.Button>
                  </Radio.Group>
                ),
                onOk: () => {
                  let sqlText = '';

                  // 根据选择生成SQL文本
                  if (fillOption === 'tableOnly') {
                    // 选项一：仅填充表名
                    sqlText = `\`${dbName}\`.\`${tableName}\``;
                  } else {
                    // 选项二：生成SELECT语句
                    if (columns.length > 15) {
                      // 字段数量超过15个，使用SELECT *
                      sqlText = `SELECT *\nFROM \`${dbName}\`.\`${tableName}\``;
                    } else {
                      // 生成完整的SELECT字段列表
                      const columnsList = columns
                        .map(col => `    \`${col}\``)
                        .join(',\n');
                      sqlText = `SELECT\n${columnsList}\nFROM \`${dbName}\`.\`${tableName}\``;
                    }
                  }

                  // 插入到编辑器
                  insertIntoEditor(sqlText);
                },
                okText: t('confirm'),
                cancelText: t('cancel'),
              });
              break;
            case 3: // Column
              insertText = `\`${title}\``;
              // 直接插入
              insertIntoEditor(insertText);
              break;
            default:
              insertText = title;
              // 直接插入
              insertIntoEditor(insertText);
          }
        }

        // 插入到编辑器的函数
        function insertIntoEditor(text) {
          // Get current position and create range
          const position = editorInstance.getPosition();
          if (position) {
            const range = {
              startLineNumber: position.lineNumber,
              startColumn: position.column,
              endLineNumber: position.lineNumber,
              endColumn: position.column,
            };

            // Insert into editor
            editorInstance.executeEdits('drag-drop', [
              {
                range,
                text,
                forceMoveMarkers: true,
              },
            ]);
          }
        }
      }
    };

    document.addEventListener('dragover', handleGlobalDragOver);
    document.addEventListener('drop', handleGlobalDrop);

    return () => {
      document.removeEventListener('dragover', handleGlobalDragOver);
      document.removeEventListener('drop', handleGlobalDrop);
    };
  }, [editorInstance, t]);

  return (
    <Container title="reference">
      <SearchBar>
        <Col span={24}>
          <StyleSpace>
            <Input
              prefix={<SearchOutlined className="icon" />}
              placeholder={t('search')}
              className="input"
              bordered={false}
              onChange={debouncedSearch}
            />
            <Dropdown key="more" trigger={['click']} overlay={getOverlays()}>
              <Button type="text" icon={<MoreOutlined />} />
            </Dropdown>
          </StyleSpace>
        </Col>
      </SearchBar>
      <TreeWrapper ref={treeWrapperRef}>
        <Tree
          className="medium"
          treeData={filteredData}
          loading={isDatabaseSchemaLoading}
          icon={renderIcon}
          selectable={false}
          defaultExpandedKeys={expandedRowKeys}
          height={height}
          onExpand={onExpand}
          draggable={true}
          onDragStart={onDragStart}
        />
      </TreeWrapper>
    </Container>
  );
});

const SearchBar = styled(Row)`
  .input {
    padding-bottom: ${SPACE_XS};
  }

  .icon {
    color: ${p => p.theme.textColorLight};
  }
`;

const TreeWrapper = styled.div`
  flex: 1;
  padding-bottom: ${SPACE_MD};
  overflow-y: auto;
`;

const StyleSpace = styled(Space)`
  width: 100%;
  > div:nth-child(1) {
    flex: 1;
  }
`;
