import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { ArrowLeft, CheckCircle2, Clock, Send, XCircle } from 'lucide-react';
import { assessmentApi, QuizResultDto, StartQuizDto } from '../api/assessmentApi';
import { getApiErrorMessage } from '@/shared/types/api';
import { LatexRenderer } from '@/shared/ui/LatexRenderer';
import {
  Badge,
  Button,
  Card,
  ErrorState,
  Spinner,
} from '@/shared/ui/primitives';

export const StudentQuizRoomPage: React.FC = () => {
  const { chapterId } = useParams<{ chapterId: string }>();
  const navigate = useNavigate();

  const [exam, setExam] = useState<StartQuizDto | null>(null);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [result, setResult] = useState<QuizResultDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const startNewAttempt = async () => {
    if (!chapterId) return;
    setLoading(true);
    setError(null);
    setResult(null);
    setAnswers({});
    try {
      const started = await assessmentApi.startQuiz(chapterId);
      setExam(started);
    } catch (err) {
      setError(
        getApiErrorMessage(
          err,
          'Chương học này chưa được Giáo viên cấu hình đề kiểm tra hoặc bạn đã hết lượt làm bài.'
        )
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    startNewAttempt();
  }, [chapterId]);

  const handleSelectChoice = (questionId: string, choiceId: string) => {
    if (result) return;
    setAnswers((prev) => ({ ...prev, [questionId]: choiceId }));
  };

  const handleSubmitQuiz = async () => {
    if (!exam) return;
    setSubmitting(true);
    try {
      const graded = await assessmentApi.submitQuiz({
        attemptId: exam.attemptId,
        answers,
      });
      setResult(graded);
      if (graded.passed) {
        toast.success(`Chúc mừng! Bạn đạt ${graded.score.toFixed(1)} điểm (ĐẠT).`);
      } else {
        toast.error(`Bạn đạt ${graded.score.toFixed(1)} điểm (Chưa đạt mức chuẩn).`);
      }
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể nộp bài kiểm tra.'));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <Spinner label="Đang sinh đề kiểm tra ngẫu nhiên từ Ngân hàng câu hỏi..." />;
  if (error || !exam) {
    return (
      <div className="space-y-4">
        <ErrorState message={error || 'Không thể khởi tạo đề thi'} onRetry={startNewAttempt} />
        <Button variant="outline" onClick={() => navigate(-1)}>
          <ArrowLeft className="h-4 w-4" />
          Quay lại phòng học
        </Button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <Card className="p-5">
        <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
          <div>
            <h1 className="text-xl font-bold text-slate-900">
              Bài Kiểm tra Đánh giá Chương (UC17, UC18, UC19)
            </h1>
            <p className="text-xs text-slate-500">
              Tổng số câu: {exam.totalQuestions} • Điểm chuẩn qua bài: {exam.passingScore}%
            </p>
          </div>
          <div className="flex items-center gap-2">
            {exam.timeLimitMinutes && (
              <Badge tone="warning">
                <Clock className="h-3.5 w-3.5" />
                {exam.timeLimitMinutes} phút
              </Badge>
            )}
            <Badge tone="brand">
              Đã chọn: {Object.keys(answers).length}/{exam.totalQuestions}
            </Badge>
          </div>
        </div>
      </Card>

      {result && (
        <Card className="border-2 border-amber-400 bg-amber-50/40 p-6">
          <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
            <div>
              <Badge tone={result.passed ? 'success' : 'danger'}>
                {result.passed ? 'ĐẠT YÊU CẦU (PASSED)' : 'CHƯA ĐẠT (FAILED)'}
              </Badge>
              <h2 className="mt-2 text-2xl font-extrabold text-slate-900">
                Điểm số: {result.score.toFixed(1)} / 100
              </h2>
              <p className="text-xs text-slate-600">
                Số câu trả lời đúng: {result.correctCount} / {result.totalQuestions} câu
              </p>
            </div>
            <div className="flex gap-2">
              <Button variant="secondary" onClick={startNewAttempt}>
                Làm lại đề mới
              </Button>
              <Button variant="outline" onClick={() => navigate(-1)}>
                Quay về bài học
              </Button>
            </div>
          </div>
        </Card>
      )}

      <div className="space-y-4">
        {exam.questions.map((q, index) => {
          const selectedChoice = answers[q.questionId];
          const review = result?.reviews.find((r) => r.questionId === q.questionId);

          return (
            <Card key={q.questionId} className="p-5">
              <div className="flex items-start justify-between gap-2">
                <span className="text-xs font-bold uppercase tracking-wider text-amber-700">
                  Câu hỏi {index + 1}
                </span>
                {review && (
                  <Badge tone={review.isCorrect ? 'success' : 'danger'}>
                    {review.isCorrect ? (
                      <>
                        <CheckCircle2 className="h-3.5 w-3.5" /> Chính xác
                      </>
                    ) : (
                      <>
                        <XCircle className="h-3.5 w-3.5" /> Sai
                      </>
                    )}
                  </Badge>
                )}
              </div>

              <div className="mt-2 text-sm font-semibold text-slate-900">
                <LatexRenderer content={q.content} />
              </div>

              <div className="mt-4 space-y-2">
                {q.choices.map((ch) => {
                  const isSelected = selectedChoice === ch.choiceId;
                  const isCorrectAnswer = review?.correctChoiceId === ch.choiceId;

                  let borderStyle = 'border-slate-200 hover:border-amber-400 bg-white';
                  if (review) {
                    if (isCorrectAnswer) {
                      borderStyle = 'border-emerald-500 bg-emerald-50 text-emerald-900 font-semibold';
                    } else if (isSelected && !review.isCorrect) {
                      borderStyle = 'border-red-400 bg-red-50 text-red-900';
                    }
                  } else if (isSelected) {
                    borderStyle = 'border-amber-500 bg-amber-50 font-semibold text-amber-950';
                  }

                  return (
                    <button
                      key={ch.choiceId}
                      type="button"
                      disabled={Boolean(result)}
                      onClick={() => handleSelectChoice(q.questionId, ch.choiceId)}
                      className={`flex w-full items-center justify-between rounded-xl border px-4 py-3 text-left text-xs transition-all ${borderStyle}`}
                    >
                      <LatexRenderer content={ch.content} />
                    </button>
                  );
                })}
              </div>

              {review?.explanation && (
                <div className="mt-4 rounded-xl bg-slate-50 p-3.5 text-xs text-slate-700">
                  <strong className="text-slate-900">Giải thích đáp án: </strong>
                  <LatexRenderer content={review.explanation} />
                </div>
              )}
            </Card>
          );
        })}
      </div>

      {!result && (
        <div className="flex justify-end">
          <Button size="lg" loading={submitting} onClick={handleSubmitQuiz}>
            <Send className="h-4 w-4" />
            Nộp bài & Chấm điểm ngay
          </Button>
        </div>
      )}
    </div>
  );
};
