import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  AlertTriangle,
  BookOpen,
  DollarSign,
  ShieldCheck,
  Users,
} from 'lucide-react';
import { adminApi, AdminDashboardDto } from '../api/adminApi';
import { formatVnd, getApiErrorMessage } from '@/shared/types/api';
import {
  Badge,
  Button,
  Card,
  ErrorState,
  Spinner,
} from '@/shared/ui/primitives';

export const AdminDashboardPage: React.FC = () => {
  const [data, setData] = useState<AdminDashboardDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchDashboard = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await adminApi.getDashboardSummary();
      setData(res);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải số liệu thống kê hệ thống.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  if (loading) return <Spinner label="Đang tổng hợp số liệu thống kê toàn hệ thống..." />;
  if (error || !data) {
    return <ErrorState message={error || 'Không có dữ liệu'} onRetry={fetchDashboard} />;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">
            Trung tâm Điều hành Quản trị BeeAcademy (UC38)
          </h1>
          <p className="text-sm text-slate-600">
            Báo cáo thời gian thực về Doanh thu GMV, Người dùng, Khóa học và Hỗ trợ
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Link to="/admin/users">
            <Button size="sm" variant="secondary">
              Quản lý Người dùng
            </Button>
          </Link>
          <Link to="/admin/courses">
            <Button size="sm" variant="outline">
              Duyệt Khóa học ({data.pendingCourseApprovals})
            </Button>
          </Link>
          <Link to="/admin/settings">
            <Button size="sm" variant="outline">
              Cấu hình & Audit Logs
            </Button>
          </Link>
        </div>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card className="p-5">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500">Tổng GMV Toàn sàn</span>
            <DollarSign className="h-4 w-4 text-amber-600" />
          </div>
          <p className="mt-2 text-2xl font-extrabold text-slate-900">
            {formatVnd(data.totalGmv)}
          </p>
          <p className="mt-1 text-xs text-slate-500">
            Phí sàn (20%): <strong>{formatVnd(data.platformFee)}</strong>
          </p>
        </Card>

        <Card className="p-5">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500">Tổng Người dùng</span>
            <Users className="h-4 w-4 text-emerald-600" />
          </div>
          <p className="mt-2 text-2xl font-extrabold text-slate-900">{data.totalUsers}</p>
          <p className="mt-1 text-xs text-slate-500">
            HS: {data.studentCount} • GV: {data.teacherCount} • PH: {data.parentCount}
          </p>
        </Card>

        <Card className="p-5">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500">Hệ sinh thái Khóa học</span>
            <BookOpen className="h-4 w-4 text-sky-600" />
          </div>
          <p className="mt-2 text-2xl font-extrabold text-slate-900">
            {data.publishedCourses} / {data.totalCourses}
          </p>
          <p className="mt-1 text-xs text-slate-500">
            Tổng lượt ghi danh học tập: <strong>{data.totalEnrollments}</strong>
          </p>
        </Card>

        <Card className="p-5">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500">Cần xử lý ngay</span>
            <AlertTriangle className="h-4 w-4 text-red-600" />
          </div>
          <div className="mt-2 flex flex-wrap gap-2">
            <Badge tone="warning">GV chờ duyệt: {data.pendingTeacherApprovals}</Badge>
            <Badge tone="info">Khóa học chờ duyệt: {data.pendingCourseApprovals}</Badge>
            <Badge tone="danger">Khiếu nại mở: {data.openComplaints}</Badge>
          </div>
        </Card>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card className="p-5 space-y-3">
          <div className="flex items-center gap-2 border-b border-slate-100 pb-3">
            <ShieldCheck className="h-5 w-5 text-amber-600" />
            <h2 className="text-base font-bold text-slate-900">
              Trạng thái Tài khoản & An toàn Hệ thống
            </h2>
          </div>
          <div className="grid grid-cols-2 gap-3 text-xs">
            <div className="rounded-xl bg-emerald-50 p-3.5">
              <p className="text-emerald-700">Tài khoản Hoạt động (ACTIVE)</p>
              <p className="mt-1 text-xl font-bold text-emerald-900">{data.activeUsers}</p>
            </div>
            <div className="rounded-xl bg-red-50 p-3.5">
              <p className="text-red-700">Tài khoản Đang khóa (LOCKED)</p>
              <p className="mt-1 text-xl font-bold text-red-900">{data.lockedUsers}</p>
            </div>
          </div>
        </Card>

        <Card className="p-5 space-y-3">
          <div className="flex items-center gap-2 border-b border-slate-100 pb-3">
            <DollarSign className="h-5 w-5 text-emerald-600" />
            <h2 className="text-base font-bold text-slate-900">
              Cơ cấu Phân bổ Doanh thu 80/20
            </h2>
          </div>
          <div className="grid grid-cols-2 gap-3 text-xs">
            <div className="rounded-xl bg-amber-50 p-3.5">
              <p className="text-amber-800">Doanh thu Giáo viên (80%)</p>
              <p className="mt-1 text-xl font-bold text-amber-950">
                {formatVnd(data.teacherAmount)}
              </p>
            </div>
            <div className="rounded-xl bg-sky-50 p-3.5">
              <p className="text-sky-800">Phí vận hành BeeAcademy (20%)</p>
              <p className="mt-1 text-xl font-bold text-sky-950">
                {formatVnd(data.platformFee)}
              </p>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};
