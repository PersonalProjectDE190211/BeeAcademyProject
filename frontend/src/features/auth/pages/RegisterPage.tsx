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
    <div className="min-h-screen bg-white">
      <div className="grid grid-cols-1 lg:grid-cols-12 min-h-screen">
        {/* Left Column: Branded Terracotta Banner */}
        <div className="lg:col-span-5 xl:col-span-5">
          <AuthLeftBanner
            title={'Học tập vui hơn,\ntiến bộ mỗi ngày'}
            subtitle="Nền tảng học tập trực tuyến K-12 với bài giảng, bài kiểm tra và theo dõi tiến độ dành cho học sinh và phụ huynh."
          />
        </div>

        {/* Right Column: Register Form */}
        <div className="lg:col-span-7 xl:col-span-7 flex flex-col justify-center px-6 py-10 sm:px-12 lg:px-16 xl:px-24">
          <div className="mx-auto w-full max-w-md">
            {/* Form Title & Subtitle */}
            <div className="mb-6">
              <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
                Tạo tài khoản
              </h2>
              <p className="mt-1 text-sm text-slate-600">
                Đã có tài khoản?{' '}
                <Link to="/login" className="font-semibold text-[#b8320c] hover:underline">
                  Đăng nhập
                </Link>
              </p>
            </div>

            {/* Role Switcher: Học sinh / Phụ huynh */}
            <div className="grid grid-cols-2 p-1 bg-slate-100 rounded-xl mb-5">
              <button
                type="button"
                onClick={() => setRole('student')}
                className={`py-2 text-sm font-semibold rounded-lg transition cursor-pointer ${
                  role === 'student'
                    ? 'bg-white text-[#b8320c] shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                Học sinh
              </button>
              <button
                type="button"
                onClick={() => setRole('parent')}
                className={`py-2 text-sm font-semibold rounded-lg transition cursor-pointer ${
                  role === 'parent'
                    ? 'bg-white text-[#b8320c] shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
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

            {/* Registration Form */}
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                  Họ và tên
                </label>
                <input
                  type="text"
                  placeholder="Nguyễn Văn An"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  required
                  className="w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:border-[#b8320c] focus:outline-hidden focus:ring-1 focus:ring-[#b8320c]"
                />
              </div>

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

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                    Mật khẩu
                  </label>
                  <input
                    type="password"
                    placeholder="Tối thiểu 8 ký tự"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    autoComplete="new-password"
                    className="w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:border-[#b8320c] focus:outline-hidden focus:ring-1 focus:ring-[#b8320c]"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                    Nhập lại mật khẩu
                  </label>
                  <input
                    type="password"
                    placeholder="Nhập lại"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                    autoComplete="new-password"
                    className="w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:border-[#b8320c] focus:outline-hidden focus:ring-1 focus:ring-[#b8320c]"
                  />
                </div>
              </div>

              {/* Terms & Privacy checkbox */}
              <label className="flex items-start gap-2.5 text-xs text-slate-600 cursor-pointer pt-1">
                <input
                  type="checkbox"
                  checked={agreeTerms}
                  onChange={(e) => setAgreeTerms(e.target.checked)}
                  required
                  className="mt-0.5 h-4 w-4 rounded border-slate-300 text-[#b8320c] focus:ring-[#b8320c]"
                />
                <span className="leading-relaxed">
                  Tôi đồng ý với{' '}
                  <a href="#" className="font-medium text-[#b8320c] underline hover:text-[#9c2909]">
                    Điều khoản sử dụng
                  </a>{' '}
                  và{' '}
                  <a href="#" className="font-medium text-[#b8320c] underline hover:text-[#9c2909]">
                    Chính sách bảo mật
                  </a>
                </span>
              </label>

              {/* Submit button */}
              <button
                type="submit"
                disabled={loading}
                className="w-full rounded-xl bg-[#b8320c] hover:bg-[#a12b0a] text-white font-bold py-3 text-sm transition shadow-sm cursor-pointer disabled:opacity-60"
              >
                {loading ? 'Đang tạo tài khoản...' : 'Hoàn tất đăng ký'}
              </button>
            </form>

            {/* Teacher note footer */}
            <p className="mt-6 text-center text-xs text-slate-500 leading-relaxed">
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
