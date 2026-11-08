import React, { useEffect, useState } from 'react';
import { Link, NavLink, Navigate, Outlet, useNavigate } from 'react-router-dom';
import {
  AlertTriangle,
  Award,
  Bell,
  BookOpen,
  FileQuestion,
  GraduationCap,
  LayoutDashboard,
  LifeBuoy,
  LogOut,
  Receipt,
  Settings,
  ShieldCheck,
  UserCheck,
  Users,
  Wallet,
} from 'lucide-react';
import { httpClient } from '../../shared/api/httpClient';
import { useAuthStore } from '../../shared/store/useAuthStore';
import type { ApiResponse, UserRole } from '../../shared/types/api';
import { Badge, Button } from '../../shared/ui/primitives';

interface MaintenanceStatus {
  enabled: boolean;
  message: string;
}

interface NavItem {
  to: string;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
  roles?: UserRole[];
}

const NAV_ITEMS: NavItem[] = [
  { to: '/courses', label: 'Khóa học K-12', icon: BookOpen },
  { to: '/student/learning', label: 'Góc học tập', icon: GraduationCap, roles: ['student'] },
  { to: '/student/certificates', label: 'Chứng chỉ số', icon: Award, roles: ['student'] },
  { to: '/student/orders', label: 'Đơn hàng & Học phí', icon: Receipt, roles: ['student'] },
  { to: '/student/family', label: 'Liên kết Phụ huynh', icon: Users, roles: ['student'] },
  { to: '/parent/portal', label: 'Cổng Phụ huynh', icon: UserCheck, roles: ['parent'] },
  { to: '/teacher/courses', label: 'Soạn giảng khóa học', icon: BookOpen, roles: ['teacher'] },
  { to: '/teacher/questions', label: 'Ngân hàng câu hỏi', icon: FileQuestion, roles: ['teacher'] },
  { to: '/teacher/revenue', label: 'Doanh thu & Ngân hàng', icon: Wallet, roles: ['teacher'] },
  { to: '/admin/dashboard', label: 'Dashboard Quản trị', icon: LayoutDashboard, roles: ['admin'] },
  { to: '/admin/courses', label: 'Duyệt khóa học', icon: ShieldCheck, roles: ['admin'] },
  { to: '/admin/users', label: 'Quản lý tài khoản', icon: Users, roles: ['admin'] },
  { to: '/admin/payouts', label: 'Đối soát & Chi trả GV', icon: Wallet, roles: ['admin'] },
  { to: '/admin/settings', label: 'Cấu hình & Kiểm toán', icon: Settings, roles: ['admin'] },
  { to: '/support/complaints', label: 'Hỗ trợ & Khiếu nại', icon: LifeBuoy, roles: ['student', 'teacher', 'parent', 'admin'] },
  { to: '/notifications', label: 'Thông báo', icon: Bell, roles: ['student', 'teacher', 'parent', 'admin'] },
];

const ROLE_LABELS: Record<UserRole, string> = {
  student: 'Học sinh',
  teacher: 'Giáo viên',
  parent: 'Phụ huynh',
  admin: 'Quản trị viên',
};

