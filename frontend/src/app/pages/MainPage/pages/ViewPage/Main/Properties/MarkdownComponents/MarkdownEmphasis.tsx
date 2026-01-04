import styled from 'styled-components/macro';

interface MarkdownEmphasisProps {
  strong?: boolean;
  children: React.ReactNode;
}

const Emphasis = styled(({ strong, ...props }: MarkdownEmphasisProps) => {
  const Tag = strong ? 'strong' : 'em';
  return <Tag {...props} />;
})<{ strong?: boolean }>`
  ${props =>
    props.strong &&
    `
    font-weight: 600;
    color: #3F4254;
  `}

  ${props =>
    !props.strong &&
    `
    font-style: italic;
    color: #3F4254;
  `}
`;

export const MarkdownEmphasis = Emphasis;
