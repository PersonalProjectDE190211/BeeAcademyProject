import katex from 'katex';
import 'katex/dist/katex.min.css';

interface LatexRendererProps {
  content: string;
  className?: string;
}

/**
 * Hiển thị văn bản có nhúng công thức toán học LaTeX ($...$ hoặc $$...$$) bằng KaTeX.
 */
export function LatexRenderer({ content, className }: LatexRendererProps) {
  if (!content) return null;

  const segments = content.split(/(\$\$[\s\S]+?\$\$|\$[^$\n]+?\$)/g);

  return (
    <span className={className}>
      {segments.map((segment, index) => {
        if (segment.startsWith('$$') && segment.endsWith('$$') && segment.length > 4) {
          const formula = segment.slice(2, -2);
          const html = katex.renderToString(formula, {
            displayMode: true,
            throwOnError: false,
          });
          return (
            <span
              key={index}
              className="my-2 block overflow-x-auto"
              dangerouslySetInnerHTML={{ __html: html }}
            />
          );
        }
        if (segment.startsWith('$') && segment.endsWith('$') && segment.length > 2) {
          const formula = segment.slice(1, -1);
          const html = katex.renderToString(formula, {
            displayMode: false,
            throwOnError: false,
          });
          return <span key={index} dangerouslySetInnerHTML={{ __html: html }} />;
        }
        return <span key={index}>{segment}</span>;
      })}
    </span>
  );
}
