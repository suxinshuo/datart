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
  CaretRightOutlined,
  EyeInvisibleOutlined,
  EyeOutlined,
  FilterOutlined,
} from '@ant-design/icons';
import { Checkbox, Input, Spin, Tooltip } from 'antd';
import { Popup, ToolbarButton, Tree } from 'app/components';
import useI18NPrefix from 'app/hooks/useI18NPrefix';
import { APP_CURRENT_VERSION } from 'app/migration/constants';
import classnames from 'classnames';
import { transparentize } from 'polished';
import { memo, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import styled from 'styled-components/macro';
import {
  FONT_FAMILY,
  FONT_SIZE_BASE,
  FONT_SIZE_BODY,
  FONT_WEIGHT_MEDIUM,
  SPACE,
  SPACE_MD,
  SPACE_TIMES,
  SPACE_XS,
} from 'styles/StyleConstants';
import { CloneValueDeep, isEmptyArray } from 'utils/object';
import { uuidv4 } from 'utils/utils';
import { selectRoles } from '../../../MemberPage/slice/selectors';
import { SubjectTypes } from '../../../PermissionPage/constants';
import { SchemaTable } from '../../components/SchemaTable';
import { ViewViewModelStages } from '../../constants';
import { useViewSlice } from '../../slice';
import { selectCurrentEditingViewAttr } from '../../slice/selectors';
import {
  Column,
  ColumnPermission,
  HierarchyModel,
  ViewViewModel,
} from '../../slice/types';

const ROW_KEY = 'DATART_ROW_KEY';

interface ResultsProps {
  height?: number;
  width?: number;
}

export const Results = memo(({ height = 0, width = 0 }: ResultsProps) => {
  const { actions } = useViewSlice();
  const dispatch = useDispatch();
  const viewId = useSelector(state =>
    selectCurrentEditingViewAttr(state, { name: 'id' }),
  ) as string;
  const model = useSelector(state =>
    selectCurrentEditingViewAttr(state, { name: 'model' }),
  ) as HierarchyModel;
  const columnPermissions = useSelector(state =>
    selectCurrentEditingViewAttr(state, { name: 'columnPermissions' }),
  ) as ColumnPermission[];
  const stage = useSelector(state =>
    selectCurrentEditingViewAttr(state, { name: 'stage' }),
  ) as ViewViewModelStages;
  const previewResults = useSelector(state =>
    selectCurrentEditingViewAttr(state, { name: 'previewResults' }),
  ) as ViewViewModel['previewResults'];
  const error = useSelector(state =>
    selectCurrentEditingViewAttr(state, { name: 'error' }),
  ) as string;
  const isCancelClicked = useSelector(state =>
    selectCurrentEditingViewAttr(state, { name: 'isCancelClicked' }),
  ) as boolean;

  const roles = useSelector(selectRoles);
  const t = useI18NPrefix('view');

  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(true);
  const [visibleColumns, setVisibleColumns] = useState<Set<string>>(() => {
    if (model?.columns) {
      return new Set(Object.keys(model.columns));
    }
    return new Set<string>();
  });

  // 侧边面板的DOM引用，用于检测外部点击
  const sidebarRef = useRef<HTMLDivElement>(null);
  // 搜索关键词状态
  const [searchKeyword, setSearchKeyword] = useState('');

  const columnNames = useMemo(() => {
    return model?.columns ? Object.keys(model.columns) : [];
  }, [model]);

  // 过滤后的列名列表
  const filteredColumnNames = useMemo(() => {
    if (!searchKeyword.trim()) {
      return columnNames;
    }
    const keyword = searchKeyword.toLowerCase();
    return columnNames.filter(columnName =>
      columnName.toLowerCase().includes(keyword),
    );
  }, [columnNames, searchKeyword]);

  // 当model.columns变化时，自动更新为全选状态
  useEffect(() => {
    if (model?.columns) {
      setVisibleColumns(new Set(Object.keys(model.columns)));
    }
  }, [model?.columns]);

  // 点击外部区域关闭侧边面板
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        sidebarRef.current &&
        !isSidebarCollapsed &&
        !sidebarRef.current.contains(event.target as Node)
      ) {
        setIsSidebarCollapsed(true);
      }
    };

    // 添加全局点击事件监听器
    document.addEventListener('mousedown', handleClickOutside);

    // 清理事件监听器
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isSidebarCollapsed]);

  const handleToggleSidebar = useCallback(() => {
    setIsSidebarCollapsed(prev => !prev);
  }, []);

  const handleColumnChange = useCallback(
    (columnName: string, checked: boolean) => {
      setVisibleColumns(prev => {
        const newSet = new Set(prev);
        if (checked) {
          newSet.add(columnName);
        } else {
          newSet.delete(columnName);
        }
        return newSet;
      });
    },
    [],
  );

  const handleCheckAll = useCallback(
    (checked: boolean) => {
      if (checked) {
        setVisibleColumns(new Set(columnNames));
      } else {
        setVisibleColumns(new Set());
      }
    },
    [columnNames],
  );

  const filteredModel = useMemo(() => {
    if (!model?.columns) return {};
    const filtered: typeof model.columns = {};
    // 优化遍历逻辑，只处理可见列
    visibleColumns.forEach(col => {
      if (model.columns && model.columns[col]) {
        filtered[col] = model.columns[col];
      }
    });
    return filtered;
  }, [model, visibleColumns]);

  const isAllChecked = useMemo(() => {
    return columnNames.length > 0 && visibleColumns.size === columnNames.length;
  }, [columnNames, visibleColumns]);

  const isIndeterminate = useMemo(() => {
    return visibleColumns.size > 0 && visibleColumns.size < columnNames.length;
  }, [columnNames, visibleColumns]);

  const dataSource = useMemo(
    () =>
      previewResults
        ? previewResults.map(o => ({ ...o, [ROW_KEY]: uuidv4() }))
        : [],
    [previewResults],
  );

  const modelChange = useCallback(
    (columnName: string, column: Omit<Column, 'name'>) =>
      (keyPath: string[]) => {
        let value;
        if (keyPath[0].includes('category')) {
          const category = keyPath[0].split('-')[1];
          value = { ...column, category };
        } else if (keyPath.includes('DATE')) {
          value = { ...column, type: keyPath[1], dateFormat: keyPath[0] };
        } else {
          value = { ...column, type: keyPath[0] };
        }
        const clonedHierarchyModel = CloneValueDeep(model.hierarchy || {});
        if (columnName in clonedHierarchyModel) {
          clonedHierarchyModel[columnName] = value;
        } else {
          Object.values(clonedHierarchyModel)
            .filter(col => !isEmptyArray(col.children))
            .forEach(col => {
              const targetChildColumnIndex = col.children!.findIndex(
                child => child.name === columnName,
              );
              if (targetChildColumnIndex > -1) {
                col.children![targetChildColumnIndex] = value;
              }
            });
        }

        dispatch(
          actions.changeCurrentEditingView({
            model: {
              ...model,
              hierarchy: clonedHierarchyModel,
              version: APP_CURRENT_VERSION,
            },
          }),
        );
      },
    [dispatch, actions, model],
  );

  const roleDropdownData = useMemo(
    () =>
      roles.map(({ id, name }) => ({
        key: id,
        title: name,
        value: id,
        isLeaf: true,
      })),
    [roles],
  );

  const checkRoleColumnPermission = useCallback(
    columnName => checkedKeys => {
      const fullPermissions = Object.keys(model?.columns || {});
      dispatch(
        actions.changeCurrentEditingView({
          columnPermissions: roleDropdownData.reduce<ColumnPermission[]>(
            (updated, { key }) => {
              const permission = columnPermissions.find(
                ({ subjectId }) => subjectId === key,
              );
              const checkOnCurrentRole = checkedKeys.includes(key);
              if (permission) {
                if (checkOnCurrentRole) {
                  const updatedColumnPermission = Array.from(
                    new Set(permission.columnPermission.concat(columnName)),
                  );
                  return fullPermissions.sort().join(',') !==
                    updatedColumnPermission.sort().join(',')
                    ? updated.concat({
                        ...permission,
                        columnPermission: updatedColumnPermission,
                      })
                    : updated;
                } else {
                  return updated.concat({
                    ...permission,
                    columnPermission: permission.columnPermission.filter(
                      c => c !== columnName,
                    ),
                  });
                }
              } else {
                return !checkOnCurrentRole
                  ? updated.concat({
                      id: uuidv4(),
                      viewId,
                      subjectId: key,
                      subjectType: SubjectTypes.Role,
                      columnPermission: fullPermissions.filter(
                        c => c !== columnName,
                      ),
                    })
                  : updated;
              }
            },
            [],
          ),
        }),
      );
    },
    [dispatch, actions, viewId, model, columnPermissions, roleDropdownData],
  );

  const getExtraHeaderActions = useCallback(
    (name: string, column: Omit<Column, 'name'>) => {
      // 没有记录相当于对所有字段都有权限
      const checkedKeys =
        columnPermissions.length > 0
          ? roleDropdownData.reduce<string[]>((selected, { key }) => {
              const permission = columnPermissions.find(
                ({ subjectId }) => subjectId === key,
              );
              if (permission) {
                return permission.columnPermission.includes(name)
                  ? selected.concat(key)
                  : selected;
              } else {
                return selected.concat(key);
              }
            }, [])
          : roleDropdownData.map(({ key }) => key);
      return [
        <Popup
          key={`${name}_columnpermission`}
          trigger={['click']}
          placement="bottomRight"
          content={
            <Tree
              className="check-list medium"
              treeData={roleDropdownData}
              checkedKeys={checkedKeys}
              loading={false}
              selectable={false}
              showIcon={false}
              onCheck={checkRoleColumnPermission(name)}
              blockNode
              checkable
            />
          }
        >
          <Tooltip title={t('columnPermission.title')}>
            <ToolbarButton
              size="small"
              iconSize={FONT_SIZE_BASE}
              icon={
                checkedKeys.length > 0 ? (
                  <EyeOutlined
                    className={classnames({
                      partial: checkedKeys.length !== roleDropdownData.length,
                    })}
                  />
                ) : (
                  <EyeInvisibleOutlined />
                )
              }
            />
          </Tooltip>
        </Popup>,
      ];
    },
    [columnPermissions, roleDropdownData, checkRoleColumnPermission, t],
  );

  const pagination = useMemo(
    () => ({
      defaultPageSize: 50,
      pageSizeOptions: ['10', '20', '50', '100', '200', '500'],
      showSizeChanger: true,
      showQuickJumper: true,
      showTotal: total => `共 ${total} 条记录`,
    }),
    [],
  );

  // 确定是否显示结果表格和列筛选项
  const shouldShowResults =
    stage > ViewViewModelStages.Fresh && !error && !isCancelClicked;

  return shouldShowResults ? (
    <TableWrapper>
      <SidebarContainer
        ref={sidebarRef}
        className={isSidebarCollapsed ? 'collapsed' : 'expanded'}
      >
        <SidebarToggle onClick={handleToggleSidebar}>
          <FilterOutlined />
          {!isSidebarCollapsed && (
            <span className="label">{t('columnFilter')}</span>
          )}
        </SidebarToggle>
        {!isSidebarCollapsed && (
          <ColumnList>
            <ColumnListHeader>
              <Checkbox
                checked={isAllChecked}
                indeterminate={isIndeterminate}
                onChange={e => handleCheckAll(e.target.checked)}
              >
                {t('selectAll')}
              </Checkbox>
              <SearchContainer>
                <Input
                  placeholder={t('search')}
                  value={searchKeyword}
                  onChange={e => setSearchKeyword(e.target.value)}
                  allowClear
                />
              </SearchContainer>
            </ColumnListHeader>
            <ColumnListBody>
              {filteredColumnNames.length > 0 ? (
                filteredColumnNames.map(columnName => (
                  <ColumnListItem key={columnName}>
                    <Checkbox
                      checked={visibleColumns.has(columnName)}
                      onChange={e =>
                        handleColumnChange(columnName, e.target.checked)
                      }
                    >
                      {columnName}
                    </Checkbox>
                  </ColumnListItem>
                ))
              ) : (
                <EmptyColumnList>{t('noColumnsFound')}</EmptyColumnList>
              )}
            </ColumnListBody>
          </ColumnList>
        )}
      </SidebarContainer>
      <SchemaTable
        height={height ? height - 90 : 0}
        width={width}
        model={filteredModel}
        hierarchy={model.hierarchy || {}}
        dataSource={dataSource}
        pagination={pagination}
        getExtraHeaderActions={getExtraHeaderActions}
        onSchemaTypeChange={modelChange}
        hasCategory
        expandable={{
          childrenColumnName: '___not_children___',
        }}
      />
      {stage === ViewViewModelStages.Running && (
        <LoadingMask>
          <Spin />
        </LoadingMask>
      )}
    </TableWrapper>
  ) : (
    <InitialDesc>
      <p>
        {t('resultEmpty1')}
        <CaretRightOutlined />
        {t('resultEmpty2')}
      </p>
    </InitialDesc>
  );
});

