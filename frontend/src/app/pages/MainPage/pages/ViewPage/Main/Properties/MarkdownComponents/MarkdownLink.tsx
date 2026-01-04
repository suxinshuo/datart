import styled from 'styled-components/macro';

const Link = styled.a`
  color: #1b9aee;
  text-decoration: none;
  transition: all 0.2s ease;

  &:hover {
    color: #167ad8;
    text-decoration: underline;
  }

  &:active {
    color: #1263b4;
  }
`;

export const MarkdownLink = ({
  children,
  href,
  ...props
}: React.AnchorHTMLAttributes<HTMLAnchorElement>) => {
  // 检查是否为外部链接
  const isExternal =
    href && (href.startsWith('http') || href.startsWith('https'));

  return (
    <Link
      href={href}
      target={isExternal ? '_blank' : undefined}
      rel={isExternal ? 'noopener noreferrer' : undefined}
      {...props}
    >
      {children}
    </Link>
  );
};
