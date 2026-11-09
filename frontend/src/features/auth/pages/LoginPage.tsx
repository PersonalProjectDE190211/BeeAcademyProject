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
    <div className="min-h-screen bg-white">
      <div className="grid grid-cols-1 lg:grid-cols-12 min-h-screen">
        {/* Left Column: Branded Terracotta Banner */}
        <div className="lg:col-span-5 xl:col-span-5">
          <AuthLeftBanner
            title={'Học tập vui hơn,\ntiến bộ mỗi ngày'}
            subtitle="Nền tảng học tập trực tuyến K-12 với bài giảng, bài kiểm tra và theo dõi tiến độ dành cho học sinh và phụ huynh."
          />
        </div>

        {/* Right Column: Login Form */}
        <div className="lg:col-span-7 xl:col-span-7 flex flex-col justify-center px-6 py-10 sm:px-12 lg:px-16 xl:px-24">
          <div className="mx-auto w-full max-w-md">
            {/* Form Title & Subtitle */}
            <div className="mb-6">
              <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
                Đăng nhập
              </h2>
              <p className="mt-1 text-sm text-slate-600">
                Chưa có tài khoản?{' '}
                <Link to="/register" className="font-semibold text-[#b8320c] hover:underline">
                  Đăng ký ngay
                </Link>
              </p>
            </div>

            {/* Google Sign-in Button */}
            <GoogleSignInButton
              mode="signin"
              text="Tiếp tục với Google"
              className="mb-5"
            />

            {/* Divider: hoặc dùng email */}
            <div className="relative mb-5">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-slate-200" />
              </div>
              <div className="relative flex justify-center text-xs">
                <span className="bg-white px-3 text-slate-400">hoặc dùng email</span>
              </div>
            </div>

            {/* Error Message Alert */}
            {errorMessage && (
              <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-xs sm:text-sm text-red-700">
                {errorMessage}
              </div>
            )}

            {/* Login Form */}
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                  Email
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

              <div>
                <div className="flex items-center justify-between mb-1.5">
                  <label className="block text-xs font-semibold text-slate-700">
                    Mật khẩu
                  </label>
                  <Link
                    to="/forgot-password"
                    className="text-xs font-medium text-[#b8320c] hover:underline"
                  >
                    Quên mật khẩu?
                  </Link>
                </div>
                <input
                  type="password"
                  placeholder="Nhập mật khẩu"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  autoComplete="current-password"
                  className="w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:border-[#b8320c] focus:outline-hidden focus:ring-1 focus:ring-[#b8320c]"
                />
              </div>

              {/* Submit button */}
              <button
                type="submit"
                disabled={loading}
                className="w-full rounded-xl bg-[#b8320c] hover:bg-[#a12b0a] text-white font-bold py-3 text-sm transition shadow-sm cursor-pointer disabled:opacity-60 mt-2"
              >
                {loading ? 'Đang đăng nhập...' : 'Đăng nhập hệ thống'}
              </button>
            </form>

            {/* Teacher note footer */}
            <p className="mt-8 text-center text-xs text-slate-500 leading-relaxed">
              Bạn là giáo viên? Tài khoản giáo viên được cấp sau khi duyệt hồ sơ. Gửi hồ sơ đến{' '}
              <a
                href="mailto:admin@beeacademy.edu.vn"
                className="font-medium text-[#b8320c] underline hover:text-[#9c2909]"
              >
                admin@beeacademy.edu.vn
              </a>.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
