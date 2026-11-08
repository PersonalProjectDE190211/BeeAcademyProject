import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { FileQuestion, Plus, Settings2, Trash2 } from 'lucide-react';
import { assessmentApi, ChoiceItemDto, QuestionItem } from '../api/assessmentApi';
import { courseApi, CourseDetail, CourseSummary } from '@/features/courses/api/courseApi';
import { getApiErrorMessage } from '@/shared/types/api';
import { LatexRenderer } from '@/shared/ui/LatexRenderer';
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

export const TeacherQuestionBankPage: React.FC = () => {
  const [questions, setQuestions] = useState<QuestionItem[]>([]);
  const [courses, setCourses] = useState<CourseSummary[]>([]);
  const [selectedCourseDetail, setSelectedCourseDetail] = useState<CourseDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Create question modal
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [chapterId, setChapterId] = useState('');
  const [grade, setGrade] = useState('12');
  const [difficulty, setDifficulty] = useState<'EASY' | 'MEDIUM' | 'HARD'>('MEDIUM');
  const [content, setContent] = useState(
    'Tính tích phân $I = \\int_{0}^{1} 2x \\, dx$ bằng bao nhiêu?'
  );
  const [explanation, setExplanation] = useState(
    'Ta có nguyên hàm của $2x$ là $x^2$, suy ra $I = 1^2 - 0^2 = 1$.'
  );
  const [choices, setChoices] = useState<ChoiceItemDto[]>([
    { content: '$I = 1$', isCorrect: true, position: 1 },
    { content: '$I = 2$', isCorrect: false, position: 2 },
    { content: '$I = 0$', isCorrect: false, position: 3 },
    { content: '$I = \\frac{1}{2}$', isCorrect: false, position: 4 },
  ]);
  const [savingQuestion, setSavingQuestion] = useState(false);

  // Quiz matrix config modal
  const [showConfigModal, setShowConfigModal] = useState(false);
  const [easyCount, setEasyCount] = useState('2');
  const [mediumCount, setMediumCount] = useState('2');
  const [hardCount, setHardCount] = useState('1');
  const [timeLimitMinutes, setTimeLimitMinutes] = useState('15');
  const [passingScore, setPassingScore] = useState('70');
  const [savingConfig, setSavingConfig] = useState(false);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [qList, myCourses] = await Promise.all([
        assessmentApi.getTeacherQuestions(),
        courseApi.getTeacherCourses().catch(() => []),
      ]);
      setQuestions(qList);
      setCourses(myCourses);

      if (myCourses.length > 0) {
        const detail = await courseApi.getTeacherCourseDetail(myCourses[0].id);
        setSelectedCourseDetail(detail);
        if (detail.chapters.length > 0) {
          setChapterId(detail.chapters[0].id);
        }
      }
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải Ngân hàng câu hỏi.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleCourseChange = async (courseId: string) => {
    try {
      const detail = await courseApi.getTeacherCourseDetail(courseId);
      setSelectedCourseDetail(detail);
      setChapterId(detail.chapters[0]?.id || '');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể tải danh sách chương của khóa học.'));
    }
  };

  const handleChoiceChange = (idx: number, field: 'content' | 'isCorrect', value: string | boolean) => {
    setChoices((prev) =>
      prev.map((c, i) => {
        if (field === 'isCorrect') {
          return { ...c, isCorrect: i === idx };
        }
        return i === idx ? { ...c, content: String(value) } : c;
      })
    );
  };

  const handleCreateQuestion = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCourseDetail || !chapterId) {
      toast.error('Vui lòng chọn khóa học và chương học trước khi tạo câu hỏi.');
      return;
    }
    setSavingQuestion(true);
    try {
      const created = await assessmentApi.createQuestion({
        categoryId: selectedCourseDetail.categoryId,
        chapterId,
        grade: Number(grade),
        content: content.trim(),
        explanation: explanation.trim(),
        difficulty,
        type: 'MULTIPLE_CHOICE',
        choices,
      });
      setQuestions((prev) => [created, ...prev]);
      setShowCreateModal(false);
      toast.success('Đã thêm câu hỏi mới vào Ngân hàng đề thi!');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể tạo câu hỏi.'));
    } finally {
      setSavingQuestion(false);
    }
  };

  const handleDeactivateQuestion = async (id: string) => {
    try {
      await assessmentApi.deactivateQuestion(id, 'Giáo viên gỡ câu hỏi');
      setQuestions((prev) => prev.filter((q) => q.id !== id));
      toast.success('Đã vô hiệu hóa câu hỏi khỏi ngân hàng.');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể xóa câu hỏi.'));
    }
  };

  const handleSaveQuizConfig = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!chapterId) {
      toast.error('Vui lòng chọn chương học để cấu hình đề thi.');
      return;
    }
    const easy = Number(easyCount) || 0;
    const med = Number(mediumCount) || 0;
    const hard = Number(hardCount) || 0;
    const total = easy + med + hard;

    setSavingConfig(true);
    try {
      await assessmentApi.saveQuizConfig({
        chapterId,
        totalQuestions: total,
        easyCount: easy,
        mediumCount: med,
        hardCount: hard,
        timeLimitMinutes: Number(timeLimitMinutes) || 15,
        passingScore: Number(passingScore) || 70,
        shuffleQuestions: true,
        shuffleChoices: true,
        maxAttempts: 5,
      });
      setShowConfigModal(false);
      toast.success('Đã lưu ma trận cấu hình đề thi cho chương!');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể lưu cấu hình đề thi.'));
    } finally {
      setSavingConfig(false);
    }
  };

  if (loading) return <Spinner label="Đang tải Ngân hàng câu hỏi..." />;
  if (error) return <ErrorState message={error} onRetry={fetchData} />;

  return (
    <div className="space-y-6">
      <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">
            Ngân hàng Câu hỏi LaTeX & Ma trận Đề thi (UC33, UC34)
          </h1>
          <p className="text-sm text-slate-600">
            Soạn thảo câu hỏi Toán/Lý/Hóa hỗ trợ công thức KaTeX và cấu hình ma trận Dễ/TB/Khó
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="secondary" onClick={() => setShowConfigModal(true)}>
            <Settings2 className="h-4 w-4" />
            Cấu hình Ma trận Đề thi
          </Button>
          <Button onClick={() => setShowCreateModal(true)}>
            <Plus className="h-4 w-4" />
            Tạo câu hỏi mới
          </Button>
        </div>
      </div>

      {questions.length === 0 ? (
        <EmptyState
          title="Ngân hàng câu hỏi đang trống"
          description="Hãy tạo các câu hỏi trắc nghiệm theo mức độ Dễ, Trung bình, Khó để hệ thống tự động sinh đề kiểm tra cho học sinh."
          action={
            <Button onClick={() => setShowCreateModal(true)}>
              <FileQuestion className="h-4 w-4" />
              Soạn câu hỏi đầu tiên
            </Button>
          }
        />
      ) : (
        <div className="space-y-4">
          {questions.map((q) => (
            <Card key={q.id} className="p-5">
              <div className="flex items-start justify-between gap-3">
                <div className="flex flex-wrap items-center gap-2">
                  <Badge
                    tone={
                      q.difficulty === 'HARD'
                        ? 'danger'
                        : q.difficulty === 'MEDIUM'
                          ? 'warning'
                          : 'success'
                    }
                  >
                    Độ khó: {q.difficulty}
                  </Badge>
                  <Badge tone="brand">Khối {q.grade}</Badge>
                  <Badge tone="neutral">{q.status}</Badge>
                </div>
                <Button
                  size="sm"
                  variant="ghost"
                  onClick={() => handleDeactivateQuestion(q.id)}
                >
                  <Trash2 className="h-4 w-4 text-red-600" />
                </Button>
              </div>

              <div className="mt-3 rounded-xl bg-slate-50 p-3.5 text-sm font-medium text-slate-900">
                <LatexRenderer content={q.content} />
              </div>

              <div className="mt-3 grid gap-2 sm:grid-cols-2">
                {q.choices.map((c, idx) => (
                  <div
                    key={c.id || idx}
                    className={`rounded-lg border px-3 py-2 text-xs ${
                      c.isCorrect
                        ? 'border-emerald-300 bg-emerald-50 font-semibold text-emerald-900'
                        : 'border-slate-200 bg-white text-slate-700'
                    }`}
                  >
                    <LatexRenderer content={c.content} />
                  </div>
                ))}
              </div>

              {q.explanation && (
                <div className="mt-3 border-t border-slate-100 pt-2.5 text-xs text-slate-600">
                  <strong className="text-slate-800">Lời giải chi tiết: </strong>
                  <LatexRenderer content={q.explanation} />
                </div>
              )}
            </Card>
          ))}
        </div>
      )}

      <Modal
        open={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        title="Soạn Câu hỏi mới (Hỗ trợ LaTeX $...$)"
      >
        <form onSubmit={handleCreateQuestion} className="space-y-3 max-h-[75vh] overflow-y-auto pr-1">
          <Select
            label="Khóa học"
            value={selectedCourseDetail?.id || ''}
            onChange={(e) => handleCourseChange(e.target.value)}
          >
            {courses.map((c) => (
              <option key={c.id} value={c.id}>
                {c.title}
              </option>
            ))}
          </Select>

          <div className="grid grid-cols-3 gap-2">
            <Select
              label="Chương học"
              value={chapterId}
              onChange={(e) => setChapterId(e.target.value)}
            >
              {selectedCourseDetail?.chapters.map((ch) => (
                <option key={ch.id} value={ch.id}>
                  Chương {ch.orderIndex}: {ch.title}
                </option>
              ))}
            </Select>

            <Select
              label="Độ khó"
              value={difficulty}
              onChange={(e) => setDifficulty(e.target.value as 'EASY' | 'MEDIUM' | 'HARD')}
            >
              <option value="EASY">Dễ (EASY)</option>
              <option value="MEDIUM">Trung bình (MEDIUM)</option>
              <option value="HARD">Khó (HARD)</option>
            </Select>

            <Input
              label="Khối lớp"
              type="number"
              min={1}
              max={12}
              value={grade}
              onChange={(e) => setGrade(e.target.value)}
            />
          </div>

          <Textarea
            label="Nội dung câu hỏi (Dùng $...$ cho công thức Toán)"
            rows={2}
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
          />

          <div className="rounded-lg border border-amber-200 bg-amber-50/50 p-2.5 text-xs">
            <span className="font-semibold text-amber-800">Xem trước công thức: </span>
            <LatexRenderer content={content} />
          </div>

          <div className="space-y-2">
            <p className="text-xs font-semibold text-slate-700">
              Các phương án lựa chọn (Tích chọn đáp án đúng)
            </p>
            {choices.map((ch, idx) => (
              <div key={idx} className="flex items-center gap-2">
                <input
                  type="radio"
                  name="correctChoice"
                  checked={ch.isCorrect}
                  onChange={() => handleChoiceChange(idx, 'isCorrect', true)}
                />
                <div className="flex-1">
                  <Input
                    value={ch.content}
                    onChange={(e) => handleChoiceChange(idx, 'content', e.target.value)}
                    required
                  />
                </div>
              </div>
            ))}
          </div>

          <Textarea
            label="Lời giải thích đáp án"
            rows={2}
            value={explanation}
            onChange={(e) => setExplanation(e.target.value)}
          />

          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="ghost" onClick={() => setShowCreateModal(false)}>
              Hủy
            </Button>
            <Button type="submit" loading={savingQuestion}>
              Lưu câu hỏi
            </Button>
          </div>
        </form>
      </Modal>

      <Modal
        open={showConfigModal}
        onClose={() => setShowConfigModal(false)}
        title="Thiết lập Ma trận Đề kiểm tra Chương (UC33)"
      >
        <form onSubmit={handleSaveQuizConfig} className="space-y-3">
          <Select
            label="Chọn Chương áp dụng"
            value={chapterId}
            onChange={(e) => setChapterId(e.target.value)}
          >
            {selectedCourseDetail?.chapters.map((ch) => (
              <option key={ch.id} value={ch.id}>
                Chương {ch.orderIndex}: {ch.title}
              </option>
            ))}
          </Select>

          <div className="grid grid-cols-3 gap-3">
            <Input
              label="Số câu Dễ"
              type="number"
              min={0}
              value={easyCount}
              onChange={(e) => setEasyCount(e.target.value)}
              required
            />
            <Input
              label="Số câu Trung bình"
              type="number"
              min={0}
              value={mediumCount}
              onChange={(e) => setMediumCount(e.target.value)}
              required
            />
            <Input
              label="Số câu Khó"
              type="number"
              min={0}
              value={hardCount}
              onChange={(e) => setHardCount(e.target.value)}
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Input
              label="Thời gian làm bài (phút)"
              type="number"
              min={1}
              value={timeLimitMinutes}
              onChange={(e) => setTimeLimitMinutes(e.target.value)}
              required
            />
            <Input
              label="Điểm chuẩn đạt (%)"
              type="number"
              min={10}
              max={100}
              value={passingScore}
              onChange={(e) => setPassingScore(e.target.value)}
              required
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="ghost" onClick={() => setShowConfigModal(false)}>
              Hủy
            </Button>
            <Button type="submit" loading={savingConfig}>
              Lưu cấu hình
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
