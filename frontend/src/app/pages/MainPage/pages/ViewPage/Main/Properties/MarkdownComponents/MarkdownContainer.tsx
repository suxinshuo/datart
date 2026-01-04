import styled from 'styled-components/macro';

export const MarkdownContainer = styled.div`
  width: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
    'Helvetica Neue', Arial, sans-serif;
  font-size: 14px;
  line-height: 1.6;
  color: #3f4254;
  word-wrap: break-word;
  overflow-wrap: break-word;

  /* 响应式设计 */
  @media (max-width: 768px) {
    font-size: 13px;
  }
`;
