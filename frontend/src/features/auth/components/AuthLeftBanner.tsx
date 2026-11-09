import React from 'react';
import { Link } from 'react-router-dom';

interface AuthLeftBannerProps {
  title?: string;
  subtitle?: string;
  showChecklist?: boolean;
  showProgressCard?: boolean;
}

export const AuthLeftBanner: React.FC<AuthLeftBannerProps> = ({
  title = 'Học tập vui hơn,\ntiến bộ mỗi ngày',
  subtitle = 'Bài giảng sinh động, bài kiểm tra có kết quả ngay và theo dõi tiến độ dành cho học sinh và phụ huynh.',
  showChecklist = true,
  showProgressCard = true,
}) => {
  return (
    <aside className="auth-side relative overflow-hidden flex flex-col justify-between p-8 sm:p-12 lg:px-16 lg:py-14 text-white min-h-[380px] lg:min-h-screen select-none">
      {/* 3 Floating Honeycomb Hexagons */}
      <svg
        className="auth-hex"
        style={{ width: 120, height: 120, top: '8%', right: '10%', opacity: 0.14 }}
        viewBox="0 0 100 100"
        aria-hidden="true"
      >
        <polygon points="50,3 93,27 93,73 50,97 7,73 7,27" fill="currentColor" />
      </svg>
      <svg
        className="auth-hex auth-hex--slow"
        style={{ width: 70, height: 70, top: '46%', right: '4%', opacity: 0.22 }}
        viewBox="0 0 100 100"
        aria-hidden="true"
      >
        <polygon points="50,3 93,27 93,73 50,97 7,73 7,27" fill="currentColor" />
      </svg>
      <svg
        className="auth-hex auth-hex--slower"
        style={{ width: 160, height: 160, bottom: '12%', right: '30%', opacity: 0.1 }}
        viewBox="0 0 100 100"
        aria-hidden="true"
      >
        <polygon points="50,3 93,27 93,73 50,97 7,73 7,27" fill="currentColor" />
      </svg>

      {/* Top: Brand Identity */}
      <Link
        to="/courses"
        className="auth-rise relative z-10 flex items-center gap-3 w-fit group"
        style={{ '--rise-delay': '100ms' } as React.CSSProperties}
      >
        <div className="flex h-11 w-11 items-center justify-center rounded-full bg-white text-brand-700 font-bold text-xl shadow-[0_6px_18px_rgba(0,0,0,0.12)] transition-transform duration-200 group-hover:scale-105">
          B
        </div>
        <span className="text-[22px] font-bold tracking-tight text-white">
          BeeAcademy
        </span>
      </Link>

      {/* Middle: Headline, Checklist & Floating Glassmorphic Progress Card */}
      <div className="relative z-10 my-auto py-8 lg:py-0 max-w-[460px]">
        <h1
          className="auth-rise text-3xl sm:text-4xl xl:text-[44px] font-bold leading-[1.15] text-white tracking-tight whitespace-pre-line mb-4"
          style={{ '--rise-delay': '250ms' } as React.CSSProperties}
        >
          {title}
        </h1>
        <p
          className="auth-rise text-[16px] sm:text-[17px] leading-[1.6] text-on-brand-muted max-w-[440px]"
          style={{ '--rise-delay': '400ms' } as React.CSSProperties}
        >
          {subtitle}
        </p>

        {/* Feature Checklist */}
        {showChecklist && (
          <div
            className="auth-rise mt-6 space-y-2.5 text-[15px] text-white/95 font-medium"
            style={{ '--rise-delay': '550ms' } as React.CSSProperties}
          >
            <div className="flex items-center gap-3">
              <span className="inline-flex h-5 w-5 items-center justify-center rounded-full bg-white/20 text-xs font-bold text-white">
                ✓
              </span>
              <span>Khóa học bám sát chương trình K–12</span>
            </div>
            <div className="flex items-center gap-3">
              <span className="inline-flex h-5 w-5 items-center justify-center rounded-full bg-white/20 text-xs font-bold text-white">
                ✓
              </span>
              <span>Làm bài kiểm tra và xem kết quả ngay</span>
            </div>
            <div className="flex items-center gap-3">
              <span className="inline-flex h-5 w-5 items-center justify-center rounded-full bg-white/20 text-xs font-bold text-white">
                ✓
              </span>
              <span>Phụ huynh theo dõi tiến độ của con</span>
            </div>
          </div>
        )}

        {/* Animated Glassmorphic Weekly Progress Card */}
        {showProgressCard && (
          <div
            className="auth-card mt-7 rounded-[18px] border border-white/30 bg-white/16 px-5 py-[18px] max-w-[340px] backdrop-blur-md shadow-[0_12px_32px_rgba(90,22,0,0.18)]"
          >
            <div className="flex items-center justify-between text-[14px] mb-2.5">
              <span className="font-medium text-white/95">Tiến độ tuần này</span>
              <strong className="font-bold text-white">82%</strong>
            </div>
            <div className="h-2 w-full rounded-full bg-white/28 overflow-hidden">
              <div
                className="auth-bar h-full rounded-full bg-white"
                style={{ '--bar-to': '82%' } as React.CSSProperties}
              />
            </div>
            <div className="mt-2.5 text-[13px] text-on-brand-muted">
              Còn 2 bài học nữa để hoàn thành mục tiêu
            </div>
          </div>
        )}
      </div>

      {/* Bottom: Copyright */}
      <div
        className="auth-rise relative z-10 text-[13px] text-white/75 pt-4 lg:pt-0"
        style={{ '--rise-delay': '700ms' } as React.CSSProperties}
      >
        © BeeAcademy
      </div>
    </aside>
  );
};
