import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { UserPlus } from 'lucide-react';
import { authApi } from '../api/authApi';
import { getApiErrorMessage } from '@/shared/types/api';
import { Button, Card, Input, Select } from '@/shared/ui/primitives';

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [role, setRole] = useState<'student' | 'teacher' | 'parent'>('student');
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
        role,
      });

      if (role === 'teacher') {
        toast.success('Đăng ký tài khoản Giáo viên thành công! Hồ sơ của bạn đang chờ Admin phê duyệt.');
      } else {
        toast.success('Đăng ký tài khoản thành công! Vui lòng đăng nhập.');
      }
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
            onChange={(e) => setRole(e.target.value as 'student' | 'teacher' | 'parent')}
          >
            <option value="student">Học sinh (Học tập & Làm bài kiểm tra)</option>
            <option value="parent">Phụ huynh (Theo dõi & Thanh toán hộ con)</option>
            <option value="teacher">Giáo viên (Giảng dạy - Cần Admin duyệt)</option>
          </Select>

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
