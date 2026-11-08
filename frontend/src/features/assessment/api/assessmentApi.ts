import { httpClient } from '@/shared/api/httpClient';
import { ApiResponse } from '@/shared/types/api';

export interface ChoiceItemDto {
  id?: string;
  content: string;
  imageUrl?: string;
  isCorrect: boolean;
  position: number;
}

export interface QuestionItem {
  id: string;
  teacherId: string;
  categoryId: string;
  chapterId: string;
  grade: number;
  content: string;
  explanation?: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  type: 'MULTIPLE_CHOICE' | 'TRUE_FALSE';
  status: string;
  usageCount: number;
  createdAt: string;
  updatedAt: string;
  choices: ChoiceItemDto[];
}

export interface CreateQuestionPayload {
  categoryId: string;
  chapterId: string;
  grade: number;
  content: string;
  explanation?: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  type: 'MULTIPLE_CHOICE' | 'TRUE_FALSE';
  choices: ChoiceItemDto[];
  tags?: string[];
}

export interface QuizConfigDto {
  id?: string;
  chapterId: string;
  teacherId?: string;
  totalQuestions: number;
  easyCount: number;
  mediumCount: number;
  hardCount: number;
  timeLimitMinutes: number;
  passingScore: number;
  shuffleQuestions: boolean;
  shuffleChoices: boolean;
  maxAttempts: number;
}

export interface ExamChoiceDto {
  choiceId: string;
  content: string;
  imageUrl?: string;
  position: number;
}

export interface ExamQuestionDto {
  questionId: string;
  content: string;
  type: string;
  choices: ExamChoiceDto[];
}

export interface StartQuizDto {
  attemptId: string;
  chapterId: string;
  totalQuestions: number;
  timeLimitMinutes?: number;
  passingScore: number;
  startedAt: string;
  questions: ExamQuestionDto[];
}

export interface QuestionReviewDto {
  questionId: string;
  content: string;
  explanation?: string;
  selectedChoiceId?: string;
  correctChoiceId: string;
  isCorrect: boolean;
}

export interface QuizResultDto {
  attemptId: string;
  studentId: string;
  chapterId: string;
  score: number;
  passed: boolean;
  totalQuestions: number;
  correctCount: number;
  submittedAt: string;
  reviews: QuestionReviewDto[];
}

export const assessmentApi = {
  async getTeacherQuestions(): Promise<QuestionItem[]> {
    const res = await httpClient.get<ApiResponse<QuestionItem[]>>('/teacher/questions');
    return res.data.data;
  },

  async createQuestion(payload: CreateQuestionPayload): Promise<QuestionItem> {
    const res = await httpClient.post<ApiResponse<QuestionItem>>('/teacher/questions', payload);
    return res.data.data;
  },

  async deactivateQuestion(id: string, reason?: string): Promise<void> {
    await httpClient.delete(`/teacher/questions/${id}`, { params: { reason } });
  },

  async getQuizConfig(chapterId: string): Promise<QuizConfigDto> {
    const res = await httpClient.get<ApiResponse<QuizConfigDto>>(
      `/teacher/quiz-configs/chapter/${chapterId}`
    );
    return res.data.data;
  },

  async saveQuizConfig(payload: QuizConfigDto): Promise<QuizConfigDto> {
    const res = await httpClient.post<ApiResponse<QuizConfigDto>>(
      '/teacher/quiz-configs',
      payload
    );
    return res.data.data;
  },

  async startQuiz(chapterId: string): Promise<StartQuizDto> {
    const res = await httpClient.post<ApiResponse<StartQuizDto>>(
      `/student/quizzes/chapter/${chapterId}/start`,
      {}
    );
    return res.data.data;
  },

  async submitQuiz(payload: {
    attemptId: string;
    answers: Record<string, string>;
  }): Promise<QuizResultDto> {
    const res = await httpClient.post<ApiResponse<QuizResultDto>>(
      '/student/quizzes/submit',
      payload
    );
    return res.data.data;
  },
};
