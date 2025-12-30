import styled from 'styled-components/macro';

interface MarkdownHeadingProps {
  level: 1 | 2 | 3 | 4 | 5 | 6;
  children: React.ReactNode;
}

const Heading = styled(({ level, ...props }: MarkdownHeadingProps) => {
  const Tag = `h${level}` as keyof JSX.IntrinsicElements;
  return <Tag {...props} />;
})<{ level: 1 | 2 | 3 | 4 | 5 | 6 }>`
  margin: 20px 0 12px 0;
  font-weight: 600;
  line-height: 1.4;
  color: #3f4254;

  ${props =>
    props.level === 1 &&
    `
    font-size: 24px;
    margin-top: 24px;
  `}

  ${props =>
    props.level === 2 &&
    `
    font-size: 20px;
  `}
  
  ${props =>
    props.level === 3 &&
    `
    font-size: 18px;
  `}
  
  ${props =>
    props.level === 4 &&
    `
    font-size: 16px;
  `}
  
  ${props =>
    props.level === 5 &&
    `
    font-size: 14px;
  `}
  
  ${props =>
    props.level === 6 &&
    `
    font-size: 13px;
    color: #7E8299;
  `}
  
  /* 响应式设计 */
  @media (max-width: 768px) {
    ${props =>
      props.level === 1 &&
      `
      font-size: 22px;
    `}

    ${props =>
      props.level === 2 &&
      `
      font-size: 18px;
    `}
    
    ${props =>
      props.level === 3 &&
      `
      font-size: 16px;
    `}
  }
`;

export const MarkdownHeading = Heading;
