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
import { DownloadOutlined } from '@ant-design/icons';
import { Button, Select, Space, Switch, Tooltip } from 'antd';
import SaveToDashboard from 'app/components/SaveToDashboard';
import useI18NPrefix from 'app/hooks/useI18NPrefix';
import useMount from 'app/hooks/useMount';
import { useWorkbenchSlice } from 'app/pages/ChartWorkbenchPage/slice';
import { DownloadListPopup } from 'app/pages/MainPage/Navbar/DownloadListPopup';
import { loadTasks } from 'app/pages/MainPage/Navbar/service';
import { selectHasVizFetched } from 'app/pages/MainPage/pages/VizPage/slice/selectors';
import { getFolders } from 'app/pages/MainPage/pages/VizPage/slice/thunks';
import { downloadFile } from 'app/utils/fetch';
import { FC, memo, useCallback, useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import styled from 'styled-components/macro';
import {
  FONT_SIZE_ICON_SM,
  FONT_WEIGHT_MEDIUM,
  LINE_HEIGHT_ICON_SM,
  SPACE_MD,
  SPACE_SM,
  SPACE_XS,
} from 'styles/StyleConstants';
import { request2 } from 'utils/request';
import { errorHandle } from 'utils/utils';
import {
  backendChartSelector,
  currentDataViewSelector,
  selectChartEditorDownloadPolling,
} from '../../slice/selectors';

const ChartHeaderPanel: FC<{
  chartName?: string;
  orgId?: string;
  container?: string;
  onSaveChart?: () => void;
  onGoBack?: () => void;
  onSaveChartToDashBoard?: (dashboardId, dashboardType) => void;
}> = memo(
  ({
    chartName,
    orgId,
    container,
    onSaveChart,
    onGoBack,
    onSaveChartToDashBoard,
  }) => {
    const t = useI18NPrefix(`viz.workbench.header`);
    const hasVizFetched = useSelector(selectHasVizFetched);
    const [isModalVisible, setIsModalVisible] = useState<boolean>(false);
    const backendChart = useSelector(backendChartSelector);
    const currentDataView = useSelector(currentDataViewSelector);
    const downloadPolling = useSelector(selectChartEditorDownloadPolling);
    const dispatch = useDispatch();
    const { actions } = useWorkbenchSlice();

    // 静态分析相关状态
    const [staticAnalysis, setStaticAnalysis] = useState<boolean>(true);
    const [sqlTaskId, setSqlTaskId] = useState<string>('');
    const [sqlTaskHistory, setSqlTaskHistory] = useState<any[]>([]);
    const [loadingHistory, setLoadingHistory] = useState<boolean>(false);

    // 获取SQL任务历史
    const fetchSqlTaskHistory = useCallback(async () => {
      if (!currentDataView?.id) return;

      setLoadingHistory(true);
      try {
        const response = await request2<any[]>(
          `/execute/tasks/${currentDataView.id}/history`,
          {
            params: {},
          },
        );

        const tasks = response.data || [];
        setSqlTaskHistory(tasks);

        // 设置默认选中最近的成功执行
        const latestSuccessTask = tasks.find(task => task.status === 'SUCCESS');
        if (latestSuccessTask) {
          setSqlTaskId(latestSuccessTask.id);
          dispatch(actions.updateSqlTaskId(latestSuccessTask.id));
        }
      } catch (error) {
        errorHandle(error);
      } finally {
        setLoadingHistory(false);
      }
    }, [currentDataView]);

    // 静态分析开关变化
    const handleStaticAnalysisChange = useCallback(
      (checked: boolean) => {
        setStaticAnalysis(checked);
        dispatch(actions.updateStaticAnalysis(checked));
      },
      [dispatch, actions],
    );

    // SQL任务结果ID变化
    const handleSqlTaskIdChange = useCallback(
      (value: string) => {
        setSqlTaskId(value);
        dispatch(actions.updateSqlTaskId(value));
      },
      [dispatch, actions],
    );

    const handleModalOk = useCallback(
      (dashboardId: string, dashboardType: string) => {
        onSaveChartToDashBoard?.(dashboardId, dashboardType);
        setIsModalVisible(true);
      },
      [onSaveChartToDashBoard],
    );

    const handleModalCancel = useCallback(() => {
      setIsModalVisible(false);
    }, []);

    const handleModalOpen = useCallback(() => {
      setIsModalVisible(true);
    }, []);

    const onSetPolling = useCallback(
      (polling: boolean) => {
        dispatch(actions.setChartEditorDownloadPolling(polling));
      },
      [dispatch, actions],
    );

    useMount(() => {
      if (!hasVizFetched) {
        // Request data when there is no data
        dispatch(getFolders(orgId as string));
      }
    });

    // 当currentDataView变化时，重新获取SQL任务历史
    useEffect(() => {
      if (currentDataView?.id) {
        fetchSqlTaskHistory();
      }
    }, [fetchSqlTaskHistory, currentDataView, staticAnalysis]);

    return (
      <Wrapper>
        <h1>{chartName}</h1>
        <Space>
          {/* SQL任务历史下拉菜单 */}
          {staticAnalysis && (
            <Select
              value={sqlTaskId}
              onChange={handleSqlTaskIdChange}
              loading={loadingHistory}
              placeholder={t('selectHistoricalExecutionResult')}
              style={{ width: 300 }}
            >
              {sqlTaskHistory.map(task => (
                <Select.Option key={task.id} value={task.id}>
                  {new Date(task.submitTime).toLocaleString()} - {task.status}
                </Select.Option>
              ))}
            </Select>
          )}

          {/* 基于历史执行结果分析 */}
          <Space align="center">
            <Tooltip title={t('analysisBasedStatic')}>
              <Switch
                checked={staticAnalysis}
                onChange={handleStaticAnalysisChange}
              />
            </Tooltip>
            <span>{t('analysisBasedStatic')}</span>
          </Space>

          <DownloadListPopup
            polling={downloadPolling}
            setPolling={onSetPolling}
            onLoadTasks={loadTasks}
            onDownloadFile={item => {
              if (item.id) {
                downloadFile(item.id).then(() => {
                  dispatch(actions.setChartEditorDownloadPolling(true));
                });
              }
            }}
            renderDom={
              <Button icon={<DownloadOutlined />}>{t('downloadList')}</Button>
            }
          />
          <Button onClick={onGoBack}>{t('cancel')}</Button>
          <Tooltip
            title={staticAnalysis ? t('analysisBasedStaticTip') : undefined}
            placement="bottom"
          >
            <Button
              type="primary"
              onClick={onSaveChart}
              disabled={staticAnalysis}
            >
              {t('save')}
            </Button>
          </Tooltip>
          {!(container === 'widget') && (
            <Tooltip
              title={staticAnalysis ? t('analysisBasedStaticTip') : undefined}
              placement="bottom"
            >
              <Button
                type="primary"
                onClick={() => {
                  setIsModalVisible(true);
                }}
                disabled={staticAnalysis}
              >
                {t('saveToDashboard')}
              </Button>
            </Tooltip>
          )}
          <SaveToDashboard
            orgId={orgId as string}
            title={t('saveToDashboard')}
            isModalVisible={isModalVisible}
            backendChartId={backendChart?.id}
            handleOk={handleModalOk}
            handleCancel={handleModalCancel}
            handleOpen={handleModalOpen}
          ></SaveToDashboard>
        </Space>
      </Wrapper>
    );
  },
);

export default ChartHeaderPanel;

const Wrapper = styled.div`
  display: flex;
  flex-shrink: 0;
  align-items: center;
  padding: ${SPACE_SM} ${SPACE_MD} ${SPACE_SM} ${SPACE_SM};
  background-color: ${p => p.theme.componentBackground};
  border-bottom: 1px solid ${p => p.theme.borderColorSplit};

  h1 {
    flex: 1;
    padding: 0 ${SPACE_XS};
    font-size: ${FONT_SIZE_ICON_SM};
    font-weight: ${FONT_WEIGHT_MEDIUM};
    line-height: ${LINE_HEIGHT_ICON_SM};
  }
`;
