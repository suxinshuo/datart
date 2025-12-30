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

import { LoadingOutlined, SendOutlined } from '@ant-design/icons';
import { Button, Input, Select } from 'antd';
import { init } from 'echarts';
import { BASE_API_URL } from 'globalConstants';
import { memo, useCallback, useEffect, useRef, useState } from 'react';
import styled from 'styled-components/macro';
import {
  BORDER_RADIUS,
  FONT_SIZE_BODY,
  SPACE_MD,
  SPACE_SM,
  SPACE_XS,
} from 'styles/StyleConstants';
import { getToken } from 'utils/auth';
import useI18NPrefix from '../../../../../../hooks/useI18NPrefix';
import Container from './Container';
import { MarkdownRenderer } from './MarkdownComponents/MarkdownRenderer';

const { TextArea } = Input;
const { Option } = Select;

// 消息类型定义
type MessageType = 'user' | 'assistant';
type MessageStatus = 'sending' | 'sent' | 'error';
type QuestionType = 'function' | 'analysis' | 'other';

type MediaType = 'markdown' | 'chart';

interface MediaContent {
  type: MediaType;
  content: string;
  chartType?: 'line' | 'bar' | 'pie';
  chartData?: any;
  isComplete?: boolean;
}

interface Message {
  id: string;
  type: MessageType;
  content: MediaContent[];
  questionType: QuestionType;
  status: MessageStatus;
  timestamp: number;
}

// 本地存储键名
const STORAGE_KEY = 'sql_assistant_conversations';
const THREE_DAYS_IN_MS = 3 * 24 * 60 * 60 * 1000;

interface ConversationStorage {
  conversationId: string;
  messages: Message[];
}

const cleanupOldMessages = (messages: Message[]): Message[] => {
  const now = Date.now();
  const threeDaysAgo = now - THREE_DAYS_IN_MS;
  return messages.filter(message => message.timestamp >= threeDaysAgo);
};

const saveToStorage = (conversationId: string, messages: Message[]): void => {
  try {
    const storageData: ConversationStorage = {
      conversationId,
      messages: cleanupOldMessages(messages),
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(storageData));
  } catch (error) {
    console.error('Failed to save to storage:', error);
  }
};

const loadFromStorage = (): { conversationId: string; messages: Message[] } => {
  try {
    const savedData = localStorage.getItem(STORAGE_KEY);
    if (savedData) {
      const parsedData: ConversationStorage = JSON.parse(savedData);
      const cleanedMessages = cleanupOldMessages(parsedData.messages);

      return {
        conversationId: parsedData.conversationId || '',
        messages: cleanedMessages,
      };
    }
  } catch (error) {
    console.error('Failed to load from storage:', error);
  }

  return {
    conversationId: '',
    messages: [],
  };
};

const parseContentToMedia = (content: string): MediaContent[] => {
  const mediaContent: MediaContent[] = [];

  const codeBlockRegex = /```(\w+)?\n([\s\S]*?)```/g;
  let lastIndex = 0;
  let match;

  while ((match = codeBlockRegex.exec(content)) !== null) {
    const [fullMatch, lang, code] = match;
    const matchIndex = match.index;

    if (matchIndex > lastIndex) {
      const textBefore = content.substring(lastIndex, matchIndex);
      if (textBefore.trim()) {
        mediaContent.push({
          type: 'markdown',
          content: textBefore,
          isComplete: true,
        });
      }
    }

    if (lang === 'chart') {
      try {
        const chartConfig = JSON.parse(code);
        mediaContent.push({
          type: 'chart',
          content: chartConfig.title || '',
          chartType: chartConfig.chartType || 'line',
          chartData: chartConfig.chartData,
          isComplete: true,
        });
      } catch (e) {
        mediaContent.push({
          type: 'markdown',
          content: fullMatch,
          isComplete: true,
        });
      }
    } else {
      mediaContent.push({
        type: 'markdown',
        content: fullMatch,
        isComplete: true,
      });
    }

    lastIndex = matchIndex + fullMatch.length;
  }

  if (lastIndex < content.length) {
    const textAfter = content.substring(lastIndex);
    if (textAfter.trim()) {
      mediaContent.push({
        type: 'markdown',
        content: textAfter,
        isComplete: true,
      });
    }
  }

  if (mediaContent.length === 0 && content.trim()) {
    mediaContent.push({
      type: 'markdown',
      content: content,
      isComplete: true,
    });
  }

  return mediaContent;
};

const SQLAssistantContainer = styled.div`
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 90%;
`;

const ChatContainer = styled.div`
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: ${SPACE_MD};
  padding: ${SPACE_MD};
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: ${p => p.theme.borderColorSplit};
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: ${p => p.theme.borderColorSplit};
    border-radius: 3px;
  }
`;

