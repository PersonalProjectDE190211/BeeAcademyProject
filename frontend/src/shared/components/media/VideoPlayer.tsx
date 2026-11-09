import React, { useRef, useState, useEffect } from 'react';
import { AlertCircle, ExternalLink, Play, Video } from 'lucide-react';

interface VideoPlayerProps {
  videoUrl?: string | null;
  title?: string;
  onProgress?: (progressPercent: number, currentTimeSec: number) => void;
  onCompleted?: () => void;
  autoPlay?: boolean;
  className?: string;
}

export function extractYouTubeId(url: string): string | null {
  if (!url) return null;
  // Match standard youtube, youtu.be, embed
  const regExp = /(?:youtube\.com\/(?:[^\/\n\s]+\/\S+\/|(?:v|e(?:mbed)?)\/|\S*?[?&]v=)|youtu\.be\/)([a-zA-Z0-9_-]{11})/;
  const match = url.match(regExp);
  return match && match[1] ? match[1] : null;
}

export function isDirectVideoUrl(url: string): boolean {
  if (!url) return false;
  const clean = url.split('?')[0].toLowerCase();
  return (
    clean.endsWith('.mp4') ||
    clean.endsWith('.webm') ||
    clean.endsWith('.ogg') ||
    clean.includes('/video/upload/') // Cloudinary video delivery path
  );
}

export const VideoPlayer: React.FC<VideoPlayerProps> = ({
  videoUrl,
  title,
  onProgress,
  onCompleted,
  autoPlay = false,
  className = '',
}) => {
  const videoRef = useRef<HTMLVideoElement>(null);
  const [hasStarted, setHasStarted] = useState(false);
  const [error, setError] = useState(false);

  useEffect(() => {
    setError(false);
    setHasStarted(false);
  }, [videoUrl]);

  if (!videoUrl || error) {
    return (
      <div
        className={`relative aspect-video w-full flex flex-col items-center justify-center rounded-2xl bg-slate-900 text-white p-6 ${className}`}
      >
        <div className="flex h-14 w-14 items-center justify-center rounded-full bg-slate-800 text-slate-400 mb-3">
          <Video className="h-7 w-7" />
        </div>
        <p className="text-sm font-semibold text-slate-200">
          {videoUrl ? 'Không thể tải luồng phát video này' : 'Chưa có video bài giảng'}
        </p>
        {videoUrl && (
          <a
            href={videoUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="mt-3 inline-flex items-center gap-1.5 text-xs font-medium text-amber-400 hover:text-amber-300 underline"
          >
            Mở liên kết nguồn ngoài <ExternalLink className="h-3.5 w-3.5" />
          </a>
        )}
      </div>
    );
  }

  const ytId = extractYouTubeId(videoUrl);

  // 1. YouTube Player (embed)
  if (ytId) {
    const embedSrc = `https://www.youtube-nocookie.com/embed/${ytId}?autoplay=${autoPlay ? 1 : 0}&rel=0&modestbranding=1&enablejsapi=1`;
    return (
      <div
        className={`relative aspect-video w-full overflow-hidden rounded-2xl bg-black shadow-lg border border-slate-800 ${className}`}
      >
        <iframe
          src={embedSrc}
          title={title || 'BeeAcademy Video Player'}
          className="h-full w-full border-0"
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
          allowFullScreen
        />
      </div>
    );
  }

  // 2. Direct HTML5 Video Player
  const handleTimeUpdate = () => {
    if (!videoRef.current) return;
    const current = videoRef.current.currentTime;
    const duration = videoRef.current.duration;
    if (duration > 0 && onProgress) {
      const pct = (current / duration) * 100;
      onProgress(pct, current);
      if (pct >= 95 && onCompleted) {
        onCompleted();
      }
    }
  };

  return (
    <div
      className={`relative aspect-video w-full overflow-hidden rounded-2xl bg-black shadow-lg border border-slate-800 group ${className}`}
    >
      <video
        ref={videoRef}
        src={videoUrl}
        controls
        autoPlay={autoPlay}
        playsInline
        onPlay={() => setHasStarted(true)}
        onTimeUpdate={handleTimeUpdate}
        onEnded={onCompleted}
        onError={() => setError(true)}
        className="h-full w-full object-contain"
      >
        Trình duyệt của bạn không hỗ trợ thẻ video HTML5.
      </video>
    </div>
  );
};