const InitialDesc = styled.div`
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;

  p {
    color: ${p => p.theme.textColorLight};
  }
`;

const TableWrapper = styled.div`
  position: relative;
  flex: 1;
  overflow: hidden;
  font-family: ${FONT_FAMILY};
  background-color: ${p => p.theme.componentBackground};
`;

const LoadingMask = styled.div`
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: ${p => transparentize(0.5, p.theme.componentBackground)};
`;

const SidebarContainer = styled.div`
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 5;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: ${p => p.theme.componentBackground};
  border-right: 1px solid ${p => p.theme.borderColorSplit};
  transition: width 0.3s ease;

  &.collapsed {
    width: ${SPACE_TIMES(12)};
  }

  &.expanded {
    width: ${SPACE_TIMES(50)};
  }
`;

const SidebarToggle = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  height: ${SPACE_TIMES(10)};
  padding: ${SPACE_XS} ${SPACE};
  cursor: pointer;
  border-bottom: 1px solid ${p => p.theme.borderColorSplit};
  transition: background-color 0.2s ease;

  &:hover {
    background-color: ${p => p.theme.emphasisBackground};
  }

  .anticon {
    font-size: ${FONT_SIZE_BODY};
    color: ${p => p.theme.primary};
  }

  .label {
    margin-left: ${SPACE_XS};
    font-size: ${FONT_SIZE_BODY};
    font-weight: ${FONT_WEIGHT_MEDIUM};
    color: ${p => p.theme.textColor};
    white-space: nowrap;
  }