const MessageWrapper = styled.div<{ $isUser: boolean }>`
  display: flex;
  justify-content: ${props => (props.$isUser ? 'flex-end' : 'flex-start')};
`;

const MessageBubble = styled.div<{ $isUser: boolean }>`
  max-width: 85%;
  padding: ${SPACE_SM};
  background-color: ${props => (props.$isUser ? '#EFF2F5' : '#FFFFFF')};
  border: 1px solid ${p => p.theme.borderColorSplit};
  border-radius: ${BORDER_RADIUS};
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
`;

const MessageHeader = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: ${SPACE_XS};
  font-size: 12px;
  color: ${p => p.theme.textColorSnd};
`;

const MessageTypeLabel = styled.span<{ $type: QuestionType }>`
  padding: 2px 8px;
  font-size: 10px;
  font-weight: 500;
  color: ${props => {
    switch (props.$type) {
      case 'function':
        return '#1890FF';
      case 'analysis':
        return '#52C41A';
      case 'other':
        return '#FA8C16';
      default:
        return '#8C8C8C';
    }
  }};
  background-color: ${props => {
    switch (props.$type) {
      case 'function':
        return '#E6F7FF';
      case 'analysis':
        return '#F6FFED';
      case 'other':
        return '#FFF7E6';
      default:
        return '#F5F5F5';
    }
  }};
  border-radius: 10px;
`;

const MediaContentWrapper = styled.div`
  margin-bottom: ${SPACE_XS};

  &:last-child {
    margin-bottom: 0;
  }
`;

const ChartContainer = styled.div`
  width: 100%;
  height: 300px;
  padding: ${SPACE_SM};
  background-color: #ffffff;
  border: 1px solid ${p => p.theme.borderColorSplit};
  border-radius: ${BORDER_RADIUS};
`;

const ChartWrapper = styled.div`
  width: 100%;
  height: 100%;
`;

const InputContainer = styled.div`
  padding: ${SPACE_MD};
  background-color: ${p => p.theme.componentBackground};
  border-top: 1px solid ${p => p.theme.borderColorSplit};
`;

const InputWrapper = styled.div`
  display: flex;
  gap: ${SPACE_SM};
  margin-bottom: ${SPACE_SM};
`;

const QuestionTypeSelect = styled(Select)`
  width: 120px;
`;

const MessageInput = styled(TextArea)`
  flex: 1;
  min-height: 80px;
  max-height: 200px;
  resize: vertical;
  border-radius: ${BORDER_RADIUS};

  & .ant-input {
    font-size: ${FONT_SIZE_BODY};
  }
`;

const SendButtonWrapper = styled.div`
  display: flex;
  justify-content: flex-end;
`;

const SendButton = styled(Button)`
  display: flex;
  gap: ${SPACE_XS};
  align-items: center;
`;

const LoadingIndicator = styled.div`
  display: flex;
  gap: ${SPACE_XS};
  align-items: center;
  font-size: 12px;
  color: ${p => p.theme.textColorSnd};
