import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { BookPlus, FolderPlus, Plus, Send, Trash2, Video } from 'lucide-react';
import {
  CategoryItem,
  courseApi,
  CourseDetail,
  CourseSummary,
} from '../api/courseApi';
import { formatVnd, getApiErrorMessage } from '@/shared/types/api';
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
  Textarea,
} from '@/shared/ui/primitives';

export const TeacherCoursesPage: React.FC = () => {
  const [courses, setCourses] = useState<CourseSummary[]>([]);
  const [categories, setCategories] = useState<CategoryItem[]>([]);
  const [selectedCourse, setSelectedCourse] = useState<CourseDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Create course modal state
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [priceVnd, setPriceVnd] = useState('250000');
  const [grade, setGrade] = useState('10');
  const [thumbnailUrl, setThumbnailUrl] = useState('');
  const [creatingCourse, setCreatingCourse] = useState(false);

  // Add chapter state
  const [chapterTitle, setChapterTitle] = useState('');
  const [chapterDesc, setChapterDesc] = useState('');
  const [addingChapter, setAddingChapter] = useState(false);

  // Add lesson modal state
  const [targetChapterId, setTargetChapterId] = useState<string | null>(null);
  const [lessonTitle, setLessonTitle] = useState('');
  const [lessonVideoUrl, setLessonVideoUrl] = useState('');
  const [lessonDurationSec, setLessonDurationSec] = useState('600');
  const [lessonFreePreview, setLessonFreePreview] = useState(false);
  const [addingLesson, setAddingLesson] = useState(false);

  const [submittingReview, setSubmittingReview] = useState(false);

  const fetchCourses = async () => {
    setLoading(true);
    setError(null);
    try {
      const [myCourses, catList] = await Promise.all([
        courseApi.getTeacherCourses(),
        courseApi.getCategories().catch(() => []),
      ]);
      setCourses(myCourses);
      setCategories(catList);
      if (catList.length > 0 && !categoryId) {
        setCategoryId(catList[0].id);
      }
      if (myCourses.length > 0 && !selectedCourse) {
        const detail = await courseApi.getTeacherCourseDetail(myCourses[0].id);
        setSelectedCourse(detail);
      }
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải danh sách khóa học của giáo viên.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCourses();
  }, []);

  const handleSelectCourse = async (id: string) => {
    try {
      const detail = await courseApi.getTeacherCourseDetail(id);
      setSelectedCourse(detail);
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể tải chi tiết khóa học.'));
    }
  };

  const handleCreateCourse = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!categoryId) {
      toast.error('Vui lòng chọn danh mục môn học.');
      return;
    }
    setCreatingCourse(true);
    try {
      const created = await courseApi.createCourse({
        categoryId,
        title: title.trim(),
        description: description.trim(),
        thumbnailUrl: thumbnailUrl.trim() || undefined,
        priceVnd: Number(priceVnd) || 0,
        grades: [Number(grade)],
      });
      toast.success('Đã tạo khóa học mới ở trạng thái Nháp (DRAFT)!');
      setShowCreateModal(false);
      setTitle('');
      setDescription('');
      setSelectedCourse(created);
      const updatedList = await courseApi.getTeacherCourses();
      setCourses(updatedList);
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể tạo khóa học.'));
    } finally {
      setCreatingCourse(false);
    }
  };

  const handleAddChapter = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCourse || !chapterTitle.trim()) return;
    setAddingChapter(true);
    try {
      const updated = await courseApi.addChapter(selectedCourse.id, {
        title: chapterTitle.trim(),
        description: chapterDesc.trim() || undefined,
      });
      setSelectedCourse(updated);
      setChapterTitle('');
      setChapterDesc('');
      toast.success('Đã thêm chương học mới!');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể thêm chương học.'));
    } finally {
      setAddingChapter(false);
    }
  };

  const handleDeleteChapter = async (chapterId: string) => {
    if (!selectedCourse) return;
    try {
      const updated = await courseApi.deleteChapter(selectedCourse.id, chapterId);
      setSelectedCourse(updated);
      toast.success('Đã xóa chương học.');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể xóa chương học.'));
    }
  };

  const handleAddLesson = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCourse || !targetChapterId) return;
    setAddingLesson(true);
    try {
      const updated = await courseApi.addLesson(selectedCourse.id, targetChapterId, {
        title: lessonTitle.trim(),
        videoUrl: lessonVideoUrl.trim(),
        durationSec: Number(lessonDurationSec) || 300,
        isFreePreview: lessonFreePreview,
      });
      setSelectedCourse(updated);
      setTargetChapterId(null);
      setLessonTitle('');
      setLessonVideoUrl('');
      toast.success('Đã thêm bài giảng vào chương!');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể thêm bài giảng.'));
    } finally {
      setAddingLesson(false);
    }
  };

  const handleDeleteLesson = async (chapterId: string, lessonId: string) => {
    if (!selectedCourse) return;
    try {
      const updated = await courseApi.deleteLesson(selectedCourse.id, chapterId, lessonId);
      setSelectedCourse(updated);
      toast.success('Đã xóa bài giảng.');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể xóa bài giảng.'));
    }
  };

  const handleSubmitForReview = async () => {
    if (!selectedCourse) return;
    setSubmittingReview(true);
    try {
      const updated = await courseApi.submitCourseForReview(selectedCourse.id);
      setSelectedCourse(updated);
      setCourses((prev) =>
        prev.map((c) => (c.id === updated.id ? { ...c, status: updated.status } : c))
      );
      toast.success('Đã gửi khóa học sang Admin chờ phê duyệt xuất bản!');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Khóa học cần có ít nhất 1 chương và 1 bài học để nộp duyệt.'));
    } finally {
      setSubmittingReview(false);
    }
  };

  if (loading) return <Spinner label="Đang tải Studio Giáo trình Giáo viên..." />;
  if (error) return <ErrorState message={error} onRetry={fetchCourses} />;

  return (
    <div className="space-y-6">
      <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Studio Quản lý Khóa học & Giáo trình</h1>
          <p className="text-sm text-slate-600">
            Xây dựng cấu trúc Chương, Bài giảng Video và nộp phê duyệt xuất bản (UC30, UC31, UC39)
          </p>
        </div>
        <Button onClick={() => setShowCreateModal(true)}>
          <BookPlus className="h-4 w-4" />
          Tạo khóa học mới
        </Button>
      </div>

      {courses.length === 0 ? (
        <EmptyState
          title="Bạn chưa tạo khóa học nào"
          description="Hãy khởi tạo khóa học đầu tiên, thêm các chương và bài giảng video để chia sẻ kiến thức tới học sinh."
          action={
            <Button onClick={() => setShowCreateModal(true)}>
              <Plus className="h-4 w-4" />
              Khởi tạo khóa học đầu tiên
            </Button>
          }
        />
      ) : (
        <div className="grid gap-6 lg:grid-cols-3">
          <div className="space-y-3">
            <h2 className="text-sm font-bold uppercase tracking-wider text-slate-500">
              Khóa học của tôi ({courses.length})
            </h2>
            {courses.map((c) => (
              <Card
                key={c.id}
                className={`cursor-pointer p-4 transition-all ${
                  selectedCourse?.id === c.id
                    ? 'border-amber-500 ring-2 ring-amber-500/20'
                    : 'hover:border-slate-300'
                }`}
              >
                <div onClick={() => handleSelectCourse(c.id)}>
                  <div className="flex items-start justify-between gap-2">
                    <h3 className="text-sm font-bold text-slate-900">{c.title}</h3>
                    <Badge
                      tone={
                        c.status === 'PUBLISHED'
                          ? 'success'
                          : c.status === 'PENDING_REVIEW'
                            ? 'warning'
                            : c.status === 'REJECTED'
                              ? 'danger'
                              : 'neutral'
                      }
                    >
                      {c.status}
                    </Badge>
                  </div>
                  <div className="mt-2 flex items-center justify-between text-xs text-slate-500">
                    <span>{formatVnd(c.priceVnd)}</span>
                    <span>
                      {c.totalChapters} chương • {c.totalLessons} bài
                    </span>
                  </div>
                </div>
              </Card>
            ))}
          </div>

          <div className="space-y-5 lg:col-span-2">
            {selectedCourse && (
              <>
                <Card className="p-6">
                  <div className="flex flex-col justify-between gap-4 border-b border-slate-100 pb-4 sm:flex-row sm:items-center">
                    <div>
                      <div className="flex items-center gap-2">
                        <h2 className="text-xl font-bold text-slate-900">{selectedCourse.title}</h2>
                        <Badge tone="brand">{selectedCourse.status}</Badge>
                      </div>
                      <p className="mt-1 text-xs text-slate-500">
                        Học phí: {formatVnd(selectedCourse.priceVnd)} • Khối lớp:{' '}
                        {selectedCourse.grades?.join(', ') || 'K-12'}
                      </p>
                    </div>
                    {(selectedCourse.status === 'DRAFT' ||
                      selectedCourse.status === 'NEEDS_REVISION' ||
                      selectedCourse.status === 'REJECTED') && (
                      <Button
                        onClick={handleSubmitForReview}
                        loading={submittingReview}
                        variant="primary"
                        size="sm"
                      >
                        <Send className="h-4 w-4" />
                        Nộp duyệt Admin
                      </Button>
                    )}
                  </div>

                  <form onSubmit={handleAddChapter} className="mt-4 flex flex-col gap-3 sm:flex-row">
                    <div className="flex-1">
                      <Input
                        placeholder="Tên chương mới (VD: Chương 1: Hàm số bậc hai)..."
                        value={chapterTitle}
                        onChange={(e) => setChapterTitle(e.target.value)}
                        required
                      />
                    </div>
                    <div className="flex-1">
                      <Input
                        placeholder="Mô tả ngắn chương..."
                        value={chapterDesc}
                        onChange={(e) => setChapterDesc(e.target.value)}
                      />
                    </div>
                    <Button type="submit" loading={addingChapter} variant="secondary">
                      <FolderPlus className="h-4 w-4" />
                      Thêm chương
                    </Button>
                  </form>
                </Card>

                <div className="space-y-4">
                  {selectedCourse.chapters.map((ch) => (
                    <Card key={ch.id} className="p-5">
                      <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                        <div>
                          <h3 className="text-sm font-bold text-slate-900">
                            Chương {ch.orderIndex}: {ch.title}
                          </h3>
                          {ch.description && (
                            <p className="text-xs text-slate-500">{ch.description}</p>
                          )}
                        </div>
                        <div className="flex items-center gap-2">
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => setTargetChapterId(ch.id)}
                          >
                            <Video className="h-3.5 w-3.5" />
                            Thêm bài giảng
                          </Button>
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() => handleDeleteChapter(ch.id)}
                          >
                            <Trash2 className="h-4 w-4 text-red-600" />
                          </Button>
                        </div>
                      </div>

                      {ch.lessons.length === 0 ? (
                        <p className="py-4 text-center text-xs text-slate-400">
                          Chưa có bài giảng nào trong chương này.
                        </p>
                      ) : (
                        <div className="mt-2 divide-y divide-slate-100">
                          {ch.lessons.map((l) => (
                            <div
                              key={l.id}
                              className="flex items-center justify-between py-2.5 text-xs"
                            >
                              <div className="flex items-center gap-2">
                                <span className="font-semibold text-slate-800">
                                  Bài {l.orderIndex}: {l.title}
                                </span>
                                {l.isFreePreview && <Badge tone="success">Học thử</Badge>}
                              </div>
                              <div className="flex items-center gap-3">
                                <span className="text-slate-500">
                                  {Math.round(l.durationSec / 60)} phút
                                </span>
                                <button
                                  type="button"
                                  onClick={() => handleDeleteLesson(ch.id, l.id)}
                                  className="text-red-500 hover:text-red-700"
                                >
                                  <Trash2 className="h-3.5 w-3.5" />
                                </button>
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </Card>
                  ))}
                </div>
              </>
            )}
          </div>
        </div>
      )}

      <Modal
        open={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        title="Khởi tạo Khóa học mới (DRAFT)"
      >
        <form onSubmit={handleCreateCourse} className="space-y-3">
          <Input
            label="Tên khóa học"
            placeholder="Toán 12 - Chuyên đề Khảo sát hàm số & Tích phân"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />
          <Select
            label="Chuyên mục môn học"
            value={categoryId}
            onChange={(e) => setCategoryId(e.target.value)}
          >
            {categories.map((cat) => (
              <option key={cat.id} value={cat.id}>
                {cat.name}
              </option>
            ))}
          </Select>
          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Học phí (VNĐ)"
              type="number"
              min={0}
              value={priceVnd}
              onChange={(e) => setPriceVnd(e.target.value)}
              required
            />
            <Select
              label="Khối lớp"
              value={grade}
              onChange={(e) => setGrade(e.target.value)}
            >
              {Array.from({ length: 12 }, (_, i) => i + 1).map((g) => (
                <option key={g} value={String(g)}>
                  Lớp {g}
                </option>
              ))}
            </Select>
          </div>
          <Input
            label="URL Ảnh bìa khóa học (tuỳ chọn)"
            placeholder="https://..."
            value={thumbnailUrl}
            onChange={(e) => setThumbnailUrl(e.target.value)}
          />
          <Textarea
            label="Mô tả chi tiết khóa học"
            rows={3}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            required
          />
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="ghost" onClick={() => setShowCreateModal(false)}>
              Hủy
            </Button>
            <Button type="submit" loading={creatingCourse}>
              Tạo khóa học
            </Button>
          </div>
        </form>
      </Modal>

      <Modal
        open={Boolean(targetChapterId)}
        onClose={() => setTargetChapterId(null)}
        title="Thêm Bài giảng Video vào Chương"
      >
        <form onSubmit={handleAddLesson} className="space-y-3">
          <Input
            label="Tiêu đề bài giảng"
            placeholder="Bài 1: Khái niệm đạo hàm và ý nghĩa hình học"
            value={lessonTitle}
            onChange={(e) => setLessonTitle(e.target.value)}
            required
          />
          <Input
            label="Đường dẫn Video (YouTube / MP4 / HLS)"
            placeholder="https://www.youtube.com/watch?v=..."
            value={lessonVideoUrl}
            onChange={(e) => setLessonVideoUrl(e.target.value)}
            required
          />
          <Input
            label="Thời lượng (giây)"
            type="number"
            min={30}
            value={lessonDurationSec}
            onChange={(e) => setLessonDurationSec(e.target.value)}
            required
          />
          <label className="flex items-center gap-2 text-xs font-medium text-slate-700">
            <input
              type="checkbox"
              checked={lessonFreePreview}
              onChange={(e) => setLessonFreePreview(e.target.checked)}
              className="rounded border-slate-300"
            />
            Cho phép Học viên xem học thử miễn phí (Free Preview)
          </label>
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="ghost" onClick={() => setTargetChapterId(null)}>
              Hủy
            </Button>
            <Button type="submit" loading={addingLesson}>
              Lưu bài giảng
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
