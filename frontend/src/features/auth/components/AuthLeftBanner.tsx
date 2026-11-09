import React from 'react';
import { Link } from 'react-router-dom';

interface AuthLeftBannerProps {
  title?: string;
  subtitle?: string;
  showChecklist?: boolean;
}

export const AuthLeftBanner: React.FC<AuthLeftBannerProps> = ({
  title = 'Học tập vui hơn,\ntiến bộ mỗi ngày',
  subtitle = 'Nền tảng học tập trực tuyến K–12 với bài giảng sinh động, bài kiểm tra có kết quả ngay và theo dõi tiến độ dành cho học sinh và phụ huynh.',
  showChecklist = true,
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

      {/* Middle: Headline, Subtitle, Checklist & Trust Card */}
      <div className="relative z-10 my-auto py-8 lg:py-0 max-w-[480px]">
        <h1
          className="auth-rise text-3xl sm:text-4xl xl:text-[46px] font-bold leading-[1.18] text-white tracking-tight whitespace-pre-line mb-5"
          style={{ '--rise-delay': '250ms' } as React.CSSProperties}
        >
          {title}
        </h1>
        <p
          className="auth-rise text-[16px] sm:text-[17px] leading-[1.65] text-on-brand-muted max-w-[440px]"
          style={{ '--rise-delay': '400ms' } as React.CSSProperties}
        >
          {subtitle}
        </p>

        {/* Feature Checklist */}
        {showChecklist && (
          <div
            className="auth-rise mt-8 space-y-4 text-[15px] sm:text-[16px] text-white/95 font-medium"
            style={{ '--rise-delay': '550ms' } as React.CSSProperties}
          >
            <div className="flex items-center gap-3.5">
              <span className="inline-flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-white/20 text-xs font-bold text-white shadow-xs">
                ✓
              </span>
              <span>Khóa học bám sát chương trình chuẩn K–12</span>
            </div>
            <div className="flex items-center gap-3.5">
              <span className="inline-flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-white/20 text-xs font-bold text-white shadow-xs">
                ✓
              </span>
              <span>Làm bài kiểm tra và xem kết quả phân tích ngay</span>
            </div>
            <div className="flex items-center gap-3.5">
              <span className="inline-flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-white/20 text-xs font-bold text-white shadow-xs">
                ✓
              </span>
              <span>Phụ huynh theo dõi sát sao lộ trình và tiến độ của con</span>
            </div>
          </div>
        )}

        {/* Social Proof / Trust Highlights for Unauthenticated Guests */}
        <div
          className="auth-rise mt-10 rounded-[18px] border border-white/25 bg-white/12 p-4 sm:p-5 backdrop-blur-md shadow-[0_12px_30px_rgba(90,22,0,0.15)] max-w-[420px]"
          style={{ '--rise-delay': '700ms' } as React.CSSProperties}
        >
          <div className="flex items-center gap-3.5">
            <div className="flex -space-x-2 shrink-0">
              <div className="flex h-9 w-9 items-center justify-center rounded-full border-2 border-white/80 bg-amber-200 text-xs font-bold text-brand-700 shadow-xs">
                B
              </div>
              <div className="flex h-9 w-9 items-center justify-center rounded-full border-2 border-white/80 bg-orange-200 text-xs font-bold text-brand-700 shadow-xs">
                A
              </div>
              <div className="flex h-9 w-9 items-center justify-center rounded-full border-2 border-white/80 bg-rose-200 text-xs font-bold text-brand-700 shadow-xs">
                K
              </div>
            </div>
            <div className="text-left">
              <div className="text-[14px] font-bold text-white">
                10.000+ Học sinh & Phụ huynh
              </div>
              <div className="text-[12px] text-on-brand-muted">
                Đồng hành rèn luyện và tiến bộ mỗi ngày cùng BeeAcademy
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom: Copyright & Policy */}
      <div
        className="auth-rise relative z-10 flex items-center justify-between text-[13px] text-white/75 pt-6 lg:pt-0"
        style={{ '--rise-delay': '850ms' } as React.CSSProperties}
      >
        <span>© BeeAcademy</span>
        <span className="text-white/60">Học tập thông minh • Tương lai vững vàng</span>
      </div>
    </aside>
  );
};
