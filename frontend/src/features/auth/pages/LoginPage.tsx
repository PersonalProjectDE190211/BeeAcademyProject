import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import toast from 'react-hot-toast';
import { authApi } from '../api/authApi';
import { AuthLeftBanner } from '../components/AuthLeftBanner';
import { GoogleSignInButton } from '../components/GoogleSignInButton';
import { useAuthStore } from '@/shared/store/useAuthStore';
import { getApiErrorMessage } from '@/shared/types/api';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const setSession = useAuthStore((s) => s.setSession);

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim() || !password) {
      setErrorMessage('Vui lòng nhập đầy đủ email và mật khẩu.');
      return;
    }

    setLoading(true);
    setErrorMessage(null);
    try {
      const result = await authApi.login({ email: email.trim(), password });
      setSession(result.accessToken, result.user);
      toast.success(`Chào mừng trở lại, ${result.user.fullName}!`);

      const from = (location.state as { from?: string } | null)?.from;
      if (from) {
        navigate(from, { replace: true });
      } else {
        const role = result.user.role.toLowerCase();
        if (role === 'admin') navigate('/admin/dashboard', { replace: true });
        else if (role === 'teacher') navigate('/teacher/courses', { replace: true });
        else if (role === 'parent') navigate('/parent/portal', { replace: true });
        else navigate('/learning/my-courses', { replace: true });
      }
    } catch (err) {
      const msg = getApiErrorMessage(err, 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.');
      setErrorMessage(msg);
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen grid grid-cols-1 lg:grid-cols-2 text-ink">
      {/* Left Branded Animated Panel */}
      <AuthLeftBanner
        title={'Học tập vui hơn,\ntiến bộ mỗi ngày'}
        subtitle="Bài giảng sinh động, bài kiểm tra có kết quả ngay và theo dõi tiến độ dành cho học sinh và phụ huynh."
      />

      {/* Right Warm Cream Form Panel */}
      <main className="relative overflow-hidden flex items-center justify-center p-6 sm:p-12 bg-cream">
        {/* Ambient blurred blobs */}
        <div
          className="auth-blob bg-peach"
          style={{ width: 300, height: 300, top: -80, right: -80, opacity: 0.7 }}
          aria-hidden="true"
        />
        <div
          className="auth-blob auth-blob--late bg-butter"
          style={{ width: 240, height: 240, bottom: -60, left: -60, opacity: 0.7 }}
          aria-hidden="true"
        />

        <div className="relative z-10 w-full max-w-[440px] flex flex-col gap-5">
          {/* Header */}
          <div
            className="auth-rise"
            style={{ '--rise-delay': '100ms' } as React.CSSProperties}
          >
            <h2 className="text-[30px] sm:text-[32px] font-bold tracking-tight text-ink mb-1.5">
              Chào mừng trở lại
            </h2>
            <p className="text-[15px] text-ink-muted">
              Chưa có tài khoản?{' '}
              <Link to="/register" className="auth-link">
                Đăng ký ngay
              </Link>
            </p>
          </div>

          {/* Google Sign-In Button */}
          <GoogleSignInButton
            mode="signin"
            text="Tiếp tục với Google"
            className="auth-rise"
            style={{ '--rise-delay': '250ms' } as React.CSSProperties}
          />

          {/* Divider */}
          <div
            className="auth-rise flex items-center gap-3.5 text-[13px] text-[#78716c]"
            style={{ '--rise-delay': '400ms' } as React.CSSProperties}
          >
            <span className="flex-1 h-px bg-[#eadfd6]" />
            <span>hoặc dùng email</span>
            <span className="flex-1 h-px bg-[#eadfd6]" />
          </div>

          {/* Error Alert */}
          {errorMessage && (
            <div
              role="alert"
              className="rounded-[14px] border border-red-200 bg-red-50/90 px-4 py-3 text-sm text-red-700"
            >
              {errorMessage}
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="flex flex-col gap-5" noValidate>
            <div
              className="auth-rise"
              style={{ '--rise-delay': '550ms' } as React.CSSProperties}
            >
              <label
                htmlFor="login-email"
                className="block text-[14px] font-semibold text-ink mb-2"
              >
                Email
              </label>
              <input
                id="login-email"
                type="email"
                placeholder="ten@email.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
                disabled={loading}
                className="auth-input"
              />
            </div>

            <div
              className="auth-rise"
              style={{ '--rise-delay': '700ms' } as React.CSSProperties}
            >
              <div className="flex items-center justify-between mb-2">
                <label
                  htmlFor="login-password"
                  className="text-[14px] font-semibold text-ink"
                >
                  Mật khẩu
                </label>
                <Link to="/forgot-password" className="auth-link text-[14px]">
                  Quên mật khẩu?
                </Link>
              </div>
              <div className="relative">
                <input
                  id="login-password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Nhập mật khẩu"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  autoComplete="current-password"
                  disabled={loading}
                  className="auth-input pr-12"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((prev) => !prev)}
                  aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                  className="absolute right-3 top-1/2 -translate-y-1/2 rounded-lg px-2 py-1 text-xs font-semibold text-ink-muted hover:text-brand-700 transition cursor-pointer"
                >
                  {showPassword ? 'Ẩn' : 'Hiện'}
                </button>
              </div>
            </div>

            <div
              className="auth-rise flex flex-col gap-4"
              style={{ '--rise-delay': '850ms' } as React.CSSProperties}
            >
              <button
                type="submit"
                disabled={loading}
                className="auth-cta h-[52px] w-full rounded-[14px] text-[16px] font-bold text-white cursor-pointer disabled:opacity-60"
              >
                {loading ? 'Đang đăng nhập...' : 'Đăng nhập hệ thống'}
              </button>

              <p className="text-center text-[13px] text-ink-muted leading-relaxed pt-1">
                Bạn là giáo viên? Tài khoản giáo viên được cấp sau khi duyệt hồ sơ. Gửi hồ sơ đến{' '}
                <a href="mailto:Thanhdatvv05@gmail.com" className="auth-link">
                  Thanhdatvv05@gmail.com
                </a>
                .
              </p>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
};
