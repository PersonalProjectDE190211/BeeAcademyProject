import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { KeyRound, ArrowLeft } from 'lucide-react';
import { authApi } from '../api/authApi';
import { getApiErrorMessage } from '@/shared/types/api';
import { Button, Card, Input } from '@/shared/ui/primitives';

export const ForgotPasswordPage: React.FC = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState<'request' | 'reset'>('request');
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleRequestOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) return;
    setLoading(true);
    try {
      await authApi.forgotPassword(email.trim());
      toast.success('Mã xác thực OTP 6 chữ số đã được gửi tới email của bạn.');
      setStep('reset');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể gửi mã OTP.'));
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (newPassword.length < 8) {
      toast.error('Mật khẩu mới phải có tối thiểu 8 ký tự.');
      return;
    }
    setLoading(true);
    try {
      await authApi.resetPassword({
        email: email.trim(),
        otp: otp.trim(),
        newPassword,
      });
      toast.success('Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại.');
      navigate('/login');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Mã OTP không hợp lệ hoặc đã hết hạn.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto flex min-h-[70vh] max-w-md flex-col justify-center py-8">
      <div className="mb-6 text-center">
        <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-amber-500 text-white shadow-sm">
          <KeyRound className="h-6 w-6" />
        </div>
        <h1 className="text-2xl font-bold text-slate-900">Khôi phục mật khẩu</h1>
        <p className="mt-1 text-sm text-slate-600">
          {step === 'request'
            ? 'Nhập email đăng ký để nhận mã OTP 6 chữ số'
            : `Nhập mã OTP đã gửi tới ${email}`}
        </p>
      </div>

      <Card className="p-6 sm:p-8">
        {step === 'request' ? (
          <form onSubmit={handleRequestOtp} className="space-y-4">
            <Input
              label="Địa chỉ Email"
              type="email"
              placeholder="hocsinh@beeacademy.edu.vn"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <Button type="submit" loading={loading} className="w-full" size="lg">
              Gửi mã xác thực OTP
            </Button>
          </form>
        ) : (
          <form onSubmit={handleResetPassword} className="space-y-4">
            <Input
              label="Mã OTP (6 chữ số)"
              placeholder="123456"
              maxLength={6}
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              required
            />
            <Input
              label="Mật khẩu mới (tối thiểu 8 ký tự)"
              type="password"
              placeholder="••••••••"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
            />
            <Button type="submit" loading={loading} className="w-full" size="lg">
              Xác nhận đặt lại mật khẩu
            </Button>
            <Button
              type="button"
              variant="ghost"
              className="w-full"
              onClick={() => setStep('request')}
            >
              Gửi lại mã khác
            </Button>
          </form>
        )}

        <div className="mt-6 border-t border-slate-100 pt-5 text-center">
          <Link
            to="/login"
            className="inline-flex items-center gap-1.5 text-sm font-medium text-slate-600 hover:text-slate-900"
          >
            <ArrowLeft className="h-4 w-4" />
            Quay lại trang Đăng nhập
          </Link>
        </div>
      </Card>
    </div>
  );
};
