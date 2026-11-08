import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { BarChart3, ShieldAlert, UserCheck, UserPlus, UserX } from 'lucide-react';
import {
  familyApi,
  ParentLinkDto,
  StudentProgressReportDto,
} from '../api/familyApi';
import { getApiErrorMessage } from '@/shared/types/api';
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Input,
  Spinner,
} from '@/shared/ui/primitives';

export const ParentPortalPage: React.FC = () => {
  const [links, setLinks] = useState<ParentLinkDto[]>([]);
  const [selectedReport, setSelectedReport] = useState<StudentProgressReportDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [studentEmail, setStudentEmail] = useState('');
  const [inviting, setInviting] = useState(false);
  const [loadingReportId, setLoadingReportId] = useState<string | null>(null);

  const fetchLinks = async () => {
    setLoading(true);
    setError(null);
    try {
      const list = await familyApi.getParentLinks();
      setLinks(list);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải danh sách liên kết phụ huynh - học sinh.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLinks();
  }, []);

  const handleInvite = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!studentEmail.trim()) return;
    setInviting(true);
    try {
      const created = await familyApi.inviteStudent(studentEmail.trim());
      setLinks((prev) => [created, ...prev]);
      setStudentEmail('');
      toast.success(
        `Đã tạo lời mời liên kết! Mã mời 6 ký tự của con là: ${created.inviteToken}`
      );
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể gửi lời mời liên kết.'));
    } finally {
      setInviting(false);
    }
  };

  const handleViewProgress = async (studentId: string) => {
    setLoadingReportId(studentId);
    try {
      const report = await familyApi.getStudentProgress(studentId);
      setSelectedReport(report);
      toast.success('Đã tải báo cáo học tập (Đã ghi vết kiểm toán Privacy Audit Log).');
    } catch (err) {
      toast.error(
        getApiErrorMessage(
          err,
          'Chỉ có thể xem tiến độ khi học sinh đã đồng thuận liên kết (ACTIVE).'
        )
      );
    } finally {
      setLoadingReportId(null);
    }
  };

  const handleUnlink = async (linkId: string) => {
    try {
      const updated = await familyApi.parentUnlink(linkId, 'Phụ huynh chủ động hủy liên kết');
      setLinks((prev) => prev.map((l) => (l.id === linkId ? updated : l)));
      setSelectedReport(null);
      toast.success('Đã hủy liên kết tài khoản học sinh.');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể hủy liên kết.'));
    }
  };

  if (loading) return <Spinner label="Đang tải Cổng Phụ huynh..." />;
  if (error) return <ErrorState message={error} onRetry={fetchLinks} />;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">
          Cổng Phụ huynh & Giám sát Học tập (UC24 - UC29)
        </h1>
        <p className="text-sm text-slate-600">
          Liên kết tài khoản với con qua mã bảo mật 6 ký tự và theo dõi tiến độ học tập minh bạch
        </p>
      </div>

      <Card className="p-5">
        <h2 className="text-sm font-bold text-slate-900">
          Gửi lời mời Liên kết Tài khoản cho Con (Hiệu lực 72 giờ)
        </h2>
        <form onSubmit={handleInvite} className="mt-3 flex flex-col gap-3 sm:flex-row">
          <div className="flex-1">
            <Input
              type="email"
              placeholder="Nhập địa chỉ email tài khoản học sinh của con..."
              value={studentEmail}
              onChange={(e) => setStudentEmail(e.target.value)}
              required
            />
          </div>
          <Button type="submit" loading={inviting}>
            <UserPlus className="h-4 w-4" />
            Tạo Mã mời Liên kết
          </Button>
        </form>
      </Card>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="space-y-3 lg:col-span-2">
          <h2 className="text-base font-bold text-slate-900">
            Danh sách Tài khoản Con ({links.length})
          </h2>

          {links.length === 0 ? (
            <EmptyState
              title="Chưa có liên kết học sinh nào"
              description="Nhập email của con ở khung phía trên để nhận mã mời 6 ký tự."
            />
          ) : (
            <div className="space-y-3">
              {links.map((link) => (
                <Card key={link.id} className="p-4">
                  <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold text-slate-900">
                          {link.studentEmail}
                        </span>
                        <Badge
                          tone={
                            link.status === 'ACTIVE'
                              ? 'success'
                              : link.status === 'PENDING'
                                ? 'warning'
                                : 'neutral'
                          }
                        >
                          {link.status}
                        </Badge>
                      </div>
                      {link.inviteToken && link.status === 'PENDING' && (
                        <p className="text-xs text-amber-800">
                          Mã mời 6 ký tự gửi cho con nhập:{' '}
                          <strong className="rounded bg-amber-100 px-2 py-0.5 font-mono text-sm">
                            {link.inviteToken}
                          </strong>
                        </p>
                      )}
                    </div>

                    <div className="flex items-center gap-2">
                      {link.status === 'ACTIVE' && (
                        <Button
                          size="sm"
                          loading={loadingReportId === link.studentId}
                          onClick={() => handleViewProgress(link.studentId)}
                        >
                          <BarChart3 className="h-4 w-4" />
                          Xem tiến độ học
                        </Button>
                      )}
                      {(link.status === 'ACTIVE' || link.status === 'PENDING') && (
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleUnlink(link.id)}
                        >
                          <UserX className="h-4 w-4 text-red-600" />
                          Hủy liên kết
                        </Button>
                      )}
                    </div>
                  </div>
                </Card>
              ))}
            </div>
          )}
        </div>

        <div className="space-y-4">
          <h2 className="text-base font-bold text-slate-900">Báo cáo Tiến độ của Con</h2>
          {!selectedReport ? (
            <Card className="p-6 text-center text-xs text-slate-500">
              Chọn <strong>Xem tiến độ học</strong> ở một tài khoản con đã liên kết (ACTIVE) để xem chi tiết.
            </Card>
          ) : (
            <Card className="p-5 space-y-4">
              <div className="flex items-center gap-2 border-b border-slate-100 pb-3">
                <UserCheck className="h-5 w-5 text-emerald-600" />
                <div>
                  <p className="text-sm font-bold text-slate-900">
                    {selectedReport.studentEmail}
                  </p>
                  <p className="text-xs text-slate-500">
                    Đang học: {selectedReport.enrolledCoursesCount} khóa • Hoàn thành:{' '}
                    {selectedReport.completedCoursesCount} khóa
                  </p>
                </div>
              </div>

              <div className="space-y-3">
                {selectedReport.courses.map((c) => {
                  const pct = Math.round(c.progressPercent || 0);
                  return (
                    <div key={c.courseId} className="rounded-xl bg-slate-50 p-3 text-xs">
                      <div className="flex justify-between font-semibold text-slate-800">
                        <span>{c.courseTitle}</span>
                        <span>{pct}%</span>
                      </div>
                      <div className="mt-1.5 h-2 w-full overflow-hidden rounded-full bg-slate-200">
                        <div
                          className="h-full rounded-full bg-emerald-500"
                          style={{ width: `${pct}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>

              <div className="rounded-xl border border-amber-200 bg-amber-50/60 p-3 text-[11px] text-amber-900">
                <div className="flex items-center gap-1.5 font-semibold">
                  <ShieldAlert className="h-3.5 w-3.5 text-amber-700" />
                  Privacy Audit Log
                </div>
                <p className="mt-0.5">
                  Mọi lượt tra cứu báo cáo học tập đều được ghi vết kiểm toán bảo mật để bảo vệ quyền riêng tư của học sinh.
                </p>
              </div>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
};
