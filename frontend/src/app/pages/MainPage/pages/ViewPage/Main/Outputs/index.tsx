import { GithubOutlined, PauseOutlined } from '@ant-design/icons';
import { Alert, Button, Popover, Progress, Space } from 'antd';
import useI18NPrefix from 'app/hooks/useI18NPrefix';
import useResizeObserver from 'app/hooks/useResizeObserver';
import { selectSystemInfo } from 'app/slice/selectors';
import React, { memo, useCallback, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import styled from 'styled-components/macro';
import { FONT_SIZE_BASE, SPACE_MD, SPACE_TIMES, SPACE_XS } from 'styles/StyleConstants';
import { newIssueUrl } from 'utils/utils';
import { ViewViewModelStages } from '../../constants';
import { useViewSlice } from '../../slice';
import { selectCurrentEditingViewAttr } from '../../slice/selectors';
import { cancelSqlTask, getSqlTaskStatus } from '../../slice/thunks';
import { SqlTaskStatus } from '../../slice/types';
import { Error } from './Error';
import { Results } from './Results';

export const Outputs = memo(() => {
  const { actions } = useViewSlice();
  const dispatch = useDispatch();
  const systemInfo = useSelector(selectSystemInfo);
  const t = useI18NPrefix('view');

  const error = useSelector(state =>
    selectCurrentEditingViewAttr(state, { name: 'error' }),
  ) as string;
  const stage = useSelector(state =>
    selectCurrentEditingViewAttr(state, { name: 'stage' }),
  ) as ViewViewModelStages;
  const warnings = useSelector(state =>
    selectCurrentEditingViewAttr(state, { name: 'warnings' }),
  ) as string[];
  const currentTaskId = useSelector(state =>
    selectCurrentEditingViewAttr(state, { name: 'currentTaskId' }),
  ) as string;
  const currentTaskStatus = useSelector(state =>
    selectCurrentEditingViewAttr(state, { name: 'currentTaskStatus' }),
  ) as SqlTaskStatus;
  const currentTaskProgress = useSelector(state =>
    selectCurrentEditingViewAttr(state, { name: 'currentTaskProgress' }),
  ) as number;
  const currentTaskErrorMessage = useSelector(state =>
    selectCurrentEditingViewAttr(state, { name: 'currentTaskErrorMessage' }),
  ) as string;

  const { width, height, ref } = useResizeObserver({
    refreshMode: 'debounce',
    refreshRate: 200,
  });

  const removeViewWarnings = useCallback(() => {
    dispatch(
      actions.changeCurrentEditingView({
        warnings: null,
      }),
    );
  }, [dispatch, actions]);

  // Task monitoring and management
  const cancelTask = useCallback(() => {
    if (currentTaskId) {
      dispatch(cancelSqlTask({ taskId: currentTaskId }));
    }
  }, [dispatch, currentTaskId]);

  // Monitor task status with optimized polling strategy
  useEffect(() => {
    let intervalId: ReturnType<typeof setInterval> | undefined;
    let retryCount = 0;
    const MAX_RETRIES = 1800; // Maximum of 60 minutes of polling (1800 * 2s)

    // Stop polling if task is already completed
    if (currentTaskId && currentTaskStatus !== SqlTaskStatus.SUCCESS && currentTaskStatus !== SqlTaskStatus.FAILURE) {
      // Function to get polling interval based on task status and retry count
      const getPollingInterval = () => {
        // Adjust interval based on task status
        if (currentTaskStatus === SqlTaskStatus.QUEUED) {
          // Longer interval for queuing status (1 seconds)
          return 1000;
        } else if (currentTaskStatus === SqlTaskStatus.RUNNING) {
          // Dynamic interval for running status: start with 2s, increase to 5s after 10 retries
          // retryCount < 10, 从 650 递增到 2000
          return retryCount <= 10 ? 500 + 150 * (retryCount + 1) : 5000;
        }
        return 2000; // Default fallback for cancelled or other statuses
      };

      // Polling function with retry limit
      const pollTaskStatus = () => {
        if (retryCount >= MAX_RETRIES) {
          // Maximum retries reached, stop polling
          console.warn(`Task ${currentTaskId}: Maximum polling retries reached`);
          if (intervalId) {
            clearInterval(intervalId);
          }
          return;
        }

        retryCount++;
        dispatch(getSqlTaskStatus({ taskId: currentTaskId }));
      };

      // Initial poll and set up interval
      pollTaskStatus();
      intervalId = setInterval(pollTaskStatus, getPollingInterval());
    }

    return () => {
      if (intervalId) {
        clearInterval(intervalId);
      }
    };
  }, [dispatch, currentTaskId, currentTaskStatus]);

  const submitIssue = useCallback(
    type => {
      let params: any = {
        type: type,
        title: 'Sql parse bug',
      };
      if (type === 'github') {
        params.body = `version: ${systemInfo?.version}\n` + warnings.join('');
      } else {
        params.description =
          `version: ${systemInfo?.version}\n` + warnings.join('');
      }
      let url = newIssueUrl(params);
      window.open(url);
    },
    [warnings, systemInfo],
  );

  return (
    <Wrapper ref={ref}>
      {warnings && (
        <Alert
          className="warningBox"
          message=""
          description={
            <p>
              {t('sqlRunWraning')}
              <Popover
                trigger={['click']}
                placement="top"
                overlayStyle={{ width: SPACE_TIMES(96) }}
                content={t('warningDescription')}
              >
                <Button className="detail" type="link" size="small">
                  {t('detail')}
                </Button>
              </Popover>
            </p>
          }
          type="warning"
          closable={false}
          action={
            <Space>
              <Button
                type="primary"
                icon={<GithubOutlined />}
                onClick={() => submitIssue('github')}
              >
                Github
              </Button>
              <Button type="primary" onClick={() => submitIssue('gitee')}>
                Gitee
              </Button>
              <Button onClick={removeViewWarnings}>{t('close')}</Button>
            </Space>
          }
        />
      )}

      {/* Task Status Display */}
      {currentTaskId && (
        <TaskStatusWrapper>
          <Space direction="vertical" className="task-status" style={{ width: '100%' }}>
            <div className="task-info">
              <span className="task-id">{t('taskId')}: {currentTaskId}</span>
              <span className={`task-status-badge status-${currentTaskStatus.toLowerCase()}`}>
                {t(`taskStatus.${currentTaskStatus.toLowerCase()}`)}
              </span>
            </div>
            <Progress
              percent={currentTaskProgress}
              status={
                currentTaskStatus === SqlTaskStatus.QUEUED ? 'active' :
                currentTaskStatus === SqlTaskStatus.RUNNING ? 'active' :
                currentTaskStatus === SqlTaskStatus.SUCCESS ? 'success' : 'exception'
              }
            />
            {(currentTaskStatus === SqlTaskStatus.QUEUED || currentTaskStatus === SqlTaskStatus.RUNNING) && (
              <Button
                icon={<PauseOutlined />}
                onClick={cancelTask}
                type="primary"
                danger
              >
                {t('cancelTask')}
              </Button>
            )}
            {currentTaskStatus === SqlTaskStatus.FAILURE && currentTaskErrorMessage && (
              <div className="task-error">{currentTaskErrorMessage}</div>
            )}
          </Space>
        </TaskStatusWrapper>
      )}

      <Results width={width} height={height} />
      {error && <Error />}
    </Wrapper>
  );
});

const Wrapper = styled.div`
  position: relative;
  display: flex;
  flex-direction: column;
  border-top: 1px solid ${p => p.theme.borderColorSplit};

  .warningBox {
    padding: ${SPACE_XS} ${SPACE_MD};

    .detail {
      padding: 0;
    }
  }
`;

const TaskStatusWrapper = styled.div`
  padding: ${SPACE_XS} ${SPACE_MD};
  background-color: ${p => p.theme.componentBackground};
  border-bottom: 1px solid ${p => p.theme.borderColorSplit};

  .task-info {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: ${SPACE_XS};
  }

  .task-id {
    font-size: ${FONT_SIZE_BASE * 0.875}px;
    color: ${p => p.theme.textColorSnd};
  }

  .task-status-badge {
    padding: 2px 8px;
    font-size: ${FONT_SIZE_BASE * 0.8125}px;
    font-weight: 500;
    border-radius: 4px;
  }

  .status-queuing {
    color: white;
    background-color: ${p => p.theme.info};
  }

  .status-running {
    color: white;
    background-color: ${p => p.theme.processing};
  }

  .status-success {
    color: white;
    background-color: ${p => p.theme.success};
  }

  .status-failure {
    color: white;
    background-color: ${p => p.theme.error};
  }

  .task-error {
    font-size: ${FONT_SIZE_BASE * 0.875}px;
    color: ${p => p.theme.error};
  }
`;
