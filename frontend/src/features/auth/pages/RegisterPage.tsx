import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Mail, UserPlus } from 'lucide-react';
import { authApi } from '../api/authApi';
import { GoogleSignInButton } from '../components/GoogleSignInButton';
import { getApiErrorMessage } from '@/shared/types/api';
import { Button, Card, Input, Select } from '@/shared/ui/primitives';

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [role, setRole] = useState<'student' | 'parent'>('student');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (password.length < 8) {
      setErrorMessage('Mật khẩu phải có ít nhất 8 ký tự.');
      return;
    }
    if (password !== confirmPassword) {
      setErrorMessage('Mật khẩu xác nhận không khớp.');
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
    <div className="mx-auto flex min-h-[75vh] max-w-md flex-col justify-center py-8">
      <div className="mb-6 text-center">
        <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-amber-500 text-white shadow-sm">
          <UserPlus className="h-6 w-6" />
        </div>
        <h1 className="text-2xl font-bold text-slate-900">Tạo tài khoản mới</h1>
        <p className="mt-1 text-sm text-slate-600">
          Tham gia cộng đồng học tập thông minh BeeAcademy
        </p>
      </div>

      <Card className="p-6 sm:p-8">
        {errorMessage && (
          <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {errorMessage}
          </div>
        )}

        {/* Đăng ký nhanh bằng Google */}
        <div className="space-y-3 mb-5">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-700">Loại tài khoản:</span>
            <div className="inline-flex rounded-lg bg-slate-100 p-0.5 text-xs">
              <button
                type="button"
                onClick={() => setRole('student')}
                className={`rounded-md px-2.5 py-1 font-medium transition cursor-pointer ${
                  role === 'student'
                    ? 'bg-white text-slate-900 shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                🎓 Học sinh
              </button>
              <button
                type="button"
                onClick={() => setRole('parent')}
                className={`rounded-md px-2.5 py-1 font-medium transition cursor-pointer ${
                  role === 'parent'
                    ? 'bg-white text-slate-900 shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                👨‍👩‍👧 Phụ huynh
              </button>
            </div>
          </div>

          <GoogleSignInButton mode="signup" accountType={role} />
        </div>

        <div className="relative mb-5">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-slate-200" />
          </div>
          <div className="relative flex justify-center text-xs uppercase">
            <span className="bg-white px-3 text-slate-500 font-medium">Hoặc đăng ký bằng email</span>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Họ và tên đầy đủ"
            placeholder="Nguyễn Văn An"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            required
          />

          <Input
            label="Địa chỉ Email"
            type="email"
            placeholder="hocsinh@beeacademy.edu.vn"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <Select
            label="Vai trò tham gia"
            value={role}
            onChange={(e) => setRole(e.target.value as 'student' | 'parent')}
          >
            <option value="student">🎓 Học sinh (Học tập & Làm bài kiểm tra)</option>
            <option value="parent">👨‍👩‍👧 Phụ huynh (Đồng hành & Quản lý con)</option>
          </Select>

          {/* Ghi chú hướng dẫn cho Giáo viên */}
          <div className="rounded-xl border border-amber-200/70 bg-amber-50/60 p-3 text-xs text-amber-900">
            <div className="flex items-center gap-1.5 font-semibold text-amber-950 mb-0.5">
              <Mail className="h-3.5 w-3.5 text-amber-600" />
              Thầy/Cô có nhu cầu giảng dạy trên BeeAcademy?
            </div>
            <p className="text-amber-800 leading-relaxed">
              Để bảo đảm chuẩn chất lượng sư phạm K-12, tài khoản Giáo viên được cấp sau khi duyệt thẩm định. Thầy/Cô vui lòng gửi hồ sơ đến{' '}
              <a
                href="mailto:admin@beeacademy.edu.vn"
                className="font-medium text-amber-950 underline decoration-amber-400 underline-offset-2 hover:text-amber-800"
              >
                admin@beeacademy.edu.vn
              </a>{' '}
              để Ban Quản trị cấp tài khoản.
            </p>
          </div>

          <Input
            label="Mật khẩu (tối thiểu 8 ký tự)"
            type="password"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          <Input
            label="Xác nhận mật khẩu"
            type="password"
            placeholder="••••••••"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
          />

          <Button type="submit" loading={loading} className="w-full" size="lg">
            Hoàn tất đăng ký
          </Button>
        </form>

        <div className="mt-6 border-t border-slate-100 pt-5 text-center text-sm text-slate-600">
          Đã có tài khoản?{' '}
          <Link to="/login" className="font-semibold text-amber-600 hover:text-amber-700">
            Đăng nhập ngay
          </Link>
        </div>
      </Card>
    </div>
  );
};
