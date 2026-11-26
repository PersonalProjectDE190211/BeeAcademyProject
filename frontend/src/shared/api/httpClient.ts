import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '../store/useAuthStore';
import type { ApiResponse, UserProfile } from '../types/api';

interface RetryableRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

interface RefreshPayload {
  accessToken: string;
  user: UserProfile;
}

function resolveApiBase(): string {
  const raw = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.trim();
  if (raw) {
    const normalized = raw.replace(/\/+$/, '').replace(/\/api$/i, '');
    return `${normalized}/api`;
  }
  if (import.meta.env.PROD) {
    return 'https://beeacademyproject.onrender.com/api';
  }
  return '/api';
}

const API_BASE = resolveApiBase();

export const httpClient = axios.create({
  baseURL: API_BASE,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

httpClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let refreshPromise: Promise<string | null> | null = null;

async function refreshAccessToken(): Promise<string | null> {
  try {
    const response = await axios.post<ApiResponse<RefreshPayload>>(
      `${API_BASE}/auth/refresh`,
      {},
      { withCredentials: true }
    );
    const payload = response.data?.data;
    if (payload?.accessToken && payload?.user) {
      useAuthStore.getState().setSession(payload.accessToken, payload.user);
      return payload.accessToken;
    }
    useAuthStore.getState().clearSession();
    return null;
  } catch {
    useAuthStore.getState().clearSession();
    return null;
  }
}

httpClient.interceptors.response.use(
  (response) => {
    const contentType = String(response.headers?.['content-type'] || '').toLowerCase();
    const isHtmlPayload =
      contentType.includes('text/html') ||
      (typeof response.data === 'string' && /^\s*(<!doctype html|<html)/i.test(response.data));

    if (isHtmlPayload) {
      return Promise.reject(
        new Error(
          'Không thể kết nối tới máy chủ Backend API (nhận phản hồi HTML tĩnh thay vì JSON). Vui lòng kiểm tra cấu hình VITE_API_BASE_URL hoặc trạng thái dịch vụ Render.'
        )
      );
    }
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryableRequestConfig | undefined;
    const isAuthEndpoint =
      originalRequest?.url?.includes('/auth/login') ||
      originalRequest?.url?.includes('/auth/register') ||
      originalRequest?.url?.includes('/auth/refresh');

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry && !isAuthEndpoint) {
      originalRequest._retry = true;
      if (!refreshPromise) {
        refreshPromise = refreshAccessToken().finally(() => {
          refreshPromise = null;
        });
      }
      const newToken = await refreshPromise;
      if (newToken && originalRequest.headers) {
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return httpClient(originalRequest);
      }
    }

    return Promise.reject(error);
  }
);
