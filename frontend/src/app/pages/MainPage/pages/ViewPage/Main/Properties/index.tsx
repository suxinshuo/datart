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
  ApartmentOutlined,
  DatabaseOutlined,
  FunctionOutlined,
  HistoryOutlined,
  RobotOutlined,
  SafetyCertificateOutlined,
} from '@ant-design/icons';
import { PaneWrapper } from 'app/components';
import useI18NPrefix from 'app/hooks/useI18NPrefix';
import { selectIsFocusMode } from 'app/pages/MainPage/slice/focusModeSelectors';
import {
  memo,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';
import { useSelector } from 'react-redux';
import styled from 'styled-components/macro';
import { LEVEL_1 } from 'styles/StyleConstants';
import { EditorContext } from '../../EditorContext';
import { ColumnPermissions } from './ColumnPermissions';
import DataModelTree from './DataModelTree/DataModelTree';
import { History } from './History';
import { Resource } from './Resource';
import { SQLAssistant } from './SQLAssistant';
import { Variables } from './Variables';
import { VerticalTabs } from './VerticalTabs';

interface PropertiesProps {
  allowManage: boolean;
  viewType: string;
}

export const Properties = memo(({ allowManage, viewType }: PropertiesProps) => {
  const [selectedTab, setSelectedTab] = useState('');
  const { editorInstance } = useContext(EditorContext);
  const t = useI18NPrefix('view.properties');
  const isFocusMode = useSelector(selectIsFocusMode); // 获取专注模式状态

  useEffect(() => {
    editorInstance?.layout();
  }, [editorInstance, selectedTab]);

  const tabTitle = useMemo(() => {
    let tabTitle = [
      { name: 'reference', title: t('reference'), icon: <DatabaseOutlined /> },
      { name: 'variable', title: t('variable'), icon: <FunctionOutlined /> },
      { name: 'model', title: t('model'), icon: <ApartmentOutlined /> },
      {
        name: 'columnPermissions',
        title: t('columnPermissions'),
        icon: <SafetyCertificateOutlined />,
      },
      { name: 'history', title: t('history'), icon: <HistoryOutlined /> },
      {
        name: 'sqlAssistant',
        title: t('sqlAssistant'),
        icon: <RobotOutlined />,
      },
    ];

    // 在专注模式下隐藏"数据模型"和"列权限"标签页
    if (isFocusMode) {
      tabTitle = tabTitle.filter(
        tab => tab.name !== 'model' && tab.name !== 'columnPermissions',
      );
    }

    return viewType === 'STRUCT'
      ? tabTitle.slice(2, tabTitle.length)
      : tabTitle;
  }, [t, viewType, isFocusMode]);

  const tabSelect = useCallback(tab => {
    setSelectedTab(tab);
  }, []);

  return allowManage ? (
    <Container>
      <PaneWrapper selected={selectedTab === 'variable'}>
        <Variables />
      </PaneWrapper>
      <PaneWrapper selected={selectedTab === 'reference'}>
        <Resource />
      </PaneWrapper>
      {/* 在专注模式下隐藏数据模型和列权限组件 */}
      {!isFocusMode && (
        <>
          <PaneWrapper selected={selectedTab === 'model'}>
            <DataModelTree />
          </PaneWrapper>
          <PaneWrapper selected={selectedTab === 'columnPermissions'}>
            <ColumnPermissions />
          </PaneWrapper>
        </>
      )}
      <PaneWrapper selected={selectedTab === 'history'}>
        <History />
      </PaneWrapper>
      <PaneWrapper selected={selectedTab === 'sqlAssistant'}>
        <SQLAssistant />
      </PaneWrapper>
      <VerticalTabs tabs={tabTitle} onSelect={tabSelect} />
    </Container>
  ) : null;
});

const Container = styled.div`
  z-index: ${LEVEL_1};
  display: flex;
  flex-shrink: 0;
  background-color: ${p => p.theme.componentBackground};
`;
