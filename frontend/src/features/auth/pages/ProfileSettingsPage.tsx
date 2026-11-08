import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { ShieldCheck, User, Camera } from 'lucide-react';
import { authApi } from '../api/authApi';
import { useAuthStore } from '@/shared/store/useAuthStore';
import { getApiErrorMessage } from '@/shared/types/api';
import { Badge, Button, Card, ErrorState, Input, Spinner, Textarea } from '@/shared/ui/primitives';

export const ProfileSettingsPage: React.FC = () => {
  const { user, updateUser } = useAuthStore();
  const [loadingProfile, setLoadingProfile] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [bio, setBio] = useState('');
  const [avatarUrl, setAvatarUrl] = useState('');
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingAvatar, setSavingAvatar] = useState(false);

  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [savingPassword, setSavingPassword] = useState(false);

  const fetchProfile = async () => {
    setLoadingProfile(true);
    setLoadError(null);
    try {
      const profile = await authApi.getMyProfile();
      updateUser(profile);
      setFullName(profile.fullName || '');
      setPhone(profile.phone || '');
      setBio(profile.bio || '');
      setAvatarUrl(profile.avatarUrl || '');
    } catch (err) {
      setLoadError(getApiErrorMessage(err, 'Không thể tải thông tin hồ sơ cá nhân.'));
    } finally {
      setLoadingProfile(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setSavingProfile(true);
    try {
      const updated = await authApi.updateMyProfile({
        fullName: fullName.trim(),
        phone: phone.trim() || undefined,
        bio: bio.trim() || undefined,
      });
      updateUser(updated);
      toast.success('Cập nhật hồ sơ cá nhân thành công!');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Lỗi khi cập nhật thông tin cá nhân.'));
    } finally {
      setSavingProfile(false);
    }
  };

  const handleSaveAvatar = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!avatarUrl.trim()) return;
    setSavingAvatar(true);
    try {
      const updated = await authApi.updateAvatar(avatarUrl.trim());
      updateUser(updated);
      toast.success('Đã cập nhật ảnh đại diện!');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể cập nhật ảnh đại diện.'));
    } finally {
      setSavingAvatar(false);
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (newPassword.length < 8) {
      toast.error('Mật khẩu mới phải có tối thiểu 8 ký tự.');
      return;
    }
    if (newPassword !== confirmPassword) {
      toast.error('Mật khẩu xác nhận không khớp.');
      return;
    }

    setSavingPassword(true);
    try {
      await authApi.changePassword({ oldPassword, newPassword });
      setOldPassword('');
      setNewPassword('');
      setConfirmPassword('');
      toast.success('Đổi mật khẩu thành công! Các thiết bị khác đã được đăng xuất bảo mật.');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Mật khẩu hiện tại không đúng hoặc không hợp lệ.'));
    } finally {
      setSavingPassword(false);
    }
  };

  if (loadingProfile) {
    return <Spinner label="Đang tải hồ sơ cá nhân..." />;
  }

  if (loadError) {
    return <ErrorState message={loadError} onRetry={fetchProfile} />;
  }

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Hồ sơ & Bảo mật tài khoản</h1>
          <p className="text-sm text-slate-600">
            Quản lý thông tin định danh và mật khẩu truy cập BeeAcademy (UC04, UC05)
          </p>
        </div>
        {user && (
          <div className="flex items-center gap-2">
            <Badge tone="brand">{user.role.toUpperCase()}</Badge>
            {user.role === 'teacher' && (
              <Badge
                tone={
                  user.teacherApprovalStatus === 'approved'
                    ? 'success'
                    : user.teacherApprovalStatus === 'rejected'
                      ? 'danger'
                      : 'warning'
                }
              >
                Trạng thái GV: {user.teacherApprovalStatus}
              </Badge>
            )}
          </div>
        )}
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        <Card className="p-6 md:col-span-2">
          <div className="mb-4 flex items-center gap-2 border-b border-slate-100 pb-3">
            <User className="h-5 w-5 text-amber-600" />
            <h2 className="text-base font-semibold text-slate-900">Thông tin cá nhân</h2>
          </div>

          <form onSubmit={handleSaveProfile} className="space-y-4">
            <Input
              label="Email tài khoản (Không thể thay đổi)"
              value={user?.email || ''}
              disabled
            />
            <Input
              label="Họ và tên"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              required
            />
            <Input
              label="Số điện thoại liên hệ"
              placeholder="0901234567"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
            />
            <Textarea
              label="Giới thiệu bản thân (Bio)"
              placeholder="Chia sẻ mục tiêu học tập hoặc kinh nghiệm giảng dạy của bạn..."
              value={bio}
              onChange={(e) => setBio(e.target.value)}
            />
            <div className="flex justify-end">
              <Button type="submit" loading={savingProfile}>
                Lưu thay đổi hồ sơ
              </Button>
            </div>
          </form>
        </Card>

        <div className="space-y-6">
          <Card className="p-6">
            <div className="mb-4 flex items-center gap-2 border-b border-slate-100 pb-3">
              <Camera className="h-5 w-5 text-amber-600" />
              <h2 className="text-base font-semibold text-slate-900">Ảnh đại diện</h2>
            </div>

            <div className="mb-4 flex flex-col items-center">
              {avatarUrl ? (
                <img
                  src={avatarUrl}
                  alt={fullName}
                  className="h-20 w-20 rounded-full border border-slate-200 object-cover"
                />
              ) : (
                <div className="flex h-20 w-20 items-center justify-center rounded-full bg-amber-100 text-2xl font-bold text-amber-800">
                  {fullName.charAt(0).toUpperCase() || 'U'}
                </div>
              )}
            </div>

            <form onSubmit={handleSaveAvatar} className="space-y-3">
              <Input
                label="URL Ảnh đại diện"
                placeholder="https://..."
                value={avatarUrl}
                onChange={(e) => setAvatarUrl(e.target.value)}
              />
              <Button type="submit" variant="secondary" loading={savingAvatar} className="w-full">
                Cập nhật Avatar
              </Button>
            </form>
          </Card>

          <Card className="p-6">
            <div className="mb-4 flex items-center gap-2 border-b border-slate-100 pb-3">
              <ShieldCheck className="h-5 w-5 text-amber-600" />
              <h2 className="text-base font-semibold text-slate-900">Đổi mật khẩu</h2>
            </div>

            <form onSubmit={handleChangePassword} className="space-y-3">
              <Input
                label="Mật khẩu hiện tại"
                type="password"
                value={oldPassword}
                onChange={(e) => setOldPassword(e.target.value)}
                required
              />
              <Input
                label="Mật khẩu mới"
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                required
              />
              <Input
                label="Nhập lại mật khẩu mới"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
              <Button type="submit" loading={savingPassword} className="w-full">
                Đổi mật khẩu
              </Button>
            </form>
          </Card>
        </div>
      </div>
    </div>
  );
};
