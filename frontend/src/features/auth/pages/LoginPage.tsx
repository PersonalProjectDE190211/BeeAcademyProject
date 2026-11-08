import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import toast from 'react-hot-toast';
import { GraduationCap, Lock, Mail } from 'lucide-react';
import { authApi } from '../api/authApi';
import { useAuthStore } from '@/shared/store/useAuthStore';
import { getApiErrorMessage } from '@/shared/types/api';
import { Button, Card, Input } from '@/shared/ui/primitives';

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
    <div className="mx-auto flex min-h-[75vh] max-w-md flex-col justify-center py-8">
      <div className="mb-6 text-center">
        <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-amber-500 text-white shadow-sm">
          <GraduationCap className="h-7 w-7" />
        </div>
        <h1 className="text-2xl font-bold text-slate-900">Đăng nhập BeeAcademy</h1>
        <p className="mt-1 text-sm text-slate-600">
          Nền tảng học trực tuyến & Luyện thi Phổ thông K-12
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
            label="Địa chỉ Email"
            type="email"
            placeholder="hocsinh@beeacademy.edu.vn"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            autoComplete="email"
          />

          <div>
            <Input
              label="Mật khẩu"
              type="password"
              placeholder="Nhập mật khẩu của bạn"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="current-password"
            />
            <div className="mt-1.5 flex justify-end">
              <Link
                to="/forgot-password"
                className="text-xs font-medium text-amber-600 hover:text-amber-700"
              >
                Quên mật khẩu?
              </Link>
            </div>
          </div>

          <Button type="submit" loading={loading} className="w-full" size="lg">
            <Lock className="h-4 w-4" />
            Đăng nhập hệ thống
          </Button>
        </form>

        <div className="mt-6 border-t border-slate-100 pt-5 text-center text-sm text-slate-600">
          Chưa có tài khoản BeeAcademy?{' '}
          <Link to="/register" className="font-semibold text-amber-600 hover:text-amber-700">
            Đăng ký ngay
          </Link>
        </div>

        <div className="mt-4 rounded-xl bg-slate-50 p-3 text-xs text-slate-500">
          <div className="flex items-center gap-1.5 font-medium text-slate-700">
            <Mail className="h-3.5 w-3.5 text-amber-500" />
            Bảo mật phiên đăng nhập
          </div>
          <p className="mt-1">
            Hệ thống bảo vệ tài khoản tự động khóa tạm thời 15 phút nếu nhập sai mật khẩu quá 5 lần liên tiếp.
          </p>
        </div>
      </Card>
    </div>
  );
};
