import axios from 'axios';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface SpringPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export type UserRole = 'student' | 'teacher' | 'parent' | 'admin';

export type TeacherApprovalStatus = 'none' | 'pending' | 'approved' | 'rejected';

export interface UserProfile {
  id: string;
  email: string;
  role: UserRole;
  fullName: string;
  avatarUrl: string | null;
  phone: string | null;
  bio: string | null;
  grade?: number | null;
  teacherApprovalStatus: TeacherApprovalStatus;
  isBlocked: boolean;
  createdAt: string;
}

export function getApiErrorMessage(error: unknown, fallback = 'Đã xảy ra lỗi không xác định'): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string; detail?: string } | undefined;
    if (data?.message && typeof data.message === 'string') {
      return data.message;
    }
    if (data?.detail && typeof data.detail === 'string') {
      return data.detail;
    }
    if (error.message) {
      return error.message;
    }
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

export function formatVnd(amount: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(amount || 0);
}
