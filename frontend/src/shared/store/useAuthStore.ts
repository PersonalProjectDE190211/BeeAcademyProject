import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { UserProfile, UserRole } from '../types/api';

interface AuthState {
  accessToken: string | null;
  user: UserProfile | null;
  setSession: (accessToken: string, user: UserProfile) => void;
  updateUser: (user: UserProfile) => void;
  clearSession: () => void;
  hasRole: (...roles: UserRole[]) => boolean;
}

function normalizeProfile(user: UserProfile): UserProfile {
  return {
    ...user,
    role: (user.role?.toLowerCase() ?? 'student') as UserRole,
  };
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      user: null,
      setSession: (accessToken, user) =>
        set({
          accessToken,
          user: normalizeProfile(user),
        }),
      updateUser: (user) =>
        set({
          user: normalizeProfile(user),
        }),
      clearSession: () =>
        set({
          accessToken: null,
          user: null,
        }),
      hasRole: (...roles) => {
        const currentRole = get().user?.role?.toLowerCase() as UserRole | undefined;
        if (!currentRole) return false;
        return roles.includes(currentRole);
      },
    }),
    {
      name: 'bee-academy-auth',
    }
  )
);