`;

const ColumnList = styled.div`
  display: flex;
  flex: 1;
  flex-direction: column;
  overflow-y: auto;
`;

const ColumnListHeader = styled.div`
  display: flex;
  gap: ${SPACE};
  align-items: center;
  justify-content: space-between;
  height: ${SPACE_TIMES(10)};
  padding: ${SPACE_XS} ${SPACE};
  background-color: ${p => p.theme.componentBackground};
  border-bottom: 1px solid ${p => p.theme.borderColorSplit};

  .ant-checkbox-wrapper {
    font-size: ${FONT_SIZE_BODY};
    font-weight: ${FONT_WEIGHT_MEDIUM};
  }
`;

const ColumnListBody = styled.div`
  flex: 1;
  overflow-y: auto;
`;

const ColumnListItem = styled.div`
  display: flex;
  align-items: center;
  height: ${SPACE_TIMES(8)};
  padding: ${SPACE_XS} ${SPACE};
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: ${p => p.theme.emphasisBackground};
  }

  .ant-checkbox-wrapper {
    width: 100%;
    font-size: ${FONT_SIZE_BODY};
    color: ${p => p.theme.textColor};
  }
`;

const SearchContainer = styled.div`
  display: flex;
  flex: 1;
  align-items: center;
  background-color: ${p => p.theme.componentBackground};

  .ant-input {
    font-size: ${FONT_SIZE_BODY};
    line-height: ${SPACE_MD};
  }
`;

const EmptyColumnList = styled.div`
  padding: ${SPACE_MD};
  font-size: ${FONT_SIZE_BODY};
  color: ${p => p.theme.textColorLight};
  text-align: center;
`;
