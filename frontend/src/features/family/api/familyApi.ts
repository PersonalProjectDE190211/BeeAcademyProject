import { httpClient } from '@/shared/api/httpClient';
import { ApiResponse } from '@/shared/types/api';

export interface ParentLinkDto {
  id: string;
  parentId: string;
  studentId: string;
  studentEmail: string;
  inviteToken?: string;
  status: string;
  consentGranted: boolean;
  inviteExpiresAt?: string;
  linkedAt?: string;
  unlinkedAt?: string;
  unlinkReason?: string;
}

export interface StudentCourseProgressDto {
  courseId: string;
  courseTitle: string;
  progressPercent: number;
  completed: boolean;
  lastAccessedAt?: string;
}

export interface StudentProgressReportDto {
  studentId: string;
  studentEmail: string;
  enrolledCoursesCount: number;
  completedCoursesCount: number;
  courses: StudentCourseProgressDto[];
}

export const familyApi = {
  // Parent Portal
  async inviteStudent(studentEmail: string): Promise<ParentLinkDto> {
    const res = await httpClient.post<ApiResponse<ParentLinkDto>>('/parent/links/invite', {
      studentEmail,
    });
    return res.data.data;
  },

  async getParentLinks(): Promise<ParentLinkDto[]> {
    const res = await httpClient.get<ApiResponse<ParentLinkDto[]>>('/parent/links');
    return res.data.data;
  },

  async getStudentProgress(studentId: string): Promise<StudentProgressReportDto> {
    const res = await httpClient.get<ApiResponse<StudentProgressReportDto>>(
      `/parent/students/${studentId}/progress`
    );
    return res.data.data;
  },

  async parentUnlink(linkId: string, reason?: string): Promise<ParentLinkDto> {
    const res = await httpClient.post<ApiResponse<ParentLinkDto>>(
      `/parent/links/${linkId}/unlink`,
      { reason }
    );
    return res.data.data;
  },

  // Student Family Links
  async getStudentLinks(): Promise<ParentLinkDto[]> {
    const res = await httpClient.get<ApiResponse<ParentLinkDto[]>>('/student/family/links');
    return res.data.data;
  },

  async acceptInvite(inviteToken: string): Promise<ParentLinkDto> {
    const res = await httpClient.post<ApiResponse<ParentLinkDto>>(
      '/student/family/links/accept',
      { inviteToken }
    );
    return res.data.data;
  },

  async rejectInvite(inviteToken: string, reason?: string): Promise<ParentLinkDto> {
    const res = await httpClient.post<ApiResponse<ParentLinkDto>>(
      '/student/family/links/reject',
      { inviteToken, reason }
    );
    return res.data.data;
  },

  async studentUnlink(linkId: string, reason?: string): Promise<ParentLinkDto> {
    const res = await httpClient.post<ApiResponse<ParentLinkDto>>(
      `/student/family/links/${linkId}/unlink`,
      { reason }
    );
    return res.data.data;
  },
};
