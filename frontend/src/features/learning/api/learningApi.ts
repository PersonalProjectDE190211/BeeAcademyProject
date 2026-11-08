import { httpClient } from '@/shared/api/httpClient';
import { ApiResponse } from '@/shared/types/api';

export interface EnrollmentSummary {
  id: string;
  courseId: string;
  courseTitle: string;
  courseSlug: string;
  courseThumbnailUrl?: string;
  completedLessons: number;
  totalLessons: number;
  progressPercent: number;
  completed: boolean;
  enrolledAt: string;
  completedAt?: string;
}

export interface LearningLesson {
  lessonId: string;
  chapterId: string;
  title: string;
  orderIndex: number;
  videoUrl: string;
  durationSec: number;
  isFreePreview: boolean;
  isCompleted: boolean;
  lastPositionSec: number;
  watchPercent: number;
}

export interface LessonProgressUpdate {
  lessonId: string;
  courseId: string;
  watchedSeconds: number;
  lastPositionSec: number;
  watchPercent: number;
  isCompleted: boolean;
  completedAt?: string;
  courseProgressPercent: number;
  courseCompleted: boolean;
}

export interface QaMessageItem {
  id: string;
  threadId: string;
  senderId: string;
  content: string;
  createdAt: string;
}

export interface QaThreadItem {
  id: string;
  courseId: string;
  lessonId: string;
  studentId: string;
  title: string;
  content: string;
  isResolved: boolean;
  createdAt: string;
  updatedAt: string;
  messages: QaMessageItem[];
}

export interface CertificateItem {
  id: string;
  certificateCode: string;
  studentId: string;
  courseId: string;
  studentName: string;
  courseTitle: string;
  issuedAt: string;
  pdfUrl?: string;
  verifyUrl: string;
}

export const learningApi = {
  async getMyCourses(): Promise<EnrollmentSummary[]> {
    const res = await httpClient.get<ApiResponse<EnrollmentSummary[]>>('/learning/my-courses');
    return res.data.data;
  },

  async getLearningLesson(courseId: string, lessonId: string): Promise<LearningLesson> {
    const res = await httpClient.get<ApiResponse<LearningLesson>>(
      `/learning/courses/${courseId}/lessons/${lessonId}`
    );
    return res.data.data;
  },

  async trackProgress(
    courseId: string,
    lessonId: string,
    payload: { startSec: number; endSec: number; currentPositionSec: number }
  ): Promise<LessonProgressUpdate> {
    const res = await httpClient.post<ApiResponse<LessonProgressUpdate>>(
      `/learning/courses/${courseId}/lessons/${lessonId}/progress`,
      payload
    );
    return res.data.data;
  },

  async getLessonDiscussions(lessonId: string): Promise<QaThreadItem[]> {
    const res = await httpClient.get<ApiResponse<QaThreadItem[]>>(
      `/learning/lessons/${lessonId}/discussions`
    );
    return res.data.data;
  },

  async askQuestion(
    courseId: string,
    lessonId: string,
    payload: { title: string; content: string }
  ): Promise<QaThreadItem> {
    const res = await httpClient.post<ApiResponse<QaThreadItem>>(
      `/learning/courses/${courseId}/lessons/${lessonId}/discussions`,
      payload
    );
    return res.data.data;
  },

  async replyQuestion(threadId: string, content: string): Promise<QaThreadItem> {
    const res = await httpClient.post<ApiResponse<QaThreadItem>>(
      `/learning/discussions/${threadId}/replies`,
      { content }
    );
    return res.data.data;
  },

  async getMyCertificates(): Promise<CertificateItem[]> {
    const res = await httpClient.get<ApiResponse<CertificateItem[]>>('/certificates/my');
    return res.data.data;
  },

  async downloadCertificatePdf(certificateId: string): Promise<Blob> {
    const res = await httpClient.get(`/certificates/${certificateId}/download`, {
      responseType: 'blob',
    });
    return res.data as Blob;
  },

  async verifyCertificateByCode(code: string): Promise<CertificateItem> {
    const res = await httpClient.get<ApiResponse<CertificateItem>>(
      `/certificates/public/verify/${encodeURIComponent(code)}`
    );
    return res.data.data;
  },
};
