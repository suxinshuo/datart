import styled from 'styled-components/macro';

const HorizontalRule = styled.hr`
  margin: 20px 0;
  background-color: transparent;
  border: none;
  border-top: 1px solid #e4e6ef;
`;

export const MarkdownHorizontalRule = () => {
  return <HorizontalRule />;
};
