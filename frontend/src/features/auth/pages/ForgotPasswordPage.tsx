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
    <div className="min-h-screen bg-white">
      <div className="grid grid-cols-1 lg:grid-cols-12 min-h-screen">
        {/* Left Column: Branded Terracotta Banner */}
        <div className="lg:col-span-5 xl:col-span-5">
          <AuthLeftBanner
            title={'Bảo mật an toàn,\nkhôi phục dễ dàng'}
            subtitle="Đặt lại mật khẩu nhanh chóng thông qua mã OTP bảo mật gửi tới email của bạn."
          />
        </div>

        {/* Right Column: Forgot / Reset Password Form */}
        <div className="lg:col-span-7 xl:col-span-7 flex flex-col justify-center px-6 py-10 sm:px-12 lg:px-16 xl:px-24">
          <div className="mx-auto w-full max-w-md">
            <div className="mb-6">
              <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
                {step === 'request' ? 'Quên mật khẩu' : 'Đặt lại mật khẩu mới'}
              </h2>
              <p className="mt-1 text-sm text-slate-600">
                {step === 'request'
                  ? 'Nhập địa chỉ email đăng ký để nhận mã OTP khôi phục.'
                  : `Nhập mã OTP 6 chữ số đã gửi về ${email}.`}
              </p>
            </div>

            {errorMessage && (
              <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-xs sm:text-sm text-red-700">
                {errorMessage}
              </div>
            )}

            {step === 'request' ? (
              <form onSubmit={handleRequestOtp} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                    Địa chỉ Email
                  </label>
                  <input
                    type="email"
                    placeholder="ten@email.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    autoComplete="email"
                    className="w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:border-[#b8320c] focus:outline-hidden focus:ring-1 focus:ring-[#b8320c]"
                  />
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full rounded-xl bg-[#b8320c] hover:bg-[#a12b0a] text-white font-bold py-3 text-sm transition shadow-sm cursor-pointer disabled:opacity-60"
                >
                  {loading ? 'Đang gửi mã...' : 'Gửi mã xác thực OTP'}
                </button>
              </form>
            ) : (
              <form onSubmit={handleResetPassword} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                    Mã xác thực OTP (6 chữ số)
                  </label>
                  <input
                    type="text"
                    placeholder="123456"
                    value={otp}
                    onChange={(e) => setOtp(e.target.value)}
                    required
                    maxLength={6}
                    className="w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 tracking-widest text-center font-mono text-base focus:border-[#b8320c] focus:outline-hidden focus:ring-1 focus:ring-[#b8320c]"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                    Mật khẩu mới (tối thiểu 8 ký tự)
                  </label>
                  <input
                    type="password"
                    placeholder="••••••••"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    required
                    className="w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:border-[#b8320c] focus:outline-hidden focus:ring-1 focus:ring-[#b8320c]"
                  />
                </div>

                <button
                  type="submit"
                  disabled={loading}
                  className="w-full rounded-xl bg-[#b8320c] hover:bg-[#a12b0a] text-white font-bold py-3 text-sm transition shadow-sm cursor-pointer disabled:opacity-60"
                >
                  {loading ? 'Đang cập nhật...' : 'Xác nhận đặt lại mật khẩu'}
                </button>
              </form>
            )}

            <div className="mt-6 border-t border-slate-100 pt-5 text-center text-sm text-slate-600">
              Nhớ mật khẩu?{' '}
              <Link to="/login" className="font-semibold text-[#b8320c] hover:underline">
                Quay lại đăng nhập
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
