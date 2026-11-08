import { httpClient } from '@/shared/api/httpClient';
import { ApiResponse, SpringPage } from '@/shared/types/api';

export interface AdminDashboardDto {
  totalGmv: number;
  platformFee: number;
  teacherAmount: number;
  totalUsers: number;
  studentCount: number;
  teacherCount: number;
  parentCount: number;
  adminCount: number;
  activeUsers: number;
  lockedUsers: number;
  pendingTeacherApprovals: number;
  totalCourses: number;
  publishedCourses: number;
  draftCourses: number;
  pendingCourseApprovals: number;
  rejectedCourses: number;
  totalEnrollments: number;
  openComplaints: number;
}

export interface AdminUserSummaryDto {
  id: string;
  email: string;
  fullName: string;
  role: string;
  status: string;
  teacherApprovalStatus?: string;
  emailVerified: boolean;
  createdAt: string;
}

export interface AdminUserDetailDto extends AdminUserSummaryDto {
  avatarUrl?: string;
  phone?: string;
  bio?: string;
  dateOfBirth?: string;
  failedLoginAttempts: number;
  lockedUntil?: string;
  mustChangePassword: boolean;
  createdByAdminId?: string;
  teacherContactNote?: string;
  updatedAt: string;
}

export interface AdminAuditLogDto {
  id: string;
  entityType: string;
  entityId?: string;
  action: string;
  actorId?: string;
  actorRole?: string;
  reason?: string;
  diffJson?: string;
  metadataJson?: string;
  createdAt: string;
}

export const adminApi = {
  async getDashboardSummary(): Promise<AdminDashboardDto> {
    const res = await httpClient.get<ApiResponse<AdminDashboardDto>>(
      '/admin/dashboard/summary'
    );
    return res.data.data;
  },

  async searchUsers(params?: {
    keyword?: string;
    role?: string;
    status?: string;
    page?: number;
    size?: number;
  }): Promise<SpringPage<AdminUserSummaryDto>> {
    const res = await httpClient.get<ApiResponse<SpringPage<AdminUserSummaryDto>>>(
      '/admin/users',
      { params }
    );
    return res.data.data;
  },

  async lockUser(userId: string, reason: string): Promise<void> {
    await httpClient.post(`/admin/users/${userId}/lock`, { reason });
  },

  async unlockUser(userId: string, reason?: string): Promise<void> {
    await httpClient.post(`/admin/users/${userId}/unlock`, { reason });
  },

  async approveTeacher(userId: string): Promise<void> {
    await httpClient.post(`/admin/users/${userId}/approve-teacher`, {});
  },

  async rejectTeacher(userId: string, reason: string): Promise<void> {
    await httpClient.post(`/admin/users/${userId}/reject-teacher`, { reason });
  },

  async provisionTeacher(payload: {
    email: string;
    temporaryPassword: string;
    fullName: string;
    contactNote?: string;
  }): Promise<AdminUserDetailDto> {
    const res = await httpClient.post<ApiResponse<AdminUserDetailDto>>(
      '/admin/users/provision-teacher',
      payload
    );
    return res.data.data;
  },

  async getAllSettings(): Promise<Record<string, string>> {
    const res = await httpClient.get<ApiResponse<Record<string, string>>>('/admin/settings');
    return res.data.data;
  },

  async toggleMaintenanceMode(payload: {
    enabled: boolean;
    message?: string;
  }): Promise<void> {
    await httpClient.post('/admin/settings/maintenance', payload);
  },

  async updateSetting(
    key: string,
    payload: { value: string; reason?: string }
  ): Promise<void> {
    await httpClient.put(`/admin/settings/${encodeURIComponent(key)}`, payload);
  },

  async getAuditLogs(params?: {
    entityType?: string;
    page?: number;
    size?: number;
  }): Promise<SpringPage<AdminAuditLogDto>> {
    const res = await httpClient.get<ApiResponse<SpringPage<AdminAuditLogDto>>>(
      '/admin/settings/audit-logs',
      { params }
    );
    return res.data.data;
  },
};
