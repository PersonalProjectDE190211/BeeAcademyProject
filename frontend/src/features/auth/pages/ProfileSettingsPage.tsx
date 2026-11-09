import React, { useEffect, useRef, useState } from 'react';
import toast from 'react-hot-toast';
import { ShieldCheck, User, Camera, Upload, Trash2, X, Check } from 'lucide-react';
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

  // Avatar Upload States
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [savingAvatar, setSavingAvatar] = useState(false);
  const [isDragging, setIsDragging] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

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

  // Clean up Object URL when component unmounts or preview changes
  useEffect(() => {
    return () => {
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

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

  const validateAndSelectFile = (file: File) => {
    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      toast.error('Chỉ chấp nhận file ảnh định dạng JPG, PNG hoặc WEBP.');
      return;
    }
    const maxBytes = 5 * 1024 * 1024; // 5MB
    if (file.size > maxBytes) {
      toast.error('Dung lượng ảnh không được vượt quá 5MB.');
      return;
    }

    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
    }
    const objectUrl = URL.createObjectURL(file);
    setSelectedFile(file);
    setPreviewUrl(objectUrl);
  };

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      validateAndSelectFile(files[0]);
    }
    // Reset file input so re-selecting same file triggers change
    e.target.value = '';
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    const files = e.dataTransfer.files;
    if (files && files.length > 0) {
      validateAndSelectFile(files[0]);
    }
  };

  const handleCancelSelectedAvatar = () => {
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
    }
    setSelectedFile(null);
    setPreviewUrl(null);
  };

  const handleUploadAvatar = async () => {
    if (!selectedFile) return;

    setSavingAvatar(true);
    try {
      const updated = await authApi.uploadAvatarFile(selectedFile);
      updateUser(updated);
      setAvatarUrl(updated.avatarUrl || '');
      handleCancelSelectedAvatar();
      toast.success('Đã cập nhật ảnh đại diện thành công!');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể tải lên ảnh đại diện. Vui lòng thử lại.'));
    } finally {
      setSavingAvatar(false);
    }
  };

  const handleRemoveAvatar = async () => {
    if (!avatarUrl) return;
    if (!window.confirm('Bạn có chắc chắn muốn xóa ảnh đại diện hiện tại?')) return;

    setSavingAvatar(true);
    try {
      const updated = await authApi.deleteAvatar();
      updateUser(updated);
      setAvatarUrl('');
      handleCancelSelectedAvatar();
      toast.success('Đã xóa ảnh đại diện thành công!');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể xóa ảnh đại diện.'));
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

  const displayedAvatar = previewUrl || avatarUrl;
  const initialLetter = fullName.trim().charAt(0).toUpperCase() || 'B';

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
        {/* Cột trái: Form thông tin cá nhân */}
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

        {/* Cột phải: Avatar Upload & Đổi mật khẩu */}
        <div className="space-y-6">
          {/* Card Tải Ảnh Đại Diện Chuẩn File Upload */}
          <Card className="p-6">
            <div className="mb-4 flex items-center gap-2 border-b border-slate-100 pb-3">
              <Camera className="h-5 w-5 text-amber-600" />
              <h2 className="text-base font-semibold text-slate-900">Ảnh đại diện</h2>
            </div>

            {/* Input file ẩn */}
            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/png,image/webp"
              className="hidden"
              onChange={handleFileInputChange}
            />

            {/* Vùng Avatar tương tác kéo thả & click */}
            <div className="flex flex-col items-center">
              <div
                onClick={() => fileInputRef.current?.click()}
                onDragOver={(e) => {
                  e.preventDefault();
                  setIsDragging(true);
                }}
                onDragLeave={() => setIsDragging(false)}
                onDrop={handleDrop}
                title="Nhấn để tải ảnh từ máy tính hoặc kéo thả ảnh vào đây"
                className={`group relative h-32 w-32 cursor-pointer rounded-full overflow-hidden transition-all duration-200 shadow-md ${
                  isDragging
                    ? 'ring-4 ring-amber-500 scale-105'
                    : 'ring-4 ring-amber-500/20 hover:ring-amber-500/50 hover:shadow-lg'
                }`}
              >
                {displayedAvatar ? (
                  <img
                    src={displayedAvatar}
                    alt={fullName}
                    className="h-full w-full aspect-square object-cover object-top transition-transform duration-200 group-hover:scale-105"
                  />
                ) : (
                  <div className="flex h-full w-full items-center justify-center bg-gradient-to-tr from-amber-500 to-orange-500 text-3xl font-extrabold text-white">
                    {initialLetter}
                  </div>
                )}

                {/* Overlay khi hover */}
                <div className="absolute inset-0 flex flex-col items-center justify-center bg-black/45 opacity-0 transition-opacity duration-200 group-hover:opacity-100 text-white">
                  <Camera className="h-6 w-6 mb-1 text-white" />
                  <span className="text-[11px] font-semibold tracking-wide">Đổi ảnh</span>
                </div>
              </div>

              {/* Thông tin file khi vừa chọn xem trước */}
              {selectedFile ? (
                <div className="mt-3.5 flex flex-col items-center text-center w-full animate-in fade-in duration-200">
                  <span className="text-xs font-semibold text-amber-700 bg-amber-50 px-2.5 py-1 rounded-full border border-amber-200 max-w-[200px] truncate">
                    {selectedFile.name}
                  </span>
                  <span className="text-[11px] text-slate-500 mt-0.5">
                    ({(selectedFile.size / (1024 * 1024)).toFixed(2)} MB)
                  </span>

                  {/* Nút hành động khi có file preview */}
                  <div className="mt-3 flex w-full gap-2">
                    <Button
                      type="button"
                      variant="primary"
                      loading={savingAvatar}
                      onClick={handleUploadAvatar}
                      className="flex-1 text-xs py-2 gap-1.5"
                    >
                      <Check className="h-4 w-4" />
                      Lưu ảnh
                    </Button>
                    <Button
                      type="button"
                      variant="secondary"
                      disabled={savingAvatar}
                      onClick={handleCancelSelectedAvatar}
                      className="text-xs py-2 px-3 text-slate-600 hover:text-slate-800"
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              ) : (
                <div className="mt-3.5 flex flex-col items-center text-center w-full">
                  <Button
                    type="button"
                    variant="secondary"
                    onClick={() => fileInputRef.current?.click()}
                    disabled={savingAvatar}
                    className="w-full text-xs py-2 gap-1.5"
                  >
                    <Upload className="h-3.5 w-3.5" />
                    Tải ảnh từ máy
                  </Button>

                  {avatarUrl && (
                    <button
                      type="button"
                      onClick={handleRemoveAvatar}
                      disabled={savingAvatar}
                      className="mt-2 flex items-center gap-1 text-[11px] font-medium text-slate-400 hover:text-red-600 transition cursor-pointer"
                    >
                      <Trash2 className="h-3 w-3" />
                      Xóa ảnh hiện tại
                    </button>
                  )}

                  <p className="mt-2 text-[11px] text-slate-500 leading-normal">
                    Hỗ trợ JPG, PNG, WEBP (Tối đa 5MB).
                    <br />
                    Kéo & thả ảnh trực tiếp vào vòng tròn.
                  </p>
                </div>
              )}
            </div>
          </Card>

          {/* Card Đổi mật khẩu */}
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
