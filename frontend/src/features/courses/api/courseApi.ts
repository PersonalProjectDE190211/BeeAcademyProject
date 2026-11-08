import { httpClient } from '@/shared/api/httpClient';
import { ApiResponse } from '@/shared/types/api';

export interface CategoryItem {
  id: string;
  name: string;
  slug: string;
  description?: string;
  iconUrl?: string;
  displayOrder: number;
  active: boolean;
}

export interface LessonItem {
  id: string;
  title: string;
  orderIndex: number;
  videoUrl?: string | null;
  durationSec: number;
  isFreePreview: boolean;
}

export interface ChapterItem {
  id: string;
  title: string;
  description?: string;
  orderIndex: number;
  lessons: LessonItem[];
}

export interface CourseSummary {
  id: string;
  teacherId: string;
  categoryId: string;
  title: string;
  slug: string;
  description: string;
  thumbnailUrl?: string;
  priceVnd: number;
  status: string;
  totalChapters: number;
  totalLessons: number;
  totalDurationSec: number;
  averageRating: number;
  totalReviews: number;
  publishedAt?: string;
  createdAt: string;
}

export interface CourseDetail extends CourseSummary {
  grades: number[];
  updatedAt: string;
  chapters: ChapterItem[];
}

export interface CourseReviewItem {
  id: string;
  courseId: string;
  studentId: string;
  rating: number;
  comment: string;
  createdAt: string;
}

export interface SearchCoursesParams {
  keyword?: string;
  categoryId?: string;
  grade?: number;
  minPrice?: number;
  maxPrice?: number;
  sortBy?: string;
}

export interface CreateOrUpdateCoursePayload {
  categoryId: string;
  title: string;
  description: string;
  thumbnailUrl?: string;
  priceVnd: number;
  grades: number[];
}

export interface AddChapterPayload {
  title: string;
  description?: string;
}

export interface AddLessonPayload {
  title: string;
  videoUrl: string;
  durationSec: number;
  isFreePreview: boolean;
}

export const courseApi = {
  async searchPublicCourses(params?: SearchCoursesParams): Promise<CourseSummary[]> {
    const res = await httpClient.get<ApiResponse<CourseSummary[]>>('/courses/public', { params });
    return res.data.data;
  },

  async getPublicCourseById(id: string): Promise<CourseDetail> {
    const res = await httpClient.get<ApiResponse<CourseDetail>>(`/courses/public/${id}`);
    return res.data.data;
  },

  async getPublicCourseBySlug(slug: string): Promise<CourseDetail> {
    const res = await httpClient.get<ApiResponse<CourseDetail>>(`/courses/public/slug/${slug}`);
    return res.data.data;
  },

  async getCategories(): Promise<CategoryItem[]> {
    const res = await httpClient.get<ApiResponse<CategoryItem[]>>('/courses/public/categories');
    return res.data.data;
  },

  async getCourseReviews(courseId: string): Promise<CourseReviewItem[]> {
    const res = await httpClient.get<ApiResponse<CourseReviewItem[]>>(
      `/courses/public/${courseId}/reviews`
    );
    return res.data.data;
  },

  async submitReview(
    courseId: string,
    payload: { rating: number; comment: string }
  ): Promise<CourseReviewItem> {
    const res = await httpClient.post<ApiResponse<CourseReviewItem>>(
      `/courses/${courseId}/reviews`,
      payload
    );
    return res.data.data;
  },

  // Teacher Course Studio APIs
  async getTeacherCourses(): Promise<CourseSummary[]> {
    const res = await httpClient.get<ApiResponse<CourseSummary[]>>('/teacher/courses');
    return res.data.data;
  },

  async getTeacherCourseDetail(id: string): Promise<CourseDetail> {
    const res = await httpClient.get<ApiResponse<CourseDetail>>(`/teacher/courses/${id}`);
    return res.data.data;
  },

  async createCourse(payload: CreateOrUpdateCoursePayload): Promise<CourseDetail> {
    const res = await httpClient.post<ApiResponse<CourseDetail>>('/teacher/courses', payload);
    return res.data.data;
  },

  async updateCourse(id: string, payload: CreateOrUpdateCoursePayload): Promise<CourseDetail> {
    const res = await httpClient.put<ApiResponse<CourseDetail>>(`/teacher/courses/${id}`, payload);
    return res.data.data;
  },

  async addChapter(courseId: string, payload: AddChapterPayload): Promise<CourseDetail> {
    const res = await httpClient.post<ApiResponse<CourseDetail>>(
      `/teacher/courses/${courseId}/chapters`,
      payload
    );
    return res.data.data;
  },

  async deleteChapter(courseId: string, chapterId: string): Promise<CourseDetail> {
    const res = await httpClient.delete<ApiResponse<CourseDetail>>(
      `/teacher/courses/${courseId}/chapters/${chapterId}`
    );
    return res.data.data;
  },

  async addLesson(
    courseId: string,
    chapterId: string,
    payload: AddLessonPayload
  ): Promise<CourseDetail> {
    const res = await httpClient.post<ApiResponse<CourseDetail>>(
      `/teacher/courses/${courseId}/chapters/${chapterId}/lessons`,
      payload
    );
    return res.data.data;
  },

  async deleteLesson(
    courseId: string,
    chapterId: string,
    lessonId: string
  ): Promise<CourseDetail> {
    const res = await httpClient.delete<ApiResponse<CourseDetail>>(
      `/teacher/courses/${courseId}/chapters/${chapterId}/lessons/${lessonId}`
    );
    return res.data.data;
  },

  async submitCourseForReview(courseId: string): Promise<CourseDetail> {
    const res = await httpClient.post<ApiResponse<CourseDetail>>(
      `/teacher/courses/${courseId}/submit-review`,
      {}
    );
    return res.data.data;
  },

  // Admin Course Approval APIs
  async getPendingCourses(): Promise<CourseSummary[]> {
    const res = await httpClient.get<ApiResponse<CourseSummary[]>>('/admin/courses/pending');
    return res.data.data;
  },

  async approveCourse(courseId: string, reasonOrComment?: string): Promise<CourseDetail> {
    const res = await httpClient.post<ApiResponse<CourseDetail>>(
      `/admin/courses/${courseId}/approve`,
      { reasonOrComment }
    );
    return res.data.data;
  },

  async rejectCourse(courseId: string, reasonOrComment: string): Promise<CourseDetail> {
    const res = await httpClient.post<ApiResponse<CourseDetail>>(
      `/admin/courses/${courseId}/reject`,
      { reasonOrComment }
    );
    return res.data.data;
  },

  async requestRevision(courseId: string, reasonOrComment: string): Promise<CourseDetail> {
    const res = await httpClient.post<ApiResponse<CourseDetail>>(
      `/admin/courses/${courseId}/request-revision`,
      { reasonOrComment }
    );
    return res.data.data;
  },
};
