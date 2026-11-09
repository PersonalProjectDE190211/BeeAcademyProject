import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { authApi } from '../api/authApi';
import { AuthLeftBanner } from '../components/AuthLeftBanner';
import { getApiErrorMessage } from '@/shared/types/api';

export const ForgotPasswordPage: React.FC = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState<'request' | 'reset'>('request');
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleRequestOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) return;
    setLoading(true);
    setErrorMessage(null);
    try {
      await authApi.forgotPassword(email.trim());
      toast.success('Mã xác thực OTP 6 chữ số đã được gửi tới email của bạn.');
      setStep('reset');
    } catch (err) {
      const msg = getApiErrorMessage(err, 'Không thể gửi mã OTP.');
      setErrorMessage(msg);
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (newPassword.length < 8) {
      setErrorMessage('Mật khẩu mới phải có tối thiểu 8 ký tự.');
      return;
    }
    setLoading(true);
    setErrorMessage(null);
    try {
      await authApi.resetPassword({
        email: email.trim(),
        otp: otp.trim(),
        newPassword,
      });
      toast.success('Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại.');
      navigate('/login');
    } catch (err) {
      const msg = getApiErrorMessage(err, 'Mã OTP không hợp lệ hoặc đã hết hạn.');
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
        title={'Bảo mật an toàn,\nkhôi phục dễ dàng'}
        subtitle="Đặt lại mật khẩu nhanh chóng thông qua mã OTP bảo mật gửi tới email của bạn."
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
          <div
            className="auth-rise"
            style={{ '--rise-delay': '100ms' } as React.CSSProperties}
          >
            <h2 className="text-[28px] sm:text-[32px] font-bold tracking-tight text-ink mb-1.5">
              {step === 'request' ? 'Quên mật khẩu' : 'Đặt lại mật khẩu mới'}
            </h2>
            <p className="text-[15px] text-ink-muted">
              {step === 'request'
                ? 'Nhập địa chỉ email đăng ký để nhận mã OTP khôi phục.'
                : `Nhập mã OTP 6 chữ số đã gửi về ${email}.`}
            </p>
          </div>

          {errorMessage && (
            <div
              role="alert"
              className="rounded-[14px] border border-red-200 bg-red-50/90 px-4 py-3 text-sm text-red-700"
            >
              {errorMessage}
            </div>
          )}

          {step === 'request' ? (
            <form onSubmit={handleRequestOtp} className="flex flex-col gap-5">
              <div
                className="auth-rise"
                style={{ '--rise-delay': '250ms' } as React.CSSProperties}
              >
                <label
                  htmlFor="forgot-email"
                  className="block text-[14px] font-semibold text-ink mb-2"
                >
                  Địa chỉ Email
                </label>
                <input
                  id="forgot-email"
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

              <button
                type="submit"
                disabled={loading}
                style={{ '--rise-delay': '400ms' } as React.CSSProperties}
                className="auth-rise auth-cta h-[52px] w-full rounded-[14px] text-[15px] font-bold text-white cursor-pointer disabled:opacity-60"
              >
                {loading ? 'Đang gửi mã...' : 'Gửi mã xác thực OTP'}
              </button>
            </form>
          ) : (
            <form onSubmit={handleResetPassword} className="flex flex-col gap-5">
              <div
                className="auth-rise"
                style={{ '--rise-delay': '250ms' } as React.CSSProperties}
              >
                <label
                  htmlFor="reset-otp"
                  className="block text-[14px] font-semibold text-ink mb-2"
                >
                  Mã xác thực OTP (6 chữ số)
                </label>
                <input
                  id="reset-otp"
                  type="text"
                  placeholder="123456"
                  value={otp}
                  onChange={(e) => setOtp(e.target.value)}
                  required
                  maxLength={6}
                  disabled={loading}
                  className="auth-input tracking-[0.35em] text-center font-mono text-base"
                />
              </div>

              <div
                className="auth-rise"
                style={{ '--rise-delay': '400ms' } as React.CSSProperties}
              >
                <label
                  htmlFor="reset-new-password"
                  className="block text-[14px] font-semibold text-ink mb-2"
                >
                  Mật khẩu mới (tối thiểu 8 ký tự)
                </label>
                <input
                  id="reset-new-password"
                  type="password"
                  placeholder="••••••••"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  required
                  disabled={loading}
                  className="auth-input"
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                style={{ '--rise-delay': '550ms' } as React.CSSProperties}
                className="auth-rise auth-cta h-[52px] w-full rounded-[14px] text-[15px] font-bold text-white cursor-pointer disabled:opacity-60"
              >
                {loading ? 'Đang cập nhật...' : 'Xác nhận đặt lại mật khẩu'}
              </button>
            </form>
          )}

          <div
            className="auth-rise border-t border-[#eadfd6] pt-5 text-center text-[14px] text-ink-muted"
            style={{ '--rise-delay': '550ms' } as React.CSSProperties}
          >
            Nhớ mật khẩu?{' '}
            <Link to="/login" className="auth-link">
              Quay lại đăng nhập
            </Link>
          </div>
        </div>
      </main>
    </div>
  );
};
