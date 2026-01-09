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

import { InfoCircleOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { Button, Card, message, Space } from 'antd';
import useI18NPrefix from 'app/hooks/useI18NPrefix';
import React, { memo } from 'react';
import styled from 'styled-components/macro';

interface CacheDialogProps {
  visible: boolean;
  onCancel: () => void;
  onUseLocalCache?: () => void;
  onUseRemoteData?: () => void;
  onSave?: () => void;
  cacheConflict?: boolean;
  cacheExpired?: boolean;
  cacheUpdatedAt?: number;
  remoteUpdatedAt?: number;
}

export const CacheDialog = memo(
  ({
    visible,
    onCancel,
    onUseLocalCache,
    onUseRemoteData,
    onSave,
    cacheConflict = false,
    cacheExpired = false,
    cacheUpdatedAt,
    remoteUpdatedAt,
  }: CacheDialogProps) => {
    const t = useI18NPrefix('view.cacheDialog');

    const formatTime = (timestamp?: number) => {
      if (!timestamp) return '-';
      const date = new Date(timestamp);
      return date.toLocaleString();
    };

    const handleUseLocalCache = () => {
      onUseLocalCache?.();
      message.success(t('useLocalCacheSuccess'));
      onCancel();
    };

    const handleUseRemoteData = () => {
      onUseRemoteData?.();
      message.success(t('useRemoteDataSuccess'));
      onCancel();
    };

    const handleSave = () => {
      onSave?.();
      onCancel();
    };

    if (!visible) return null;

    return (
      <Overlay>
        <DialogWrapper>
          <Card
            title={
              cacheConflict ? (
                <Title>
                  <InfoCircleOutlined /> {t('conflictTitle')}
                </Title>
              ) : (
                <Title>
                  <QuestionCircleOutlined /> {t('expiredTitle')}
                </Title>
              )
            }
            bordered={false}
          >
            {cacheConflict ? (
              <Content>
                <p>{t('conflictDescription')}</p>
                <TimeInfo>
                  <div>
                    <strong>{t('localCache')}:</strong>
                    {formatTime(cacheUpdatedAt)}
                  </div>
                  <div>
                    <strong>{t('remoteData')}:</strong>
                    {formatTime(remoteUpdatedAt)}
                  </div>
                </TimeInfo>
                <ActionButtons>
                  <Button type="primary" onClick={handleUseLocalCache}>
                    {t('useLocalCache')}
                  </Button>
                  <Button onClick={handleUseRemoteData}>
                    {t('useRemoteData')}
                  </Button>
                  <Button onClick={onCancel}>{t('cancel')}</Button>
                </ActionButtons>
              </Content>
            ) : (
              <Content>
                <p>{t('expiredDescription')}</p>
                <ActionButtons>
                  <Button type="primary" onClick={handleSave}>
                    {t('save')}
                  </Button>
                  <Button onClick={onCancel}>{t('cancel')}</Button>
                </ActionButtons>
              </Content>
            )}
          </Card>
        </DialogWrapper>
      </Overlay>
    );
  },
);

const Overlay = styled.div`
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.5);
`;

const DialogWrapper = styled.div`
  width: 480px;
  max-width: 90vw;
`;

const Title = styled.div`
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 16px;
`;

const Content = styled.div`
  padding: 16px 0;
`;

const TimeInfo = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  margin: 16px 0;
  background-color: #f5f5f5;
  border-radius: 4px;
`;

const ActionButtons = styled(Space)`
  display: flex;
  justify-content: flex-end;
  width: 100%;
  margin-top: 24px;
`;
