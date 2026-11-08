import { httpClient } from '@/shared/api/httpClient';
import { ApiResponse } from '@/shared/types/api';

export interface ComplaintMessageDto {
  id: string;
  authorId: string;
  authorRole: string;
  content: string;
  createdAt: string;
}

export interface ComplaintDto {
  id: string;
  senderId: string;
  senderRole: string;
  title: string;
  category: string;
  priority: string;
  status: string;
  createdAt: string;
  lastActivityAt: string;
  resolvedAt?: string;
  resolvedByAdminId?: string;
  resolutionSummary?: string;
  messages: ComplaintMessageDto[];
}

export interface NotificationDto {
  id: string;
  recipientId: string;
  title: string;
  message: string;
  type: string;
  actionUrl?: string;
  read: boolean;
  createdAt: string;
  readAt?: string;
}

export const supportApi = {
  async createComplaint(payload: {
    title: string;
    category: string;
    priority: string;
    content: string;
  }): Promise<ComplaintDto> {
    const res = await httpClient.post<ApiResponse<ComplaintDto>>('/complaints', payload);
    return res.data.data;
  },

  async getMyComplaints(): Promise<ComplaintDto[]> {
    const res = await httpClient.get<ApiResponse<ComplaintDto[]>>('/complaints/my');
    return res.data.data;
  },

  async addMessage(complaintId: string, content: string): Promise<ComplaintDto> {
    const res = await httpClient.post<ApiResponse<ComplaintDto>>(
      `/complaints/${complaintId}/messages`,
      { content }
    );
    return res.data.data;
  },

  async closeComplaint(complaintId: string): Promise<ComplaintDto> {
    const res = await httpClient.post<ApiResponse<ComplaintDto>>(
      `/complaints/${complaintId}/close`,
      {}
    );
    return res.data.data;
  },

  // Admin Complaints
  async getAllComplaints(status?: string): Promise<ComplaintDto[]> {
    const res = await httpClient.get<ApiResponse<ComplaintDto[]>>('/admin/complaints', {
      params: { status },
    });
    return res.data.data;
  },

  async resolveComplaint(
    complaintId: string,
    resolutionSummary: string
  ): Promise<ComplaintDto> {
    const res = await httpClient.post<ApiResponse<ComplaintDto>>(
      `/admin/complaints/${complaintId}/resolve`,
      { resolutionSummary }
    );
    return res.data.data;
  },

  // Notifications
  async getMyNotifications(): Promise<NotificationDto[]> {
    const res = await httpClient.get<ApiResponse<NotificationDto[]>>('/notifications');
    return res.data.data;
  },

  async markNotificationRead(id: string): Promise<NotificationDto> {
    const res = await httpClient.post<ApiResponse<NotificationDto>>(
      `/notifications/${id}/read`,
      {}
    );
    return res.data.data;
  },
};
