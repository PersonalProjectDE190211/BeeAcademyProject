import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import {
  ArrowLeft,
  Award,
  CheckCircle2,
  Clock,
  HelpCircle,
  RotateCcw,
  Send,
  XCircle,
} from 'lucide-react';
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

const CHOICE_LETTERS = ['A', 'B', 'C', 'D', 'E', 'F'];

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
    const answeredCount = Object.keys(answers).length;
    if (answeredCount < exam.totalQuestions) {
      const proceed = window.confirm(
        `Bạn mới trả lời ${answeredCount}/${exam.totalQuestions} câu hỏi. Bạn có chắc chắn muốn nộp bài ngay?`
      );
      if (!proceed) return;
    }

    setSubmitting(true);
    try {
      const graded = await assessmentApi.submitQuiz({
        attemptId: exam.attemptId,
        answers,
      });
      setResult(graded);
      if (graded.passed) {
        toast.success(`Chúc mừng! Bạn đạt ${graded.score.toFixed(1)} điểm (ĐẠT YÊU CẦU)!`);
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
      <div className="mx-auto max-w-2xl space-y-4 pt-10">
        <ErrorState message={error || 'Không thể khởi tạo đề thi'} onRetry={startNewAttempt} />
        <div className="flex justify-center">
          <Button variant="outline" onClick={() => navigate(-1)}>
            <ArrowLeft className="h-4 w-4" />
            Quay lại phòng học
          </Button>
        </div>
      </div>
    );
  }

  const answeredCount = Object.keys(answers).length;

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      {/* Header Bar */}
      <Card className="p-5 border-sand bg-white shadow-2xs">
        <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <Button
                variant="ghost"
                size="sm"
                onClick={() => navigate(-1)}
                className="p-1 h-7 text-ink-muted hover:text-ink -ml-2"
              >
                <ArrowLeft className="h-4 w-4" />
              </Button>
              <h1 className="text-xl font-bold text-ink">
                Bài Kiểm tra Đánh giá Chương
              </h1>
            </div>
            <p className="text-xs text-ink-muted">
              Đề thi ngẫu nhiên • Tổng số: {exam.totalQuestions} câu • Điểm chuẩn qua bài: {exam.passingScore}%
            </p>
          </div>

          <div className="flex items-center gap-2.5">
            {exam.timeLimitMinutes && (
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-amber-50 text-amber-800 border border-amber-200">
                <Clock className="h-3.5 w-3.5 text-amber-600" />
                {exam.timeLimitMinutes} phút
              </span>
            )}
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-cream text-brand-700 border border-sand">
              Đã làm: {answeredCount}/{exam.totalQuestions}
            </span>
          </div>
        </div>

        {/* Quick Question Jump Pills */}
        <div className="mt-4 flex flex-wrap gap-1.5 border-t border-sand-soft pt-3">
          {exam.questions.map((q, idx) => {
            const isAnswered = Boolean(answers[q.questionId]);
            const review = result?.reviews.find((r) => r.questionId === q.questionId);

            let pillStyle = isAnswered
              ? 'bg-brand-500 text-white border-brand-500'
              : 'bg-white text-ink-muted border-sand hover:border-sand-strong';

            if (review) {
              pillStyle = review.isCorrect
                ? 'bg-emerald-600 text-white border-emerald-600'
                : 'bg-red-500 text-white border-red-500';
            }

            return (
              <a
                key={q.questionId}
                href={`#question-${idx + 1}`}
                className={`flex h-7 w-7 items-center justify-center rounded-lg border text-xs font-bold transition-all ${pillStyle}`}
              >
                {idx + 1}
              </a>
            );
          })}
        </div>
      </Card>

      {/* Result Card when Graded */}
      {result && (
        <Card
          className={`p-6 border-2 shadow-md ${
            result.passed
              ? 'border-emerald-500 bg-emerald-50/50'
              : 'border-red-400 bg-red-50/40'
          }`}
        >
          <div className="flex flex-col justify-between gap-5 sm:flex-row sm:items-center">
            <div className="space-y-1">
              <Badge tone={result.passed ? 'success' : 'danger'}>
                {result.passed ? '✓ ĐẠT YÊU CẦU (PASSED)' : '✕ CHƯA ĐẠT (FAILED)'}
              </Badge>
              <h2 className="text-3xl font-extrabold text-ink">
                Điểm số: {result.score.toFixed(1)} / 100
              </h2>
              <p className="text-xs text-ink-muted">
                Đúng: <strong>{result.correctCount}</strong> / {result.totalQuestions} câu • Tỷ lệ chính xác: {((result.correctCount / result.totalQuestions) * 100).toFixed(0)}%
              </p>
            </div>

            <div className="flex flex-wrap gap-2.5">
              <Button variant="secondary" size="md" onClick={startNewAttempt}>
                <RotateCcw className="h-4 w-4" />
                Làm lại đề mới
              </Button>
              <Button variant="outline" size="md" onClick={() => navigate(-1)}>
                <ArrowLeft className="h-4 w-4" />
                Quay về phòng học
              </Button>
            </div>
          </div>
        </Card>
      )}

      {/* Question List */}
      <div className="space-y-5">
        {exam.questions.map((q, index) => {
          const selectedChoice = answers[q.questionId];
          const review = result?.reviews.find((r) => r.questionId === q.questionId);

          return (
            <Card
              key={q.questionId}
              id={`question-${index + 1}`}
              className="p-5 sm:p-6 border-sand bg-white shadow-2xs scroll-mt-24"
            >
              <div className="flex items-center justify-between gap-2 border-b border-sand-soft pb-2.5">
                <span className="text-xs font-bold uppercase tracking-wider text-brand-700">
                  Câu hỏi {index + 1}
                </span>
                {review && (
                  <Badge tone={review.isCorrect ? 'success' : 'danger'}>
                    {review.isCorrect ? (
                      <>
                        <CheckCircle2 className="h-3.5 w-3.5" /> Chính xác (+{(100 / exam.totalQuestions).toFixed(1)}đ)
                      </>
                    ) : (
                      <>
                        <XCircle className="h-3.5 w-3.5" /> Chưa chính xác
                      </>
                    )}
                  </Badge>
                )}
              </div>

              <div className="mt-3.5 text-sm sm:text-base font-medium text-ink leading-relaxed">
                <LatexRenderer content={q.content} />
              </div>

              {/* Choices */}
              <div className="mt-5 space-y-2.5">
                {q.choices.map((ch, cIdx) => {
                  const letter = CHOICE_LETTERS[cIdx] || String(cIdx + 1);
                  const isSelected = selectedChoice === ch.choiceId;
                  const isCorrectAnswer = review?.correctChoiceId === ch.choiceId;

                  let cardStyle = 'border-sand hover:border-sand-strong bg-white hover:bg-cream text-ink';
                  let letterStyle = 'bg-sand-soft text-ink-muted border-sand';

                  if (review) {
                    if (isCorrectAnswer) {
                      cardStyle = 'border-emerald-500 bg-emerald-50 text-emerald-950 font-medium shadow-xs';
                      letterStyle = 'bg-emerald-600 text-white border-emerald-600';
                    } else if (isSelected && !review.isCorrect) {
                      cardStyle = 'border-red-400 bg-red-50 text-red-950';
                      letterStyle = 'bg-red-500 text-white border-red-500';
                    }
                  } else if (isSelected) {
                    cardStyle = 'border-brand-500 bg-blush font-semibold text-brand-700 shadow-xs ring-1 ring-brand-500/30';
                    letterStyle = 'bg-gradient-to-br from-brand-700 to-brand-500 text-white border-transparent';
                  }

                  return (
                    <button
                      key={ch.choiceId}
                      type="button"
                      disabled={Boolean(result)}
                      onClick={() => handleSelectChoice(q.questionId, ch.choiceId)}
                      className={`flex w-full items-start gap-3 rounded-xl border p-3.5 text-left text-xs sm:text-sm transition-all cursor-pointer disabled:cursor-default ${cardStyle}`}
                    >
                      <span
                        className={`flex h-6 w-6 shrink-0 items-center justify-center rounded-lg border text-xs font-bold ${letterStyle}`}
                      >
                        {letter}
                      </span>
                      <div className="flex-1 pt-0.5">
                        <LatexRenderer content={ch.content} />
                      </div>
                    </button>
                  );
                })}
              </div>

              {/* Explanation */}
              {review?.explanation && (
                <div className="mt-4 rounded-xl bg-cream p-4 border border-sand text-xs text-ink leading-relaxed">
                  <div className="flex items-center gap-1.5 font-bold text-brand-700 mb-1">
                    <HelpCircle className="h-3.5 w-3.5" />
                    <span>Giải thích chi tiết:</span>
                  </div>
                  <LatexRenderer content={review.explanation} />
                </div>
              )}
            </Card>
          );
        })}
      </div>

      {/* Submit Button */}
      {!result && (
        <div className="sticky bottom-4 z-20 flex justify-end">
          <Button
            size="lg"
            variant="primary"
            loading={submitting}
            onClick={handleSubmitQuiz}
            className="shadow-xl px-7 py-3 text-sm font-bold"
          >
            <Send className="h-4 w-4" />
            Nộp bài & Chấm điểm tức thì ({answeredCount}/{exam.totalQuestions})
          </Button>
        </div>
      )}
    </div>
  );
};
