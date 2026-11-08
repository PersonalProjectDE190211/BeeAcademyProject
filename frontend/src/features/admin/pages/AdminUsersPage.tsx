import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { CheckCircle2, Lock, Search, Unlock, UserPlus, XCircle } from 'lucide-react';
import { adminApi, AdminUserSummaryDto } from '../api/adminApi';
import { getApiErrorMessage } from '@/shared/types/api';
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Input,
  Modal,
  Select,
  Spinner,
} from '@/shared/ui/primitives';

export const AdminUsersPage: React.FC = () => {
  const [users, setUsers] = useState<AdminUserSummaryDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [keyword, setKeyword] = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const [showProvisionModal, setShowProvisionModal] = useState(false);
  const [email, setEmail] = useState('');
  const [fullName, setFullName] = useState('');
  const [tempPassword, setTempPassword] = useState('Teacher@123456');
  const [contactNote, setContactNote] = useState('');
  const [provisioning, setProvisioning] = useState(false);

  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const page = await adminApi.searchUsers({
        keyword: keyword.trim() || undefined,
        role: roleFilter || undefined,
        status: statusFilter || undefined,
        page: 0,
        size: 50,
      });
      setUsers(page.content || []);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải danh sách người dùng.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [roleFilter, statusFilter]);

  const handleLock = async (userId: string) => {
    try {
      await adminApi.lockUser(userId, 'Vi phạm quy chế sử dụng nền tảng');
      toast.success('Đã khóa tài khoản và thu hồi toàn bộ phiên đăng nhập!');
      fetchUsers();
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể khóa tài khoản.'));
    }
  };

  const handleUnlock = async (userId: string) => {
    try {
      await adminApi.unlockUser(userId, 'Mở khóa sau khi xác minh');
      toast.success('Đã mở khóa tài khoản thành công!');
      fetchUsers();
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể mở khóa tài khoản.'));
    }
  };

  const handleApproveTeacher = async (userId: string) => {
    try {
      await adminApi.approveTeacher(userId);
      toast.success('Đã phê duyệt hồ sơ Giáo viên!');
      fetchUsers();
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể phê duyệt giáo viên.'));
    }
  };

  const handleRejectTeacher = async (userId: string) => {
    try {
      await adminApi.rejectTeacher(userId, 'Chưa đủ chứng chỉ sư phạm theo quy định');
      toast.success('Đã từ chối hồ sơ Giáo viên.');
      fetchUsers();
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể từ chối hồ sơ giáo viên.'));
    }
  };

  const handleProvisionTeacher = async (e: React.FormEvent) => {
    e.preventDefault();
    setProvisioning(true);
    try {
      await adminApi.provisionTeacher({
        email: email.trim(),
        fullName: fullName.trim(),
        temporaryPassword: tempPassword,
        contactNote: contactNote.trim() || undefined,
      });
      toast.success('Đã cấp mới tài khoản Giáo viên trực tiếp thành công!');
      setShowProvisionModal(false);
      setEmail('');
      setFullName('');
      setContactNote('');
      fetchUsers();
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể cấp mới tài khoản giáo viên.'));
    } finally {
      setProvisioning(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">
            Quản trị Người dùng & Thẩm định Giáo viên (UC39, UC40)
          </h1>
          <p className="text-sm text-slate-600">
            Tra cứu tài khoản, khóa/mở khóa bảo mật và cấp phát tài khoản Giáo viên
          </p>
        </div>
        <Button onClick={() => setShowProvisionModal(true)}>
          <UserPlus className="h-4 w-4" />
          Cấp mới Tài khoản Giáo viên
        </Button>
      </div>

      <Card className="p-4">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            fetchUsers();
          }}
          className="grid gap-3 sm:grid-cols-4"
        >
          <Input
            placeholder="Tìm theo họ tên hoặc email..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
          <Select value={roleFilter} onChange={(e) => setRoleFilter(e.target.value)}>
            <option value="">Tất cả vai trò</option>
            <option value="student">Học sinh (STUDENT)</option>
            <option value="teacher">Giáo viên (TEACHER)</option>
            <option value="parent">Phụ huynh (PARENT)</option>
            <option value="admin">Quản trị viên (ADMIN)</option>
          </Select>
          <Select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <option value="">Tất cả trạng thái</option>
            <option value="ACTIVE">Đang hoạt động (ACTIVE)</option>
            <option value="LOCKED">Đang khóa (LOCKED)</option>
          </Select>
          <Button type="submit" variant="secondary">
            <Search className="h-4 w-4" />
            Lọc danh sách
          </Button>
        </form>
      </Card>

      {loading ? (
        <Spinner label="Đang tải danh sách tài khoản người dùng..." />
      ) : error ? (
        <ErrorState message={error} onRetry={fetchUsers} />
      ) : users.length === 0 ? (
        <EmptyState title="Không tìm thấy người dùng nào phù hợp" />
      ) : (
        <div className="space-y-3">
          {users.map((u) => {
            const isLocked = u.status === 'LOCKED';
            const isPendingTeacher =
              u.role.toLowerCase() === 'teacher' &&
              u.teacherApprovalStatus?.toUpperCase() === 'PENDING';

            return (
              <Card key={u.id} className="p-4">
                <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
                  <div className="space-y-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="text-sm font-bold text-slate-900">{u.fullName}</span>
                      <Badge tone="brand">{u.role.toUpperCase()}</Badge>
                      <Badge tone={isLocked ? 'danger' : 'success'}>{u.status}</Badge>
                      {u.role.toLowerCase() === 'teacher' && u.teacherApprovalStatus && (
                        <Badge
                          tone={
                            u.teacherApprovalStatus.toUpperCase() === 'APPROVED'
                              ? 'success'
                              : u.teacherApprovalStatus.toUpperCase() === 'PENDING'
                                ? 'warning'
                                : 'danger'
                          }
                        >
                          GV: {u.teacherApprovalStatus}
                        </Badge>
                      )}
                    </div>
                    <p className="text-xs text-slate-500">
                      Email: {u.email} • Ngày tạo:{' '}
                      {new Date(u.createdAt).toLocaleDateString('vi-VN')}
                    </p>
                  </div>

                  <div className="flex flex-wrap items-center gap-2">
                    {isPendingTeacher && (
                      <>
                        <Button size="sm" onClick={() => handleApproveTeacher(u.id)}>
                          <CheckCircle2 className="h-3.5 w-3.5" />
                          Duyệt GV
                        </Button>
                        <Button
                          size="sm"
                          variant="danger"
                          onClick={() => handleRejectTeacher(u.id)}
                        >
                          <XCircle className="h-3.5 w-3.5" />
                          Từ chối GV
                        </Button>
                      </>
                    )}

                    {u.role.toLowerCase() !== 'admin' && (
                      isLocked ? (
                        <Button
                          size="sm"
                          variant="secondary"
                          onClick={() => handleUnlock(u.id)}
                        >
                          <Unlock className="h-3.5 w-3.5" />
                          Mở khóa
                        </Button>
                      ) : (
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleLock(u.id)}
                        >
                          <Lock className="h-3.5 w-3.5 text-red-600" />
                          Khóa TK
                        </Button>
                      )
                    )}
                  </div>
                </div>
              </Card>
            );
          })}
        </div>
      )}

      <Modal
        open={showProvisionModal}
        onClose={() => setShowProvisionModal(false)}
        title="Cấp mới Tài khoản Giáo viên Trực tiếp"
      >
        <form onSubmit={handleProvisionTeacher} className="space-y-3">
          <Input
            label="Họ và tên Giáo viên"
            placeholder="Thầy Nguyễn Văn B"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            required
          />
          <Input
            label="Địa chỉ Email"
            type="email"
            placeholder="teacher.b@beeacademy.edu.vn"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <Input
            label="Mật khẩu tạm thời (ối thiểu 8 ký tự)"
            value={tempPassword}
            onChange={(e) => setTempPassword(e.target.value)}
            required
          />
          <Input
            label="Ghi chú hồ sơ / Bộ môn phụ trách"
            placeholder="Giáo viên Toán THPT - Đã xác minh bằng cấp"
            value={contactNote}
            onChange={(e) => setContactNote(e.target.value)}
          />
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="ghost" onClick={() => setShowProvisionModal(false)}>
              Hủy
            </Button>
            <Button type="submit" loading={provisioning}>
              Cấp phát Tài khoản
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