`;

// 主组件
export const SQLAssistant = memo(() => {
  const t = useI18NPrefix('view.sqlAssistant');

  const [messages, setMessages] = useState<Message[]>([]);
  const [conversationId, setConversationId] = useState<string>('');
  const [inputValue, setInputValue] = useState('');
  const [selectedQuestionType, setSelectedQuestionType] =
    useState<QuestionType>('function');
  const [isSending, setIsSending] = useState(false);

  // 问题类型选项
  const QUESTION_TYPES = [
    { value: 'function', label: t('function') },
    { value: 'analysis', label: t('analysis') },
    { value: 'other', label: t('other') },
  ];

  // 引用
  const chatContainerRef = useRef<HTMLDivElement>(null);
  const chatEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<any>(null);

  // 图表组件
  const ChartComponent = memo(
    ({
      chartType,
      chartData,
    }: {
      chartType: 'line' | 'bar' | 'pie';
      chartData: any;
    }) => {
      const chartRef = useRef<HTMLDivElement>(null);
      const chartInstanceRef = useRef<any>(null);

      useEffect(() => {
        if (!chartRef.current) return;

        // 初始化图表
        const chart = init(chartRef.current);
        chartInstanceRef.current = chart;

        // 根据图表类型设置不同的配置
        let option = {};

        switch (chartType) {
          case 'line':
            option = {
              tooltip: {
                trigger: 'axis',
              },
              xAxis: chartData?.xAxis || {},
              yAxis: chartData?.yAxis || {},
              series: chartData?.series || [],
            };
            break;
          case 'bar':
            option = {
              tooltip: {
                trigger: 'axis',
                axisPointer: {
                  type: 'shadow',
                },
              },
              xAxis: chartData?.xAxis || {},
              yAxis: chartData?.yAxis || {},
              series: chartData?.series || [],
            };
            break;
          case 'pie':
            option = {
              tooltip: {
                trigger: 'item',
              },
              legend: {
                orient: 'vertical',
                left: 'left',
              },
              series: chartData?.series || [],
            };
            break;
          default:
            option = {
              title: {
                text: t('chatTypeNotSupport'),
                left: 'center',
                top: 'center',
              },
            };
        }

        // 设置图表配置
        chart.setOption(option);

        // 使用 ResizeObserver 监听容器尺寸变化
        const resizeObserver = new ResizeObserver(() => {
          chart.resize();
        });

        if (chartRef.current) {
          resizeObserver.observe(chartRef.current);
        }

        // 多次延迟调整，确保在缓存加载时也能正确渲染
        const timers: NodeJS.Timeout[] = [];
        const delays = [0, 100, 300, 500];

        delays.forEach(delay => {
          const timer = setTimeout(() => {
            chart.resize();
          }, delay);
          timers.push(timer);
        });

        // 响应式处理
        const resizeHandler = () => {
          chart.resize();
        };
        window.addEventListener('resize', resizeHandler);

        // 清理函数
        return () => {
          timers.forEach(timer => clearTimeout(timer));
          resizeObserver.disconnect();
          window.removeEventListener('resize', resizeHandler);
          chart.dispose();
        };
      }, [chartType, chartData]);

      return <ChartWrapper ref={chartRef} />;
    },
  );

  useEffect(() => {
    const { conversationId, messages: loadedMessages } = loadFromStorage();
    setConversationId(conversationId);
    setMessages(loadedMessages);
  }, []);

  useEffect(() => {
    if (conversationId) {
      saveToStorage(conversationId, messages);
    }
  }, [messages, conversationId]);

  const scrollToBottom = useCallback(() => {
    if (chatContainerRef.current && chatContainerRef.current.scrollHeight > 0) {
      chatContainerRef.current.scrollTop =
        chatContainerRef.current.scrollHeight;
    }
  }, []);

  useEffect(() => {
    if (messages.length > 0) {
      scrollToBottom();

      const timer = setTimeout(() => {
        scrollToBottom();
      }, 100);

      return () => clearTimeout(timer);
    }
  }, [messages, scrollToBottom]);

  useEffect(() => {
    if (!chatContainerRef.current) return;

    const observer = new ResizeObserver(() => {
      if (messages.length > 0) {
        scrollToBottom();
      }
    });

    observer.observe(chatContainerRef.current);

    return () => {
      observer.disconnect();
    };
  }, [messages, scrollToBottom]);

  useEffect(() => {
    inputRef.current?.focus();
  }, []);

  // 实现 SSE 流式响应
  const sendMessageToServer = useCallback(
    (messageId: string, questionType: QuestionType, content: string) => {
      const url = `${BASE_API_URL}/sql-assistant/chat`;
      const token = getToken() || '';
      let accumulatedContent = '';

      fetch(url, {
        method: 'POST',
        headers: {
          Authorization: token,
          'Content-Type': 'application/json',
          'Cache-Control': 'no-cache',
        },
        body: JSON.stringify({
          conversationId,
          questionType,
          content,
        }),
      })
        .then(response => {
          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }

          const reader = response.body?.getReader();
          const decoder = new TextDecoder();

          if (!reader) {
            throw new Error('Response body is not readable');
          }

          const processStream = async () => {
            try {
              while (true) {
                const { done, value } = await reader.read();

                if (done) {
                  setMessages(prev => {
                    const updatedMessages = [...prev];
                    const assistantMessage = updatedMessages.find(
                      msg => msg.id === messageId,
                    );

                    if (assistantMessage) {
                      assistantMessage.status = 'sent';
                      assistantMessage.content.forEach(c => {
                        c.isComplete = true;
                      });
                    }

                    return updatedMessages;
                  });
                  setIsSending(false);
                  break;
                }

                const chunk = decoder.decode(value, { stream: true });
                const lines = chunk.split('\n');

                for (const line of lines) {
                  if (line.startsWith('data: ')) {
                    const lineBreakLine = line.replace(/\$line_break\$/g, '\n');
                    const data = lineBreakLine.slice(6);

                    if (data.startsWith('conversation_id: ')) {
                      const conversationId = data.slice(
                        'conversation_id: '.length,
                      );
                      setConversationId(conversationId);
                      continue;
                    }

                    accumulatedContent += data;
                    // 创建一个局部副本以避免闭包问题
                    const currentContent = accumulatedContent;

                    setMessages(prev => {
                      const updatedMessages = [...prev];
                      const assistantMessage = updatedMessages.find(
                        msg => msg.id === messageId,
                      );

                      if (!assistantMessage) return prev;

                      assistantMessage.content =
                        parseContentToMedia(currentContent);

                      return updatedMessages;
                    });
                  }
                }
              }
            } catch (error) {
              console.error('流处理错误:', error);
              setMessages(prev => {
                const updatedMessages = [...prev];
                const assistantMessage = updatedMessages.find(
                  msg => msg.id === messageId,
                );

                if (assistantMessage) {
                  assistantMessage.status = 'error';
                }

                return updatedMessages;
              });
              setIsSending(false);
            }
          };

          processStream();
        })
        .catch(error => {
          console.error('发送消息失败:', error);
          setMessages(prev => {
            const updatedMessages = [...prev];
            const assistantMessage = updatedMessages.find(
              msg => msg.id === messageId,
            );

            if (assistantMessage) {
              assistantMessage.status = 'error';
            }

            return updatedMessages;
          });
          setIsSending(false);
        });
    },
    [conversationId],
  );

  // 发送消息
  const handleSendMessage = useCallback(() => {
    if (!inputValue.trim() || isSending) return;

    const userMessage: Message = {
      id: `user-${Date.now()}`,
      type: 'user',
      content: [{ type: 'markdown', content: inputValue.trim() }],
      questionType: selectedQuestionType,
      status: 'sent',
      timestamp: Date.now(),
    };

    const assistantMessage: Message = {
      id: `assistant-${Date.now()}`,
      type: 'assistant',
      content: [],
      questionType: selectedQuestionType,
      status: 'sending',
      timestamp: Date.now(),
    };

    setMessages(prev => [...prev, userMessage, assistantMessage]);
    setInputValue('');
    setIsSending(true);

    // 调用后端 API
    sendMessageToServer(
      assistantMessage.id,
      selectedQuestionType,
      inputValue.trim(),
    );
  }, [inputValue, selectedQuestionType, isSending, sendMessageToServer]);

  // 处理键盘快捷键 (Ctrl+Enter)
  const handleKeyPress = useCallback(
    (e: React.KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
        handleSendMessage();
      }
    },
    [handleSendMessage],
  );

  // 格式化时间戳
  const formatTime = (timestamp: number) => {
    const date = new Date(timestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <Container title="sqlAssistant">
      <SQLAssistantContainer>
        <ChatContainer ref={chatContainerRef}>
          {messages.map(message => (
            <MessageWrapper key={message.id} $isUser={message.type === 'user'}>
              <MessageBubble $isUser={message.type === 'user'}>
                <MessageHeader>
                  <span>{formatTime(message.timestamp)}</span>
                  <MessageTypeLabel $type={message.questionType}>
                    {
                      QUESTION_TYPES.find(
                        type => type.value === message.questionType,
                      )?.label
                    }
                  </MessageTypeLabel>
                </MessageHeader>

                {message.content.map((media, index) => (
                  <MediaContentWrapper key={index}>
                    {media.type === 'markdown' && (
                      <MarkdownRenderer content={media.content} />
                    )}

                    {media.type === 'chart' && (
                      <ChartContainer>
                        <ChartComponent
                          chartType={media.chartType!}
                          chartData={media.chartData}
                        />
                      </ChartContainer>
                    )}
                  </MediaContentWrapper>
                ))}

                {message.status === 'sending' && (
                  <LoadingIndicator>
                    <LoadingOutlined spin size={12} />
                    <span>{t('replying')}</span>
                  </LoadingIndicator>
                )}

                {message.status === 'error' && (
                  <div
                    style={{
                      color: '#E62412',
                      fontSize: '12px',
                      marginTop: SPACE_XS,
                    }}
                  >
                    {t('replyFailed')}
                  </div>
                )}
              </MessageBubble>
            </MessageWrapper>
          ))}
          <div ref={chatEndRef} />
        </ChatContainer>

        <InputContainer>
          <InputWrapper>
            <QuestionTypeSelect
              value={selectedQuestionType}
              onChange={value => setSelectedQuestionType(value as QuestionType)}
            >
              {QUESTION_TYPES.map(type => (
                <Option key={type.value} value={type.value}>
                  {type.label}
                </Option>
              ))}
            </QuestionTypeSelect>
            <MessageInput
              ref={inputRef}
              value={inputValue}
              onChange={e => setInputValue(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder={t('inputQuestion')}
              autoSize={{ minRows: 1, maxRows: 6 }}
            />
          </InputWrapper>

          <SendButtonWrapper>
            <SendButton
              type="primary"
              icon={<SendOutlined />}
              onClick={handleSendMessage}
              loading={isSending}
            >
              {t('send')}
            </SendButton>
          </SendButtonWrapper>
        </InputContainer>
      </SQLAssistantContainer>
    </Container>
  );
});
