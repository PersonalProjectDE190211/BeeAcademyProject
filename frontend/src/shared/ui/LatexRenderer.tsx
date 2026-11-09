import React from 'react';
import katex from 'katex';
import 'katex/dist/katex.min.css';
import { ArrowRight, BookOpen, ExternalLink } from 'lucide-react';

interface LatexRendererProps {
  content: string;
  className?: string;
  onNavigate?: (url: string) => void;
}

/**
 * Phân tích và hiển thị văn bản kết hợp Markdown (link, bold) và công thức LaTeX KaTeX.
 * Các liên kết tới khóa học (/courses/...) được định dạng thành nút liên kết trực quan.
 */
export function LatexRenderer({ content, className, onNavigate }: LatexRendererProps) {
  if (!content) return null;

  // Tách văn bản thành các đoạn LaTeX ($$...$$ hoặc $...$) và đoạn văn bản thường
  const segments = content.split(/(\$\$[\s\S]+?\$\$|\$[^$\n]+?\$)/g);

  return (
    <span className={className}>
      {segments.map((segment, index) => {
        // Display math ($$...$$)
        if (segment.startsWith('$$') && segment.endsWith('$$') && segment.length > 4) {
          const formula = segment.slice(2, -2);
          const html = katex.renderToString(formula, {
            displayMode: true,
            throwOnError: false,
          });
          return (
            <span
              key={index}
              className="my-2.5 block overflow-x-auto text-center"
              dangerouslySetInnerHTML={{ __html: html }}
            />
          );
        }

        // Inline math ($...$)
        if (segment.startsWith('$') && segment.endsWith('$') && segment.length > 2) {
          const formula = segment.slice(1, -1);
          const html = katex.renderToString(formula, {
            displayMode: false,
            throwOnError: false,
          });
          return <span key={index} dangerouslySetInnerHTML={{ __html: html }} />;
        }

        // Đoạn văn bản thường: Hỗ trợ Markdown Links, Bold và ngắt dòng
        return <TextSegmentRenderer key={index} text={segment} onNavigate={onNavigate} />;
      })}
    </span>
  );
}

interface TextSegmentRendererProps {
  text: string;
  onNavigate?: (url: string) => void;
}

function TextSegmentRenderer({ text, onNavigate }: TextSegmentRendererProps) {
  // Regex tách liên kết markdown [Title](url), chữ in đậm **text**, và ngắt dòng \n
  const tokens = text.split(/(\[[^\]]+\]\([^)]+\)|\*\*[^*]+\*\*|\n)/g);

  return (
    <>
      {tokens.map((token, idx) => {
        if (!token) return null;

        // Xử lý ngắt dòng
        if (token === '\n') {
          return <br key={idx} />;
        }

        // Xử lý chữ in đậm: **text**
        if (token.startsWith('**') && token.endsWith('**') && token.length > 4) {
          const boldText = token.slice(2, -2);
          return (
            <strong key={idx} className="font-semibold text-slate-900">
              {boldText}
            </strong>
          );
        }

        // Xử lý liên kết Markdown: [label](url)
        const linkMatch = token.match(/^\[([^\]]+)\]\(([^)]+)\)$/);
        if (linkMatch) {
          const label = linkMatch[1];
          const url = linkMatch[2];

          // Nếu là đường dẫn nội bộ khóa học (/courses/...)
          if (url.startsWith('/courses/')) {
            return (
              <a
                key={idx}
                href={url}
                onClick={(e) => {
                  if (onNavigate) {
                    e.preventDefault();
                    onNavigate(url);
                  }
                }}
                className="my-1.5 inline-flex items-center gap-1.5 rounded-lg border border-amber-300 bg-amber-50/80 px-3 py-1.5 text-xs font-semibold text-amber-900 shadow-xs transition hover:border-amber-400 hover:bg-amber-100 hover:text-amber-950 no-underline"
              >
                <BookOpen className="h-3.5 w-3.5 text-amber-700" />
                <span>{label}</span>
                <ArrowRight className="h-3 w-3 text-amber-700" />
              </a>
            );
          }

          // Liên kết thông thường khác
          return (
            <a
              key={idx}
              href={url}
              target={url.startsWith('http') ? '_blank' : undefined}
              rel={url.startsWith('http') ? 'noopener noreferrer' : undefined}
              className="inline-flex items-center gap-0.5 text-amber-700 underline hover:text-amber-900 font-medium"
            >
              <span>{label}</span>
              <ExternalLink className="h-3 w-3 inline" />
            </a>
          );
        }

        // Văn bản thường
        return <React.Fragment key={idx}>{token}</React.Fragment>;
      })}
    </>
  );
}