export function AppShell() {
  const { user, clearSession } = useAuthStore();
  const navigate = useNavigate();
  const [maintenance, setMaintenance] = useState<MaintenanceStatus | null>(null);

  useEffect(() => {
    let active = true;
    httpClient
      .get<ApiResponse<MaintenanceStatus>>('/public/system/maintenance')
      .then((res) => {
        if (active && res.data?.data) {
          setMaintenance(res.data.data);
        }
      })
      .catch(() => {
        // Ignore network status error when backend is offline
      });
    return () => {
      active = false;
    };
  }, []);

  const handleLogout = async () => {
    try {
      await httpClient.post('/auth/logout');
    } catch {
      // Clear local session regardless
    } finally {
      clearSession();
      navigate('/login');
    }
  };

  const visibleNavItems = NAV_ITEMS.filter((item) => {
    if (!item.roles) return true;
    if (!user) return false;
    return item.roles.includes(user.role);
  });

  if (maintenance?.enabled && user?.role !== 'admin') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-amber-50/60 p-6">
        <div className="max-w-md rounded-2xl border border-amber-300 bg-white p-8 text-center shadow-lg">
          <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-amber-100 text-amber-800">
            <AlertTriangle className="h-6 w-6" />
          </div>
          <h1 className="text-lg font-bold text-slate-900">Hệ thống đang bảo trì</h1>
          <p className="mt-2 text-sm text-slate-600">{maintenance.message}</p>
          <div className="mt-6 flex justify-center gap-3">
            <Link to="/login">
              <Button size="sm" variant="outline">
                Đăng nhập Quản trị viên
              </Button>
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col bg-surface text-on-surface">
      {maintenance?.enabled && (
        <div className="bg-amber-500 px-4 py-2 text-center text-xs font-semibold text-slate-950">
          [CHẾ ĐỘ BẢO TRÌ ĐANG BẬT] {maintenance.message} — Chỉ Quản trị viên mới có thể thao tác.
        </div>
      )}

      <header className="sticky top-0 z-30 border-b border-slate-200/80 bg-white/95 backdrop-blur-xs">
        <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6">
          <div className="flex items-center gap-6">
            <Link to="/courses" className="flex items-center gap-2.5">
              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-primary text-white font-bold shadow-xs">
                B
              </div>
              <div>
                <span className="text-base font-extrabold tracking-tight text-slate-900">
                  BeeAcademy
                </span>
                <span className="block text-[10px] font-medium text-slate-500">
                  Nền tảng Học tập Trực tuyến K-12
                </span>
              </div>
            </Link>
          </div>

          <div className="flex items-center gap-3">
            {user ? (
              <>
                <Link
                  to="/profile"
                  className="flex items-center gap-2.5 rounded-xl border border-slate-200 bg-slate-50/70 px-3 py-1.5 hover:bg-slate-100"
                >
                  <div className="flex h-7 w-7 items-center justify-center rounded-full bg-primary-fixed text-xs font-bold text-primary">
                    {user.fullName?.charAt(0)?.toUpperCase() || 'U'}
                  </div>
                  <div className="text-left">
                    <p className="text-xs font-semibold text-slate-900 leading-tight">
                      {user.fullName}
                    </p>
                    <p className="text-[10px] text-slate-500">{ROLE_LABELS[user.role]}</p>
                  </div>
                </Link>
                {user.role === 'teacher' && user.teacherApprovalStatus !== 'approved' && (
                  <Badge tone="warning">Chờ duyệt GV</Badge>
                )}
                <Button size="sm" variant="ghost" onClick={handleLogout} title="Đăng xuất">
                  <LogOut className="h-4 w-4" />
                </Button>
              </>
            ) : (
              <div className="flex items-center gap-2">
                <Link to="/login">
                  <Button size="sm" variant="ghost">
                    Đăng nhập
                  </Button>
                </Link>
                <Link to="/register">
                  <Button size="sm" variant="primary">
                    Đăng ký học
                  </Button>
                </Link>
              </div>
            )}
          </div>
        </div>
      </header>

      <div className="mx-auto flex w-full max-w-7xl flex-1 gap-6 px-4 py-6 sm:px-6">
        <aside className="hidden w-60 shrink-0 md:block">
          <nav className="sticky top-22 space-y-1 rounded-2xl border border-slate-200/80 bg-white p-3 shadow-2xs">
            {visibleNavItems.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) =>
                    `flex items-center gap-2.5 rounded-xl px-3 py-2 text-xs font-semibold transition-colors ${
                      isActive
                        ? 'bg-primary text-white shadow-2xs'
                        : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                    }`
                  }
                >
                  <Icon className="h-4 w-4 shrink-0" />
                  <span>{item.label}</span>
                </NavLink>
              );
            })}
          </nav>
        </aside>

        <main className="min-w-0 flex-1">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export function RequireAuth({
  allowedRoles,
  children,
}: {
  allowedRoles?: UserRole[];
  children: React.ReactNode;
}) {
  const { user, accessToken } = useAuthStore();

  if (!accessToken || !user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/courses" replace />;
  }

  return <>{children}</>;
}
