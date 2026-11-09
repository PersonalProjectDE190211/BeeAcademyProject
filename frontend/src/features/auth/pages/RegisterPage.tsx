import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { authApi } from '../api/authApi';
import { AuthLeftBanner } from '../components/AuthLeftBanner';
import { GoogleSignInButton } from '../components/GoogleSignInButton';
import { getApiErrorMessage } from '@/shared/types/api';

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [role, setRole] = useState<'student' | 'parent'>('student');
  const [agreeTerms, setAgreeTerms] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const passwordMismatch =
    confirmPassword.length > 0 && password !== confirmPassword;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!fullName.trim() || !email.trim()) {
      setErrorMessage('Vui lòng điền đầy đủ họ tên và email.');
      return;
    }
    if (password.length < 8) {
      setErrorMessage('Mật khẩu phải có ít nhất 8 ký tự.');
      return;
    }
    if (password !== confirmPassword) {
      setErrorMessage('Nhập lại mật khẩu không khớp.');
      return;
    }
    if (!agreeTerms) {
      setErrorMessage('Vui lòng đồng ý với Điều khoản sử dụng và Chính sách bảo mật.');
      return;
    }

    setLoading(true);
    setErrorMessage(null);
    try {
      await authApi.register({
        fullName: fullName.trim(),
        email: email.trim(),
        password,
        role: role.toUpperCase() as 'STUDENT' | 'PARENT',
      });

      toast.success('Đăng ký tài khoản thành công! Vui lòng đăng nhập.');
      navigate('/login');
    } catch (err) {
      const msg = getApiErrorMessage(err, 'Không thể đăng ký tài khoản.');
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
        subtitle="Nền tảng học tập trực tuyến K–12 với bài giảng, bài kiểm tra và theo dõi tiến độ dành cho học sinh và phụ huynh."
      />

      {/* Right Warm Cream Form Panel */}
      <main className="relative overflow-hidden flex items-center justify-center p-6 sm:p-10 bg-cream">
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

        <div className="relative z-10 w-full max-w-[440px] flex flex-col gap-4">
          {/* Header */}
          <div
            className="auth-rise"
            style={{ '--rise-delay': '100ms' } as React.CSSProperties}
          >
            <h2 className="text-[28px] sm:text-[32px] font-bold tracking-tight text-ink mb-1">
              Tạo tài khoản
            </h2>
            <p className="text-[15px] text-ink-muted">
              Đã có tài khoản?{' '}
              <Link to="/login" className="auth-link">
                Đăng nhập
              </Link>
            </p>
          </div>

          {/* Role Switcher: Học sinh / Phụ huynh */}
          <div
            role="radiogroup"
            aria-label="Chọn vai trò tài khoản"
            className="auth-rise grid grid-cols-2 p-1 bg-sand-soft rounded-[14px]"
            style={{ '--rise-delay': '200ms' } as React.CSSProperties}
          >
            <button
              type="button"
              role="radio"
              aria-checked={role === 'student'}
              onClick={() => setRole('student')}
              className={`h-10 text-[14px] font-semibold rounded-[10px] transition-all cursor-pointer ${
                role === 'student'
                  ? 'bg-white text-brand-700 shadow-[0_2px_8px_rgba(28,25,23,0.08)]'
                  : 'text-ink-muted hover:text-ink'
              }`}
            >
              Học sinh
            </button>
            <button
              type="button"
              role="radio"
              aria-checked={role === 'parent'}
              onClick={() => setRole('parent')}
              className={`h-10 text-[14px] font-semibold rounded-[10px] transition-all cursor-pointer ${
                role === 'parent'
                  ? 'bg-white text-brand-700 shadow-[0_2px_8px_rgba(28,25,23,0.08)]'
                  : 'text-ink-muted hover:text-ink'
              }`}
            >
              Phụ huynh
            </button>
          </div>

          {/* Google Sign-up Button */}
          <GoogleSignInButton
            mode="signup"
            accountType={role}
            text="Tiếp tục với Google"
            className="auth-rise"
            style={{ '--rise-delay': '300ms' } as React.CSSProperties}
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

          {/* Registration Form */}
          <form onSubmit={handleSubmit} className="flex flex-col gap-4" noValidate>
            <div
              className="auth-rise"
              style={{ '--rise-delay': '500ms' } as React.CSSProperties}
            >
              <label
                htmlFor="register-fullname"
                className="block text-[13px] font-semibold text-ink mb-1.5"
              >
                Họ và tên
              </label>
              <input
                id="register-fullname"
                type="text"
                placeholder="Nguyễn Văn An"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
                autoComplete="name"
                disabled={loading}
                className="auth-input"
              />
            </div>

            <div
              className="auth-rise"
              style={{ '--rise-delay': '600ms' } as React.CSSProperties}
            >
              <label
                htmlFor="register-email"
                className="block text-[13px] font-semibold text-ink mb-1.5"
              >
                Email
              </label>
              <input
                id="register-email"
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
              className="auth-rise grid grid-cols-1 sm:grid-cols-2 gap-3"
              style={{ '--rise-delay': '700ms' } as React.CSSProperties}
            >
              <div>
                <label
                  htmlFor="register-password"
                  className="block text-[13px] font-semibold text-ink mb-1.5"
                >
                  Mật khẩu
                </label>
                <input
                  id="register-password"
                  type="password"
                  placeholder="Tối thiểu 8 ký tự"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  autoComplete="new-password"
                  disabled={loading}
                  aria-invalid={password.length > 0 && password.length < 8}
                  className="auth-input"
                />
              </div>

              <div>
                <label
                  htmlFor="register-confirm-password"
                  className="block text-[13px] font-semibold text-ink mb-1.5"
                >
                  Nhập lại mật khẩu
                </label>
                <input
                  id="register-confirm-password"
                  type="password"
                  placeholder="Nhập lại"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                  autoComplete="new-password"
                  disabled={loading}
                  aria-invalid={passwordMismatch}
                  className="auth-input"
                />
              </div>
            </div>

            {passwordMismatch && (
              <p className="text-xs font-medium text-red-600 -mt-2">
                Mật khẩu nhập lại chưa khớp.
              </p>
            )}

            {/* Terms & Privacy Checkbox */}
            <label
              className="auth-rise flex items-start gap-2.5 text-[13px] text-ink-muted cursor-pointer pt-0.5"
              style={{ '--rise-delay': '800ms' } as React.CSSProperties}
            >
              <input
                type="checkbox"
                checked={agreeTerms}
                onChange={(e) => setAgreeTerms(e.target.checked)}
                required
                disabled={loading}
                className="mt-0.5 h-4 w-4 rounded border-sand-strong accent-brand-700 cursor-pointer"
              />
              <span className="leading-relaxed">
                Tôi đồng ý với{' '}
                <a href="#terms" onClick={(e) => e.preventDefault()} className="auth-link">
                  Điều khoản sử dụng
                </a>{' '}
                và{' '}
                <a href="#privacy" onClick={(e) => e.preventDefault()} className="auth-link">
                  Chính sách bảo mật
                </a>
              </span>
            </label>

            {/* Submit CTA & Teacher Note */}
            <div
              className="auth-rise flex flex-col gap-3.5 pt-1"
              style={{ '--rise-delay': '900ms' } as React.CSSProperties}
            >
              <button
                type="submit"
                disabled={loading}
                className="auth-cta h-[52px] w-full rounded-[14px] text-[15px] font-bold text-white cursor-pointer disabled:opacity-60"
              >
                {loading ? 'Đang tạo tài khoản...' : 'Hoàn tất đăng ký'}
              </button>

              <p className="text-center text-[12px] text-ink-muted leading-relaxed">
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
