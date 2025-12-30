import { memo } from 'react';
import ReactMarkdown from 'react-markdown';
import rehypeRaw from 'rehype-raw';
import remarkGfm from 'remark-gfm';
import { MarkdownBlockquote } from './MarkdownBlockquote';
import { MarkdownCode } from './MarkdownCode';
import { MarkdownContainer } from './MarkdownContainer';
import { MarkdownEmphasis } from './MarkdownEmphasis';
import { MarkdownHeading } from './MarkdownHeading';
import { MarkdownHorizontalRule } from './MarkdownHorizontalRule';
import { MarkdownImage } from './MarkdownImage';
import { MarkdownLink } from './MarkdownLink';
import { MarkdownList } from './MarkdownList';
import { MarkdownParagraph } from './MarkdownParagraph';
import { MarkdownTable } from './MarkdownTable';
import { MarkdownTaskList } from './MarkdownTaskList';

interface MarkdownRendererProps {
  content: string;
}

export const MarkdownRenderer = memo(({ content }: MarkdownRendererProps) => {
  return (
    <ReactMarkdown
      remarkPlugins={[remarkGfm]}
      rehypePlugins={[rehypeRaw]}
      components={{
        // 容器
        wrapper: ({ node, ...props }) => <MarkdownContainer {...props} />,
        // 标题
        h1: ({ node, ...props }) => <MarkdownHeading level={1} {...props} />,
        h2: ({ node, ...props }) => <MarkdownHeading level={2} {...props} />,
        h3: ({ node, ...props }) => <MarkdownHeading level={3} {...props} />,
        h4: ({ node, ...props }) => <MarkdownHeading level={4} {...props} />,
        h5: ({ node, ...props }) => <MarkdownHeading level={5} {...props} />,
        h6: ({ node, ...props }) => <MarkdownHeading level={6} {...props} />,
        // 段落
        p: ({ node, ...props }) => <MarkdownParagraph {...props} />,
        // 列表
        ul: ({ node, ...props }) => <MarkdownList ordered={false} {...props} />,
        ol: ({ node, ...props }) => <MarkdownList ordered={true} {...props} />,
        li: ({ node, ...props }) => {
          // 检查是否为任务列表项
          if (node?.children?.[0]?.type === 'checkbox') {
            return <MarkdownTaskList.Item {...props} />;
          }
          return <MarkdownList.Item {...props} />;
        },
        // 任务列表
        input: ({ node, ...props }) => {
          if (props.type === 'checkbox') {
            return <MarkdownTaskList.Checkbox {...props} />;
          }
          return <input {...props} />;
        },
        // 代码
        code: ({ node, inline, className, children, ...props }) => (
          <MarkdownCode
            inline={inline}
            className={className}
            children={children}
            {...props}
          />
        ),
        // 表格
        table: ({ node, ...props }) => <MarkdownTable {...props} />,
        thead: ({ node, ...props }) => <MarkdownTable.Head {...props} />,
        tbody: ({ node, ...props }) => <MarkdownTable.Body {...props} />,
        tr: ({ node, ...props }) => <MarkdownTable.Row {...props} />,
        th: ({ node, ...props }) => <MarkdownTable.HeaderCell {...props} />,
        td: ({ node, ...props }) => <MarkdownTable.Cell {...props} />,
        // 链接
        a: ({ node, ...props }) => <MarkdownLink {...props} />,
        // 图片
        img: ({ node, ...props }) => <MarkdownImage {...props} />,
        // 引用
        blockquote: ({ node, ...props }) => <MarkdownBlockquote {...props} />,
        // 分割线
        hr: ({ node, ...props }) => <MarkdownHorizontalRule {...props} />,
        // 强调
        strong: ({ node, ...props }) => <MarkdownEmphasis strong {...props} />,
        em: ({ node, ...props }) => <MarkdownEmphasis {...props} />,
      }}
    >
      {content}
    </ReactMarkdown>
  );
});

MarkdownRenderer.displayName = 'MarkdownRenderer';
