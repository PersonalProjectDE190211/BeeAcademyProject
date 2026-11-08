import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import {
  CheckCircle2,
  ClipboardCheck,
  MessageSquare,
  Play,
  Send,
  ShieldCheck,
} from 'lucide-react';
import { courseApi, CourseDetail } from '@/features/courses/api/courseApi';
import {
  learningApi,
  LearningLesson,
  QaThreadItem,
} from '../api/learningApi';
import { getApiErrorMessage } from '@/shared/types/api';
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Input,
  Spinner,
  Textarea,
} from '@/shared/ui/primitives';

export const CourseLearningRoomPage: React.FC = () => {
  const { courseId } = useParams<{ courseId: string }>();

  const [course, setCourse] = useState<CourseDetail | null>(null);
  const [activeLesson, setActiveLesson] = useState<LearningLesson | null>(null);
  const [discussions, setDiscussions] = useState<QaThreadItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [trackingHeartbeat, setTrackingHeartbeat] = useState(false);

  // Q&A form
  const [questionTitle, setQuestionTitle] = useState('');
  const [questionContent, setQuestionContent] = useState('');
  const [asking, setAsking] = useState(false);

  const [replyMap, setReplyMap] = useState<Record<string, string>>({});
  const [replyingThreadId, setReplyingThreadId] = useState<string | null>(null);

  const loadCourseAndFirstLesson = async () => {
    if (!courseId) return;
    setLoading(true);
    setError(null);
    try {
      const detail = await courseApi.getPublicCourseById(courseId);
      setCourse(detail);

      const firstLesson = detail.chapters?.[0]?.lessons?.[0];
      if (firstLesson) {
        await selectLesson(courseId, firstLesson.id);
      }
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải phòng học trực tuyến.'));
    } finally {
      setLoading(false);
    }
  };

  const selectLesson = async (cId: string, lessonId: string) => {
    try {
      const [lessonData, threadList] = await Promise.all([
        learningApi.getLearningLesson(cId, lessonId),
        learningApi.getLessonDiscussions(lessonId).catch(() => []),
      ]);
      setActiveLesson(lessonData);
      setDiscussions(threadList);
    } catch (err) {
      toast.error(
        getApiErrorMessage(err, 'Bạn cần ghi danh khóa học để mở khóa bài giảng này.')
      );
    }
  };

  useEffect(() => {
    loadCourseAndFirstLesson();
  }, [courseId]);

  const handleRecordHeartbeat = async (completeFullSegment: boolean) => {
    if (!courseId || !activeLesson) return;
    setTrackingHeartbeat(true);
    try {
      const duration = Math.max(60, activeLesson.durationSec || 300);
      const startSec = completeFullSegment ? 0 : activeLesson.lastPositionSec;
      const endSec = completeFullSegment
        ? duration
        : Math.min(duration, startSec + Math.ceil(duration * 0.3));

      const progress = await learningApi.trackProgress(courseId, activeLesson.lessonId, {
        startSec,
        endSec,
        currentPositionSec: endSec,
      });

      setActiveLesson((prev) =>
        prev
          ? {
              ...prev,
              lastPositionSec: progress.lastPositionSec,
              watchPercent: progress.watchPercent,
              isCompleted: progress.isCompleted,
            }
          : null
      );

      if (progress.courseCompleted) {
        toast.success(
          'Chúc mừng! Bạn đã hoàn thành 100% khóa học và được cấp Chứng chỉ số PDF!'
        );
      } else if (progress.isCompleted) {
        toast.success(
          `Đã hoàn thành bài học (${Math.round(progress.watchPercent)}%)! Tiến độ khóa học: ${Math.round(progress.courseProgressPercent)}%`
        );
      } else {
        toast.success(`Đã ghi nhận tiến độ xem video: ${Math.round(progress.watchPercent)}%`);
      }
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể cập nhật tiến độ xem bài giảng.'));
    } finally {
      setTrackingHeartbeat(false);
    }
  };

  const handleAskQuestion = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!courseId || !activeLesson || !questionTitle.trim() || !questionContent.trim()) return;
    setAsking(true);
    try {
      const created = await learningApi.askQuestion(courseId, activeLesson.lessonId, {
        title: questionTitle.trim(),
        content: questionContent.trim(),
      });
      setDiscussions((prev) => [created, ...prev]);
      setQuestionTitle('');
      setQuestionContent('');
      toast.success('Đã gửi câu hỏi thảo luận tới Giáo viên!');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể gửi câu hỏi thảo luận.'));
    } finally {
      setAsking(false);
    }
  };

  const handleReply = async (threadId: string) => {
    const content = replyMap[threadId]?.trim();
    if (!content) return;
    setReplyingThreadId(threadId);
    try {
      const updated = await learningApi.replyQuestion(threadId, content);
      setDiscussions((prev) => prev.map((t) => (t.id === threadId ? updated : t)));
      setReplyMap((prev) => ({ ...prev, [threadId]: '' }));
      toast.success('Đã gửi phản hồi!');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể gửi phản hồi.'));
    } finally {
      setReplyingThreadId(null);
    }
  };

  if (loading) return <Spinner label="Đang chuẩn bị phòng học..." />;
  if (error || !course) {
    return <ErrorState message={error || 'Không tìm thấy khóa học'} onRetry={loadCourseAndFirstLesson} />;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-xl font-bold text-slate-900 sm:text-2xl">{course.title}</h1>
          <p className="text-xs text-slate-500">
            Phòng học Video chống tua (Anti-Skip ≥ 80%) & Thảo luận Hỏi đáp trực tiếp (UC14, UC15, UC21)
          </p>
        </div>
        {activeLesson && (
          <Link to={`/learning/lessons/${activeLesson.lessonId}/quiz`}>
            <Button variant="secondary" size="sm">
              <ClipboardCheck className="h-4 w-4" />
              Làm bài Kiểm tra (Quiz) của bài học này
            </Button>
          </Link>
        )}
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="space-y-6 lg:col-span-2">
          {activeLesson ? (
            <Card className="overflow-hidden p-0">
              <div className="bg-slate-900 p-6 text-white">
                <div className="flex items-center justify-between gap-2">
                  <Badge tone={activeLesson.isCompleted ? 'success' : 'warning'}>
                    {activeLesson.isCompleted
                      ? 'Đã hoàn thành (≥ 80%)'
                      : `Đã xem ${Math.round(activeLesson.watchPercent || 0)}%`}
                  </Badge>
                  <span className="text-xs text-slate-400">
                    Thời lượng: {Math.round(activeLesson.durationSec / 60)} phút
                  </span>
                </div>

                <h2 className="mt-3 text-lg font-bold">{activeLesson.title}</h2>
                <p className="mt-1 break-all text-xs text-amber-300">
                  Nguồn phát Video: {activeLesson.videoUrl}
                </p>

                <div className="mt-6 flex flex-wrap items-center gap-3 border-t border-slate-800 pt-4">
                  <Button
                    size="sm"
                    variant="secondary"
                    loading={trackingHeartbeat}
                    onClick={() => handleRecordHeartbeat(false)}
                  >
                    <Play className="h-4 w-4" />
                    Gửi Heartbeat (+30% phân đoạn xem)
                  </Button>
                  <Button
                    size="sm"
                    variant="primary"
                    loading={trackingHeartbeat}
                    onClick={() => handleRecordHeartbeat(true)}
                  >
                    <ShieldCheck className="h-4 w-4" />
                    Xem trọn vẹn bài giảng (Đạt chuẩn ≥ 80%)
                  </Button>
                </div>
              </div>
            </Card>
          ) : (
            <EmptyState title="Hãy chọn một bài giảng từ danh sách bên phải để bắt đầu học" />
          )}

          {activeLesson && (
            <Card className="p-5">
              <div className="mb-4 flex items-center gap-2 border-b border-slate-100 pb-3">
                <MessageSquare className="h-5 w-5 text-amber-600" />
                <h3 className="text-base font-bold text-slate-900">
                  Hỏi đáp & Thảo luận dưới bài giảng ({discussions.length})
                </h3>
              </div>

              <form onSubmit={handleAskQuestion} className="mb-6 space-y-3 rounded-xl bg-slate-50 p-4">
                <Input
                  label="Tiêu đề câu hỏi"
                  placeholder="Thầy/cô giải thích giúp em bước biến đổi ở phút 05:20..."
                  value={questionTitle}
                  onChange={(e) => setQuestionTitle(e.target.value)}
                  required
                />
                <Textarea
                  label="Nội dung chi tiết"
                  rows={2}
                  placeholder="Mô tả chi tiết thắc mắc của bạn..."
                  value={questionContent}
                  onChange={(e) => setQuestionContent(e.target.value)}
                  required
                />
                <div className="flex justify-end">
                  <Button type="submit" size="sm" loading={asking}>
                    <Send className="h-3.5 w-3.5" />
                    Gửi câu hỏi
                  </Button>
                </div>
              </form>

              <div className="space-y-4">
                {discussions.map((thread) => (
                  <div
                    key={thread.id}
                    className="rounded-xl border border-slate-200 p-4 space-y-3"
                  >
                    <div className="flex items-start justify-between gap-2">
                      <div>
                        <h4 className="text-sm font-bold text-slate-900">{thread.title}</h4>
                        <p className="mt-1 text-xs text-slate-700">{thread.content}</p>
                      </div>
                      <Badge tone={thread.isResolved ? 'success' : 'neutral'}>
                        {thread.isResolved ? 'Đã giải đáp' : 'Đang thảo luận'}
                      </Badge>
                    </div>

                    {thread.messages.length > 0 && (
                      <div className="space-y-2 border-l-2 border-amber-300 pl-3">
                        {thread.messages.map((m) => (
                          <div key={m.id} className="rounded-lg bg-amber-50/60 p-2.5 text-xs">
                            <p className="text-slate-800">{m.content}</p>
                            <span className="mt-1 block text-[10px] text-slate-400">
                              {new Date(m.createdAt).toLocaleString('vi-VN')}
                            </span>
                          </div>
                        ))}
                      </div>
                    )}

                    <div className="flex gap-2 pt-1">
                      <div className="flex-1">
                        <Input
                          placeholder="Nhập câu trả lời phản hồi..."
                          value={replyMap[thread.id] || ''}
                          onChange={(e) =>
                            setReplyMap((prev) => ({ ...prev, [thread.id]: e.target.value }))
                          }
                        />
                      </div>
                      <Button
                        size="sm"
                        variant="outline"
                        loading={replyingThreadId === thread.id}
                        onClick={() => handleReply(thread.id)}
                      >
                        Trả lời
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          )}
        </div>

        <div className="space-y-3">
          <h3 className="text-sm font-bold uppercase tracking-wider text-slate-500">
            Danh mục bài giảng
          </h3>
          {course.chapters.map((ch) => (
            <Card key={ch.id} className="p-4">
              <h4 className="text-xs font-bold uppercase text-slate-500">
                Chương {ch.orderIndex}: {ch.title}
              </h4>
              <div className="mt-2 space-y-1.5">
                {ch.lessons.map((lesson) => {
                  const isCurrent = activeLesson?.lessonId === lesson.id;
                  return (
                    <button
                      key={lesson.id}
                      type="button"
                      onClick={() => courseId && selectLesson(courseId, lesson.id)}
                      className={`flex w-full items-center justify-between rounded-lg px-3 py-2 text-left text-xs font-medium transition-colors ${
                        isCurrent
                          ? 'bg-amber-500 text-white'
                          : 'hover:bg-slate-100 text-slate-700'
                      }`}
                    >
                      <span className="line-clamp-1">
                        Bài {lesson.orderIndex}: {lesson.title}
                      </span>
                      {isCurrent && activeLesson?.isCompleted && (
                        <CheckCircle2 className="h-4 w-4 shrink-0" />
                      )}
                    </button>
                  );
                })}
              </div>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
};
