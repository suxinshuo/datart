import styled from 'styled-components/macro';

interface MarkdownListProps {
  ordered: boolean;
  children: React.ReactNode;
}

const ListContainer = styled(({ ordered, ...props }: MarkdownListProps) => {
  const Tag = ordered ? 'ol' : 'ul';
  return <Tag {...props} />;
})`
  padding-left: 20px;
  margin: 10px 0;

  ul,
  ol {
    padding-left: 20px;
    margin: 4px 0;
  }
`;

const ListItem = styled.li`
  margin: 4px 0;
  line-height: 1.6;
  color: #3f4254;

  &::marker {
    font-weight: 400;
    color: #7e8299;
  }

  ol > li::marker {
    font-weight: 500;
  }
`;

export const MarkdownList = Object.assign(ListContainer, {
  Item: ListItem,
});
