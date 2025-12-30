import styled from 'styled-components/macro';

const Blockquote = styled.blockquote`
  padding: 10px 16px;
  margin: 12px 0;
  font-style: italic;
  line-height: 1.6;
  color: #3f4254;
  background-color: #f5f8fa;
  border-left: 4px solid #1b9aee;

  p {
    margin: 0;
  }

  blockquote {
    margin: 8px 0 0 0;
  }
`;

export const MarkdownBlockquote = ({
  children,
}: {
  children: React.ReactNode;
}) => {
  return <Blockquote>{children}</Blockquote>;
};
