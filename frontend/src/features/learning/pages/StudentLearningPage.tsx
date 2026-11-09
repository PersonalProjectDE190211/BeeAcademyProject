
import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Award, BookOpen, CheckCircle2, PlayCircle } from 'lucide-react';
import { EnrollmentSummary, learningApi } from '../api/learningApi';
import { getApiErrorMessage } from '@/shared/types/api';
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Spinner,
} from '@/shared/ui/primitives';

export const StudentLearningPage: React.FC = () => {
  const [enrollments, setEnrollments] = useState<EnrollmentSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchEnrollments = async () => {
    setLoading(true);
    setError(null);
    try {
      const list = await learningApi.getMyCourses();
      setEnrollments(list);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải danh sách khóa học đang theo học.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEnrollments();
  }, []);

  if (loading) return <Spinner label="Đang tải không gian học tập của bạn..." />;
  if (error) return <ErrorState message={error} onRetry={fetchEnrollments} />;

  return (
    <div className="space-y-6">
      <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Khóa học của tôi (UC13)</h1>
          <p className="text-sm text-slate-600">
            Theo dõi tiến độ hoàn thành bài giảng và nhận Chứng chỉ số khi đạt 100%
          </p>
        </div>
        <Link to="/learning/certificates">
          <Button variant="secondary" size="sm">
            <Award className="h-4 w-4" />
            Xem Chứng chỉ số của tôi
          </Button>
        </Link>
      </div>

      {enrollments.length === 0 ? (
        <EmptyState
          title="Bạn chưa đăng ký khóa học nào"
          description="Khám phá kho bài giảng Phổ thông K-12 để bắt đầu hành trình học tập ngay hôm nay."
          action={
            <Link to="/courses">
              <Button>Khám phá Khóa học</Button>
            </Link>
          }
        />
      ) : (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {enrollments.map((item) => {
            const pct = Math.min(100, Math.round(item.progressPercent || 0));
            return (
              <Card key={item.id} className="flex flex-col justify-between p-5">
                <div>
                  <div className="flex items-center justify-between gap-2">
                    <Badge tone={item.completed ? 'success' : 'brand'}>
                      {item.completed ? 'Đã hoàn thành 100%' : `Đang học (${pct}%)`}
                    </Badge>
                    <span className="text-xs text-slate-400">
                      {item.completedLessons}/{item.totalLessons} bài
                    </span>
                  </div>

                  <h3 className="mt-3 line-clamp-2 text-base font-bold text-slate-900">
                    {item.courseTitle}
                  </h3>

                  <div className="mt-4">
                    <div className="mb-1 flex justify-between text-xs font-medium text-slate-600">
                      <span>Tiến độ hoàn thành</span>
                      <span>{pct}%</span>
                    </div>
                    <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100">
                      <div
                        className="h-full rounded-full bg-amber-500 transition-all"
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                  </div>
                </div>

                <div className="mt-5 flex items-center gap-2 border-t border-slate-100 pt-4">
                  <Link to={`/learning/courses/${item.courseId}`} className="flex-1">
                    <Button className="w-full" size="sm">
                      <PlayCircle className="h-4 w-4" />
                      {item.completed ? 'Ôn tập lại' : 'Tiếp tục học'}
                    </Button>
                  </Link>
                  {item.completed && (
                    <Link to="/learning/certificates">
                      <Button variant="outline" size="sm" title="Đã cấp chứng chỉ">
                        <CheckCircle2 className="h-4 w-4 text-emerald-600" />
                      </Button>
                    </Link>
                  )}
                </div>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
};
