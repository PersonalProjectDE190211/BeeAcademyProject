import { httpClient } from '@/shared/api/httpClient';
import { ApiResponse, UserProfile } from '@/shared/types/api';

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  email: string;
  password: string;
  fullName: string;
  role: 'STUDENT' | 'PARENT' | 'student' | 'parent';
}

export interface GoogleAuthPayload {
  idToken: string;
  accountType?: 'student' | 'parent';
}

export interface AuthResponseData {
  accessToken: string;
  user: UserProfile;
}

export interface UpdateProfilePayload {
  fullName: string;
  phone?: string;
  bio?: string;
}

export interface ChangePasswordPayload {
  oldPassword: string;
  newPassword: string;
}

export interface ResetPasswordPayload {
  email: string;
  otp: string;
  newPassword: string;
}

export const authApi = {
  async login(payload: LoginPayload): Promise<AuthResponseData> {
    const res = await httpClient.post<ApiResponse<AuthResponseData>>('/auth/login', payload);
    return res.data.data;
  },

  async loginWithGoogle(payload: GoogleAuthPayload): Promise<AuthResponseData> {
    const res = await httpClient.post<ApiResponse<AuthResponseData>>('/auth/google', payload);
    return res.data.data;
  },

  async register(payload: RegisterPayload): Promise<UserProfile> {
    const res = await httpClient.post<ApiResponse<UserProfile>>('/auth/register', payload);
    return res.data.data;
  },

  async logout(): Promise<void> {
    await httpClient.post('/auth/logout', {});
  },

  async forgotPassword(email: string): Promise<void> {
    await httpClient.post('/auth/forgot-password', { email });
  },

  async resetPassword(payload: ResetPasswordPayload): Promise<void> {
    await httpClient.post('/auth/reset-password', payload);
  },

  async getMyProfile(): Promise<UserProfile> {
    const res = await httpClient.get<ApiResponse<UserProfile>>('/profile/me');
    return res.data.data;
  },

  async updateMyProfile(payload: UpdateProfilePayload): Promise<UserProfile> {
    const res = await httpClient.put<ApiResponse<UserProfile>>('/profile/me', payload);
    return res.data.data;
  },

  async changePassword(payload: ChangePasswordPayload): Promise<void> {
    await httpClient.put('/profile/me/password', payload);
  },

  async updateAvatar(avatarUrl: string): Promise<UserProfile> {
    const res = await httpClient.put<ApiResponse<UserProfile>>('/profile/me/avatar', { avatarUrl });
    return res.data.data;
  },
};
