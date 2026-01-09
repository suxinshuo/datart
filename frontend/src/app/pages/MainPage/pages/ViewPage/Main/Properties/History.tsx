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
  CopyOutlined,
  EyeOutlined,
  LoadingOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { Button, List, Modal, Spin, Tag, Tooltip, Typography } from 'antd';
import useI18NPrefix from 'app/hooks/useI18NPrefix';
import copy from 'copy-to-clipboard';
import { memo, useCallback, useEffect, useState } from 'react';
import styled from 'styled-components/macro';
import {
  ERROR,
  G50,
  SPACE,
  SPACE_MD,
  SPACE_XS,
  SUCCESS,
  WARNING,
} from 'styles/StyleConstants';
import { request2 } from 'utils/request';
import { errorHandle } from 'utils/utils';
import Container from './Container';

const { Text, Paragraph } = Typography;

interface TaskHistory {
  id: string;
  status: 'SUCCESS' | 'FAILED' | 'RUNNING' | 'CANCELLED';
  query: string;
  submitTime: string;
  startTime: string;
  endTime: string;
  duration: number;
  failType: string;
  errorMessage: string;
}

export const History = memo(() => {
  const [tasks, setTasks] = useState<TaskHistory[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [hasMore, setHasMore] = useState(true);
  const [detailTask, setDetailTask] = useState<TaskHistory | null>(null);
  const t = useI18NPrefix('view.history');

  // 获取查询历史数据
  const fetchHistory = useCallback(
    async (currentPage = 1) => {
      if (!hasMore && currentPage > 1) return;

      setLoading(true);
      try {
        const response = await request2<TaskHistory[]>(
          '/execute/tasks/history',
          {
            params: {
              page: currentPage,
              size: 20,
            },
          },
        );

        const newTasks = response.data || [];
        setTasks(prev =>
          currentPage === 1 ? newTasks : [...prev, ...newTasks],
        );
        setHasMore(newTasks.length >= 20);
        setPage(currentPage);
      } catch (error) {
        errorHandle(error);
      } finally {
        setLoading(false);
      }
    },
    [hasMore],
  );

  // 初始加载
  useEffect(() => {
    fetchHistory(1);
  }, [fetchHistory]);

  // 加载更多
  const loadMore = useCallback(() => {
    if (!loading && hasMore) {
      fetchHistory(page + 1);
    }
  }, [loading, hasMore, page, fetchHistory]);

  // 刷新历史记录
  const handleRefresh = useCallback(() => {
    setHasMore(true);
    fetchHistory(1);
  }, [fetchHistory]);

  // 查看详情
  const handleViewDetail = useCallback((task: TaskHistory) => {
    setDetailTask(task);
  }, []);

  // 关闭详情
  const handleCloseDetail = useCallback(() => {
    setDetailTask(null);
  }, []);

  // 复制查询语句
  const handleCopyQuery = useCallback(
    (task: TaskHistory) => {
      const success = copy(task.query);
      if (success) {
        Modal.success({
          title: t('copySuccess'),
          content: t('copySuccessMessage'),
        });
      } else {
        Modal.error({
          title: t('copyFailed'),
          content: t('copyFailedMessage'),
        });
      }
    },
    [t],
  );

  // 获取状态样式
  const getStatusConfig = useCallback(
    (status: string) => {
      switch (status) {
        case 'SUCCESS':
          return {
            text: t('status.success'),
            color: SUCCESS,
          };
        case 'FAILED':
          return {
            text: t('status.failed'),
            color: ERROR,
          };
        case 'RUNNING':
          return {
            text: t('status.running'),
            color: WARNING,
          };
        case 'CANCELLED':
          return {
            text: t('status.cancelled'),
            color: G50,
          };
        default:
          return {
            text: t('status.unknown'),
            color: G50,
          };
      }
    },
    [t],
  );

  // 格式化时间
  const formatTime = useCallback((time: string) => {
    if (!time) return '-';
    const date = new Date(time);
    return date.toLocaleString();
  }, []);

  // 格式化耗时
  const formatDuration = useCallback((duration: number) => {
    if (!duration) return '-';
    if (duration < 1000) {
      return `${duration}ms`;
    } else if (duration < 60000) {
      return `${(duration / 1000).toFixed(2)}s`;
    } else {
      return `${(duration / 60000).toFixed(2)}min`;
    }
  }, []);

  // 渲染任务项
  const renderItem = useCallback(
    (task: TaskHistory) => {
      const statusConfig = getStatusConfig(task.status);

      return (
        <TaskItemWrapper key={task.id}>
          <List.Item
            actions={[
              <Tooltip title={t('viewDetail')}>
                <Button
                  type="text"
                  size="small"
                  icon={<EyeOutlined />}
                  onClick={() => handleViewDetail(task)}
                />
              </Tooltip>,
            ]}
            style={{ padding: 0, border: 'none', margin: 0 }}
          >
            <List.Item.Meta
              title={
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    marginBottom: '2px',
                  }}
                >
                  <Tag
                    color={statusConfig.color}
                    style={{ fontSize: '12px', padding: '0 6px' }}
                  >
                    {statusConfig.text}
                  </Tag>
                  <Text type="secondary" style={{ fontSize: '12px' }}>
                    {formatTime(task.submitTime)}
                  </Text>
                </div>
              }
              description={
                <div>
                  <Paragraph
                    ellipsis={{ rows: 2, expandable: false }}
                    style={{
                      margin: '0 0 4px 0',
                      fontSize: '13px',
                      lineHeight: 1.3,
                    }}
                  >
                    {task.query}
                  </Paragraph>
                  <HistoryMeta>
                    <MetaItem>
                      <Text type="secondary" style={{ fontSize: '11px' }}>
                        {t('startTime')}:
                      </Text>
                      <Text style={{ marginLeft: '4px', fontSize: '11px' }}>
                        {formatTime(task.startTime)}
                      </Text>
                    </MetaItem>
                    <MetaItem>
                      <Text type="secondary" style={{ fontSize: '11px' }}>
                        {t('endTime')}:
                      </Text>
                      <Text style={{ marginLeft: '4px', fontSize: '11px' }}>
                        {formatTime(task.endTime)}
                      </Text>
                    </MetaItem>
                    <MetaItem>
                      <Text type="secondary" style={{ fontSize: '11px' }}>
                        {t('duration')}:
                      </Text>
                      <Text
                        style={{
                          marginLeft: '4px',
                          fontSize: '11px',
                          color: statusConfig.color,
                        }}
                      >
                        {formatDuration(task.duration)}
                      </Text>
                    </MetaItem>
                  </HistoryMeta>
                </div>
              }
            />
          </List.Item>
        </TaskItemWrapper>
      );
    },
    [getStatusConfig, formatTime, formatDuration, handleViewDetail, t],
  );

  return (
    <Container
      title="history"
      loading={loading}
      add={{
        icon: <ReloadOutlined />,
        items: [
          {
            key: 'refresh',
            text: t('refresh'),
          },
        ],
        callback: key => {
          handleRefresh();
        },
      }}
    >
      <ListWrapper>
        <List
          dataSource={tasks}
          renderItem={renderItem}
          loadMore={
            hasMore ? (
              <LoadMore>
                <Button type="text" onClick={loadMore} loading={loading}>
                  {t('loadMore')}
                </Button>
              </LoadMore>
            ) : null
          }
          locale={{
            emptyText: t('empty'),
          }}
          style={{ padding: 0, border: 'none' }}
        />
        {loading && tasks.length === 0 && (
          <CenteredLoading>
            <Spin indicator={<LoadingOutlined spin />} />
          </CenteredLoading>
        )}
      </ListWrapper>

      {/* 详情模态框 */}
      <Modal
        title={t('detailTitle')}
        visible={!!detailTask}
        onCancel={handleCloseDetail}
        footer={[
          <Button
            key="copy"
            type="primary"
            icon={<CopyOutlined />}
            onClick={() => detailTask && handleCopyQuery(detailTask)}
          >
            {t('copy')}
          </Button>,
          <Button key="close" onClick={handleCloseDetail}>
            {t('close')}
          </Button>,
        ]}
        width={800}
        style={{ maxWidth: '90vw' }}
      >
        {detailTask && (
          <div>
            <div style={{ marginBottom: SPACE_MD }}>
              <Tag
                color={getStatusConfig(detailTask.status).color}
                style={{ marginRight: SPACE_XS }}
              >
                {getStatusConfig(detailTask.status).text}
              </Tag>
              <Text type="secondary" style={{ fontSize: '13px' }}>
                {t('submitTime')}: {formatTime(detailTask.submitTime)}
              </Text>
            </div>
            <div style={{ marginBottom: SPACE_MD }}>
              <h4
                style={{
                  margin: '0 0 8px 0',
                  fontSize: '14px',
                  fontWeight: 'bold',
                }}
              >
                {t('query')}:
              </h4>
              <PreFormattedText>{detailTask.query}</PreFormattedText>
            </div>
            <div style={{ fontSize: '13px' }}>
              <p style={{ margin: '0 0 4px 0' }}>
                <Text type="secondary">{t('startTime')}:</Text>
                <Text style={{ marginLeft: '8px' }}>
                  {formatTime(detailTask.startTime)}
                </Text>
              </p>
              <p style={{ margin: '0 0 4px 0' }}>
                <Text type="secondary">{t('endTime')}:</Text>
                <Text style={{ marginLeft: '8px' }}>
                  {formatTime(detailTask.endTime)}
                </Text>
              </p>
              <p style={{ margin: '0 0 4px 0' }}>
                <Text type="secondary">{t('duration')}:</Text>
                <Text
                  style={{
                    marginLeft: '8px',
                    color: getStatusConfig(detailTask.status).color,
                  }}
                >
                  {formatDuration(detailTask.duration)}
                </Text>
              </p>
            </div>
            {detailTask.status === 'FAILED' && (
              <div style={{ marginTop: SPACE_MD }}>
                <h4
                  style={{
                    margin: '0 0 8px 0',
                    fontSize: '14px',
                    fontWeight: 'bold',
                  }}
                >
                  {t('failMessage')}:
                </h4>
                <p>{detailTask.failType}</p>
                <p>{detailTask.errorMessage}</p>
              </div>
            )}
          </div>
        )}
      </Modal>
    </Container>
  );
});

const TaskItemWrapper = styled.div`
  padding: ${SPACE_XS} ${SPACE_MD};
  margin-bottom: ${SPACE_XS};
  background-color: ${p => p.theme.bodyBackground};
  border: 1px solid ${p => p.theme.borderColorSplit};
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
`;

const HistoryMeta = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: ${SPACE};
  margin-top: ${SPACE_XS};
  font-size: 12px;
  line-height: 1.2;
`;

const MetaItem = styled.div`
  display: flex;
  align-items: center;
  margin: 2px 0;
`;

const ListWrapper = styled.div`
  flex: 1;
  padding: ${SPACE_XS};
  overflow-y: auto;
`;

const LoadMore = styled.div`
  padding: ${SPACE_MD} 0;
  text-align: center;
`;

const CenteredLoading = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
`;

const PreFormattedText = styled.pre`
  padding: ${SPACE_XS};
  margin: 0;
  font-family: monospace;
  font-size: 12px;
  word-break: break-all;
  white-space: pre-wrap;
  background: #f5f5f5;
  border-radius: 4px;
`;
