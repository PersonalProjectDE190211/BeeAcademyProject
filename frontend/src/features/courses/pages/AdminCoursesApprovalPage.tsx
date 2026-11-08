import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { CheckCircle, Edit3, XCircle } from 'lucide-react';
import { courseApi, CourseSummary } from '../api/courseApi';
import { formatVnd, getApiErrorMessage } from '@/shared/types/api';
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Modal,
  Spinner,
  Textarea,
} from '@/shared/ui/primitives';

export const AdminCoursesApprovalPage: React.FC = () => {
  const [courses, setCourses] = useState<CourseSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [decisionModal, setDecisionModal] = useState<{
    course: CourseSummary;
    action: 'approve' | 'reject' | 'revision';
  } | null>(null);
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const fetchPending = async () => {
    setLoading(true);
    setError(null);
    try {
      const list = await courseApi.getPendingCourses();
      setCourses(list);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải danh sách khóa học chờ duyệt.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPending();
  }, []);

  const handleConfirmDecision = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!decisionModal) return;
    setSubmitting(true);
    try {
      const { course, action } = decisionModal;
      if (action === 'approve') {
        await courseApi.approveCourse(course.id, comment.trim() || 'Đạt chuẩn chất lượng BeeAcademy');
        toast.success(`Đã phê duyệt xuất bản khóa học "${course.title}"!`);
      } else if (action === 'reject') {
        await courseApi.rejectCourse(
          course.id,
          comment.trim() || 'Nội dung chưa phù hợp tiêu chuẩn xuất bản'
        );
        toast.success(`Đã từ chối khóa học "${course.title}".`);
      } else {
        await courseApi.requestRevision(
          course.id,
          comment.trim() || 'Vui lòng bổ sung thêm mô tả và bài giảng chi tiết'
        );
        toast.success(`Đã gửi yêu cầu chỉnh sửa cho khóa học "${course.title}".`);
      }
      setDecisionModal(null);
      setComment('');
      fetchPending();
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể thực hiện quyết định kiểm duyệt.'));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <Spinner label="Đang tải hàng đợi kiểm duyệt khóa học..." />;
  if (error) return <ErrorState message={error} onRetry={fetchPending} />;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">
          Kiểm duyệt & Xuất bản Khóa học (UC41)
        </h1>
        <p className="text-sm text-slate-600">
          Thẩm định chất lượng giáo trình của Giáo viên trước khi xuất bản công khai lên BeeAcademy
        </p>
      </div>

      {courses.length === 0 ? (
        <EmptyState
          title="Không có khóa học nào đang chờ duyệt"
          description="Tất cả các khóa học do giáo viên gửi lên đều đã được xử lý."
        />
      ) : (
        <div className="grid gap-4">
          {courses.map((course) => (
            <Card key={course.id} className="p-5">
              <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <Badge tone="warning">{course.status}</Badge>
                    <span className="text-xs text-slate-500">
                      Ngày tạo: {new Date(course.createdAt).toLocaleDateString('vi-VN')}
                    </span>
                  </div>
                  <h3 className="text-base font-bold text-slate-900">{course.title}</h3>
                  <p className="text-xs text-slate-600">{course.description}</p>
                  <div className="flex flex-wrap gap-4 pt-1 text-xs font-medium text-slate-500">
                    <span>Học phí đề xuất: {formatVnd(course.priceVnd)}</span>
                    <span>• {course.totalChapters} chương</span>
                    <span>• {course.totalLessons} bài giảng</span>
                  </div>
                </div>

                <div className="flex flex-wrap items-center gap-2">
                  <Button
                    size="sm"
                    variant="primary"
                    onClick={() => {
                      setDecisionModal({ course, action: 'approve' });
                      setComment('Đạt tiêu chuẩn nội dung K-12 BeeAcademy.');
                    }}
                  >
                    <CheckCircle className="h-4 w-4" />
                    Phê duyệt (Publish)
                  </Button>
                  <Button
                    size="sm"
                    variant="secondary"
                    onClick={() => {
                      setDecisionModal({ course, action: 'revision' });
                      setComment('');
                    }}
                  >
                    <Edit3 className="h-4 w-4" />
                    Yêu cầu sửa
                  </Button>
                  <Button
                    size="sm"
                    variant="danger"
                    onClick={() => {
                      setDecisionModal({ course, action: 'reject' });
                      setComment('');
                    }}
                  >
                    <XCircle className="h-4 w-4" />
                    Từ chối
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}

      <Modal
        open={Boolean(decisionModal)}
        onClose={() => setDecisionModal(null)}
        title={
          decisionModal?.action === 'approve'
            ? 'Xác nhận Phê duyệt Khóa học'
            : decisionModal?.action === 'revision'
              ? 'Yêu cầu Giáo viên Chỉnh sửa Bổ sung'
              : 'Từ chối Xuất bản Khóa học'
        }
      >
        <form onSubmit={handleConfirmDecision} className="space-y-4">
          <p className="text-xs text-slate-600">
            Khóa học: <strong className="text-slate-900">{decisionModal?.course.title}</strong>
          </p>
          <Textarea
            label="Ghi chú / Nhận xét gửi tới Giáo viên"
            rows={3}
            placeholder="Nhập nhận xét thẩm định hoặc lý do cần chỉnh sửa..."
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            required={decisionModal?.action !== 'approve'}
          />
          <div className="flex justify-end gap-2">
            <Button type="button" variant="ghost" onClick={() => setDecisionModal(null)}>
              Hủy
            </Button>
            <Button
              type="submit"
              loading={submitting}
              variant={decisionModal?.action === 'reject' ? 'danger' : 'primary'}
            >
              Xác nhận
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
