import { CheckOutlined, CopyOutlined } from '@ant-design/icons';
import copy from 'copy-to-clipboard';
import { useState } from 'react';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { tomorrow } from 'react-syntax-highlighter/dist/esm/styles/prism';
import styled from 'styled-components/macro';

interface MarkdownCodeProps {
  inline: boolean;
  className?: string;
  children: React.ReactNode;
}

const CodeContainer = styled.div`
  position: relative;
  margin: 12px 0;
  overflow: hidden;
  background-color: #f5f8fa;
  border-radius: 4px;
`;

const InlineCode = styled.code`
  padding: 2px 6px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier,
    monospace;
  font-size: 0.9em;
  color: #e62412;
  word-break: break-word;
  background-color: #f5f8fa;
  border-radius: 3px;
`;

const CodeContent = styled(SyntaxHighlighter)`
  margin: 0 !important;
  font-size: 13px;
  line-height: 1.5;
  border-radius: 4px;
`;

const CopyButton = styled.button<{ copied: boolean }>`
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 4px;
  align-items: center;
  padding: 4px 8px;
  font-size: 12px;
  color: white;
  cursor: pointer;
  background-color: rgba(0, 0, 0, 0.5);
  border: none;
  border-radius: 3px;
  opacity: 0.7;
  transition: all 0.2s ease;

  &:hover {
    background-color: rgba(0, 0, 0, 0.7);
    opacity: 1;
  }

  ${props =>
    props.copied &&
    `
    background-color: #15AD31;
    opacity: 1;
  `}
`;

const CodeLanguage = styled.div`
  position: absolute;
  top: 0;
  left: 0;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 500;
  color: white;
  background-color: #7e8299;
  border-radius: 0 0 4px 0;
`;

export const MarkdownCode = ({
  inline,
  className,
  children,
}: MarkdownCodeProps) => {
  const [copied, setCopied] = useState(false);
  const code = String(children).replace(/\n$/, '');
  const match = /language-(\w+)/.exec(className || '');
  const language = match?.[1] || 'text';

  if (inline) {
    return <InlineCode>{children}</InlineCode>;
  }

  const handleCopy = () => {
    copy(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <CodeContainer>
      <CodeLanguage>{language}</CodeLanguage>
      <CopyButton onClick={handleCopy} copied={copied}>
        {copied ? <CheckOutlined /> : <CopyOutlined />}
        {copied ? 'Copied' : 'Copy'}
      </CopyButton>
      <CodeContent style={tomorrow} language={language} PreTag="div">
        {code}
      </CodeContent>
    </CodeContainer>
  );
};
