import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import {
  BookOpen,
  CheckCircle2,
  Download,
  FileText,
  Lock,
  PlayCircle,
  ShoppingCart,
  Star,
} from 'lucide-react';
import { courseApi, CourseDetail, CourseReviewItem, LessonItem } from '../api/courseApi';
import { VietQrPaymentModal } from '@/features/commerce/components/VietQrPaymentModal';
import { VideoPlayer } from '@/shared/components/media/VideoPlayer';
import { httpClient } from '@/shared/api/httpClient';
import { useAuthStore } from '@/shared/store/useAuthStore';
import { ApiResponse, formatVnd, getApiErrorMessage } from '@/shared/types/api';
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Modal,
  Select,
  Spinner,
  Textarea,
} from '@/shared/ui/primitives';

export const CourseDetailPage: React.FC = () => {
  const { courseId } = useParams<{ courseId: string }>();
  const navigate = useNavigate();
  const { user } = useAuthStore();

  const [course, setCourse] = useState<CourseDetail | null>(null);
  const [reviews, setReviews] = useState<CourseReviewItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [previewLesson, setPreviewLesson] = useState<LessonItem | null>(null);
  const [enrollingOrOrdering, setEnrollingOrOrdering] = useState(false);
  const [paymentOrder, setPaymentOrder] = useState<{
    id: string;
    orderCode: number;
    paymentRef: string;
    totalAmountVnd: number;
    expiresAt?: string;
    status?: string;
  } | null>(null);

  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);

  const fetchDetail = async () => {
    if (!courseId) return;
    setLoading(true);
    setError(null);
    try {
      const [detailData, reviewList] = await Promise.all([
        courseApi.getPublicCourseById(courseId),
        courseApi.getCourseReviews(courseId).catch(() => []),
      ]);
      setCourse(detailData);
      setReviews(reviewList);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải thông tin chi tiết khóa học.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDetail();
  }, [courseId]);

  const handleEnrollOrBuy = async () => {
    if (!course) return;
    if (!user) {
      toast.error('Vui lòng đăng nhập để đăng ký khóa học.');
      navigate('/login');
      return;
    }

    setEnrollingOrOrdering(true);
    try {
      if (course.priceVnd === 0) {
        await httpClient.post(`/learning/courses/${course.id}/enroll-free`, {});
        toast.success('Ghi danh khóa học miễn phí thành công!');
        navigate(`/learning/courses/${course.id}`);
      } else {
        const res = await httpClient.post<
          ApiResponse<{
            orderId: string;
            orderCode?: number;
            paymentRef?: string;
            totalAmountVnd?: number;
            expiresAt?: string;
            status?: string;
          }>
        >('/student/orders', {
          courseIds: [course.id],
          paymentMethod: 'PAYOS',
        });
        const orderData = res.data.data;
        toast.success('Đã khởi tạo đơn hàng VietQR!');
        setPaymentOrder({
          id: orderData.orderId,
          orderCode: orderData.orderCode || Date.now() % 1000000,
          paymentRef: orderData.paymentRef || `PAY-${orderData.orderId.substring(0, 8).toUpperCase()}`,
          totalAmountVnd: orderData.totalAmountVnd || course.priceVnd,
          expiresAt: orderData.expiresAt,
          status: orderData.status || 'PENDING',
        });
      }
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể xử lý đăng ký khóa học.'));
    } finally {
      setEnrollingOrOrdering(false);
    }
  };

  const handleSubmitReview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!courseId || !comment.trim()) return;
    setSubmittingReview(true);
    try {
      const created = await courseApi.submitReview(courseId, {
        rating,
        comment: comment.trim(),
      });
      setReviews((prev) => [created, ...prev.filter((r) => r.id !== created.id)]);
      setComment('');
      toast.success('Cảm ơn bạn đã gửi đánh giá khóa học!');
    } catch (err) {
      toast.error(
        getApiErrorMessage(err, 'Chỉ học viên đã ghi danh mới có thể đánh giá khóa học.')
      );
    } finally {
      setSubmittingReview(false);
    }
  };

  if (loading) return <Spinner label="Đang tải thông tin giáo trình..." />;
  if (error || !course) {
    return <ErrorState message={error || 'Khóa học không tồn tại'} onRetry={fetchDetail} />;
  }

  return (
    <div className="space-y-6">
      <Card className="p-6 sm:p-8">
        <div className="grid gap-6 lg:grid-cols-3">
          <div className="space-y-4 lg:col-span-2">
            <div className="flex flex-wrap items-center gap-2">
              <Badge tone="brand">
                Khối lớp: {course.grades?.length ? course.grades.join(', ') : 'Phổ thông'}
              </Badge>
              <Badge tone="warning">
                ★ {Number(course.averageRating || 0).toFixed(1)} ({course.totalReviews} đánh giá)
              </Badge>
            </div>

            <h1 className="text-2xl font-bold text-slate-900 sm:text-3xl">{course.title}</h1>
            <p className="whitespace-pre-line text-sm text-slate-600">{course.description}</p>

            <div className="flex flex-wrap gap-4 pt-2 text-xs font-medium text-slate-600">
              <span>• {course.totalChapters} chương học</span>
              <span>• {course.totalLessons} bài giảng video</span>
              <span>
                • Tổng thời lượng: {Math.max(1, Math.round(course.totalDurationSec / 60))} phút
              </span>
            </div>
          </div>

          <div className="flex flex-col justify-between rounded-xl border border-amber-200 bg-amber-50/50 p-5">
            <div>
              <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                Học phí trọn gói
              </p>
              <p className="mt-1 text-3xl font-extrabold text-slate-900">
                {course.priceVnd === 0 ? 'Miễn phí' : formatVnd(course.priceVnd)}
              </p>
              <ul className="mt-4 space-y-2 text-xs text-slate-700">
                <li className="flex items-center gap-2">
                  <CheckCircle2 className="h-4 w-4 text-emerald-600" />
                  Truy cập trọn đời toàn bộ bài giảng & đề thi
                </li>
                <li className="flex items-center gap-2">
                  <CheckCircle2 className="h-4 w-4 text-emerald-600" />
                  Hỏi đáp trực tiếp cùng Giáo viên bộ môn
                </li>
                <li className="flex items-center gap-2">
                  <CheckCircle2 className="h-4 w-4 text-emerald-600" />
                  Cấp chứng chỉ PDF xác thực khi hoàn thành 100%
                </li>
              </ul>
            </div>

            <div className="mt-6 space-y-2">
              <Button
                onClick={handleEnrollOrBuy}
                loading={enrollingOrOrdering}
                className="w-full"
                size="lg"
              >
                <ShoppingCart className="h-4 w-4" />
                {course.priceVnd === 0 ? 'Ghi danh Học ngay' : 'Mua khóa học / Tạo đơn VietQR'}
              </Button>
              {user && (
                <Button
                  variant="outline"
                  className="w-full"
                  size="sm"
                  onClick={() => navigate(`/learning/courses/${course.id}`)}
                >
                  Vào phòng học (nếu đã sở hữu)
                </Button>
              )}
            </div>
          </div>
        </div>
      </Card>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="space-y-4 lg:col-span-2">
          <h2 className="text-lg font-bold text-slate-900">
            Nội dung Giáo trình ({course.chapters.length} chương)
          </h2>

          {course.chapters.length === 0 ? (
            <EmptyState title="Giáo trình đang được cập nhật" />
          ) : (
            <div className="space-y-3">
              {course.chapters.map((ch) => (
                <Card key={ch.id} className="p-4">
                  <div className="flex items-center justify-between border-b border-slate-100 pb-2.5">
                    <div>
                      <h3 className="text-sm font-bold text-slate-900">
                        Chương {ch.orderIndex}: {ch.title}
                      </h3>
                      {ch.description && (
                        <p className="text-xs text-slate-500">{ch.description}</p>
                      )}
                    </div>
                    <Badge tone="neutral">{ch.lessons.length} bài học</Badge>
                  </div>

                  <div className="mt-2 divide-y divide-slate-100">
                    {ch.lessons.map((lesson) => (
                      <div
                        key={lesson.id}
                        className="py-2.5 text-sm"
                      >
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2.5">
                            {lesson.isFreePreview ? (
                              <PlayCircle className="h-4 w-4 text-emerald-600 shrink-0" />
                            ) : (
                              <Lock className="h-4 w-4 text-slate-400 shrink-0" />
                            )}
                            <span className="font-medium text-slate-800">{lesson.title}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <span className="text-xs text-slate-500">
                              {Math.max(1, Math.round(lesson.durationSec / 60))} phút
                            </span>
                            {lesson.isFreePreview && lesson.videoUrl && (
                              <Button
                                size="sm"
                                variant="secondary"
                                onClick={() => setPreviewLesson(lesson)}
                              >
                                Học thử miễn phí
                              </Button>
                            )}
                          </div>
                        </div>

                        {lesson.documents && lesson.documents.length > 0 && (
                          <div className="mt-2 pl-6 space-y-1.5">
                            {lesson.documents.map((doc) => (
                              <a
                                key={doc.id}
                                href={doc.fileUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="flex items-center justify-between p-2 rounded-lg bg-slate-50 hover:bg-amber-50 border border-slate-200 hover:border-amber-300 transition-colors group"
                              >
                                <div className="flex items-center gap-2 min-w-0">
                                  <FileText className="h-3.5 w-3.5 text-amber-600 shrink-0" />
                                  <span className="text-xs font-medium text-slate-700 group-hover:text-amber-900 truncate">
                                    {doc.title}
                                  </span>
                                  <span className="text-[10px] uppercase font-bold px-1.5 py-0.5 rounded bg-slate-200 text-slate-600">
                                    {doc.documentType}
                                  </span>
                                </div>
                                <span className="text-[11px] text-slate-400 group-hover:text-amber-700 flex items-center gap-1 shrink-0 ml-2">
                                  {(doc.fileSizeBytes / 1024 / 1024).toFixed(1)} MB
                                  <Download className="h-3 w-3" />
                                </span>
                              </a>
                            ))}
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                </Card>
              ))}
            </div>
          )}
        </div>

        <div className="space-y-4">
          <h2 className="text-lg font-bold text-slate-900">Đánh giá từ Học viên</h2>

          {user && (
            <Card className="p-4">
              <form onSubmit={handleSubmitReview} className="space-y-3">
                <Select
                  label="Số sao đánh giá"
                  value={String(rating)}
                  onChange={(e) => setRating(Number(e.target.value))}
                >
                  <option value="5">5 Sao - Xuất sắc</option>
                  <option value="4">4 Sao - Rất tốt</option>
                  <option value="3">3 Sao - Khá</option>
                  <option value="2">2 Sao - Trung bình</option>
                  <option value="1">1 Sao - Cần cải thiện</option>
                </Select>
                <Textarea
                  label="Nhận xét của bạn"
                  placeholder="Chia sẻ trải nghiệm học tập của bạn..."
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  required
                />
                <Button type="submit" size="sm" loading={submittingReview} className="w-full">
                  Gửi đánh giá
                </Button>
              </form>
            </Card>
          )}

          {reviews.length === 0 ? (
            <Card className="p-6 text-center text-xs text-slate-500">
              Chưa có nhận xét nào cho khóa học này.
            </Card>
          ) : (
            <div className="space-y-3">
              {reviews.map((rev) => (
                <Card key={rev.id} className="p-4">
                  <div className="flex items-center justify-between">
                    <span className="inline-flex items-center gap-1 text-xs font-bold text-amber-600">
                      <Star className="h-3.5 w-3.5 fill-amber-400 text-amber-400" />
                      {rev.rating} / 5
                    </span>
                    <span className="text-[11px] text-slate-400">
                      {new Date(rev.createdAt).toLocaleDateString('vi-VN')}
                    </span>
                  </div>
                  <p className="mt-1.5 text-xs text-slate-700">{rev.comment}</p>
                </Card>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Free Lesson Video Preview Modal */}
      <Modal
        open={Boolean(previewLesson)}
        onClose={() => setPreviewLesson(null)}
        title={`Học thử bài giảng: ${previewLesson?.title || ''}`}
      >
        {previewLesson && (
          <div className="space-y-4">
            <VideoPlayer
              videoUrl={previewLesson.videoUrl}
              title={previewLesson.title}
              autoPlay
            />
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs text-slate-500 pt-1">
              <span>Thời lượng bài giảng: {Math.max(1, Math.round(previewLesson.durationSec / 60))} phút</span>
              <Button
                size="sm"
                variant="primary"
                onClick={() => {
                  setPreviewLesson(null);
                  handleEnrollOrBuy();
                }}
              >
                {course.priceVnd === 0 ? 'Ghi danh miễn phí để học tiếp' : 'Mua trọn bộ khóa học'}
              </Button>
            </div>
          </div>
        )}
      </Modal>

      {/* VietQR Payment Modal */}
      <VietQrPaymentModal
        isOpen={Boolean(paymentOrder)}
        onClose={() => setPaymentOrder(null)}
        order={paymentOrder}
        onPaymentSuccess={() => {
          navigate(`/learning/courses/${course.id}`);
        }}
      />
    </div>
  );
};
