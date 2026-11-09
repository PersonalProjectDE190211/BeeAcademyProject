import React from 'react';
import { Link } from 'react-router-dom';

interface AuthLeftBannerProps {
  title?: string;
  subtitle?: string;
}

export const AuthLeftBanner: React.FC<AuthLeftBannerProps> = ({
  title = 'Học tập vui hơn,\ntiến bộ mỗi ngày',
  subtitle = 'Nền tảng học tập trực tuyến K-12 với bài giảng, bài kiểm tra và theo dõi tiến độ dành cho học sinh và phụ huynh.',
}) => {
  return (
    <div className="relative flex flex-col justify-between bg-[#b8320c] p-8 sm:p-12 lg:p-14 text-white min-h-[360px] lg:min-h-screen">
      {/* Top: Logo */}
      <Link to="/courses" className="flex items-center gap-3 w-fit group">
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-white text-[#b8320c] font-black text-xl shadow-xs transition group-hover:scale-105">
          B
        </div>
        <span className="text-xl font-extrabold tracking-tight text-white">
          BeeAcademy
        </span>
      </Link>

      {/* Middle: Headline & Features */}
      <div className="my-auto py-8 lg:py-0">
        <h1 className="text-3xl sm:text-4xl lg:text-4xl xl:text-5xl font-extrabold leading-tight text-white tracking-tight whitespace-pre-line">
          {title}
        </h1>
        <p className="mt-4 text-sm sm:text-base text-white/90 leading-relaxed max-w-md font-normal">
          {subtitle}
        </p>

        {/* Feature checklist */}
        <div className="mt-8 space-y-3.5 text-sm sm:text-base text-white/95 font-medium">
          <div className="flex items-center gap-3">
            <span className="font-bold text-white text-base">✓</span>
            <span>Khóa học bám sát chương trình K-12</span>
          </div>
          <div className="flex items-center gap-3">
            <span className="font-bold text-white text-base">✓</span>
            <span>Làm bài kiểm tra và xem kết quả ngay</span>
          </div>
          <div className="flex items-center gap-3">
            <span className="font-bold text-white text-base">✓</span>
            <span>Phụ huynh theo dõi tiến độ của con</span>
          </div>
        </div>
      </div>

      {/* Bottom: Copyright */}
      <div className="text-xs text-white/70 pt-6 lg:pt-0">
        © BeeAcademy
      </div>
    </div>
  );
};
