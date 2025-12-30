import { render, screen } from '@testing-library/react';
import { MarkdownRenderer } from './MarkdownRenderer';

describe('MarkdownRenderer', () => {
  it('renders paragraphs correctly', () => {
    const content = 'This is a paragraph.';
    render(<MarkdownRenderer content={content} />);
    expect(screen.getByText('This is a paragraph.')).toBeInTheDocument();
  });

  it('renders headings correctly', () => {
    const content = `# Heading 1
## Heading 2
### Heading 3`;
    render(<MarkdownRenderer content={content} />);
    expect(screen.getByText('Heading 1')).toBeInTheDocument();
    expect(screen.getByText('Heading 2')).toBeInTheDocument();
    expect(screen.getByText('Heading 3')).toBeInTheDocument();
  });

  it('renders lists correctly', () => {
    const content = `- Item 1
- Item 2
- Item 3

1. Item A
2. Item B
3. Item C`;
    render(<MarkdownRenderer content={content} />);
    expect(screen.getByText('Item 1')).toBeInTheDocument();
    expect(screen.getByText('Item 2')).toBeInTheDocument();
    expect(screen.getByText('Item 3')).toBeInTheDocument();
    expect(screen.getByText('Item A')).toBeInTheDocument();
    expect(screen.getByText('Item B')).toBeInTheDocument();
    expect(screen.getByText('Item C')).toBeInTheDocument();
  });

  it('renders code blocks correctly', () => {
    const content = '```javascript\nconsole.log("Hello, world!");\n```';
    render(<MarkdownRenderer content={content} />);
    expect(
      screen.getByText('console.log("Hello, world!");'),
    ).toBeInTheDocument();
  });

  it('renders links correctly', () => {
    const content = '[Google](https://www.google.com)';
    render(<MarkdownRenderer content={content} />);
    const link = screen.getByText('Google');
    expect(link).toBeInTheDocument();
    expect(link.closest('a')).toHaveAttribute('href', 'https://www.google.com');
  });

  it('renders images correctly', () => {
    const content = '![Alt text](https://example.com/image.jpg)';
    render(<MarkdownRenderer content={content} />);
    const img = screen.getByAltText('Alt text');
    expect(img).toBeInTheDocument();
    expect(img).toHaveAttribute('src', 'https://example.com/image.jpg');
  });

  it('renders blockquotes correctly', () => {
    const content = '> This is a blockquote.';
    render(<MarkdownRenderer content={content} />);
    expect(screen.getByText('This is a blockquote.')).toBeInTheDocument();
  });

  it('renders horizontal rules correctly', () => {
    const content = '---';
    render(<MarkdownRenderer content={content} />);
    expect(screen.getByRole('separator')).toBeInTheDocument();
  });

  it('renders emphasis correctly', () => {
    const content = `**bold** and *italic*`;
    render(<MarkdownRenderer content={content} />);
    expect(screen.getByText('bold')).toBeInTheDocument();
    expect(screen.getByText('italic')).toBeInTheDocument();
  });

  it('renders task lists correctly', () => {
    const content = `- [x] Completed task
- [ ] Incomplete task`;
    render(<MarkdownRenderer content={content} />);
    expect(screen.getByText('Completed task')).toBeInTheDocument();
    expect(screen.getByText('Incomplete task')).toBeInTheDocument();
  });

  it('renders tables correctly', () => {
    const content = `| Column 1 | Column 2 |
|----------|----------|
| Row 1    | Data 1   |
| Row 2    | Data 2   |`;
    render(<MarkdownRenderer content={content} />);
    expect(screen.getByText('Column 1')).toBeInTheDocument();
    expect(screen.getByText('Column 2')).toBeInTheDocument();
    expect(screen.getByText('Row 1')).toBeInTheDocument();
    expect(screen.getByText('Data 1')).toBeInTheDocument();
    expect(screen.getByText('Row 2')).toBeInTheDocument();
    expect(screen.getByText('Data 2')).toBeInTheDocument();
  });
});
