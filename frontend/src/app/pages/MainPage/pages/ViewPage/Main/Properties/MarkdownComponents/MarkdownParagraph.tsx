import styled from 'styled-components/macro';

export const MarkdownParagraph = styled.p`
  margin: 10px 0;
  line-height: 1.6;
  color: #3f4254;

  &:first-child {
    margin-top: 0;
  }

  &:last-child {
    margin-bottom: 0;
  }
`;
