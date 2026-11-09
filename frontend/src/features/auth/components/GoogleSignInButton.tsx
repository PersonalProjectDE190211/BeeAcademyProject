import React, { useEffect, useRef, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import toast from 'react-hot-toast';
import { authApi } from '../api/authApi';
import { useAuthStore } from '@/shared/store/useAuthStore';
import { getApiErrorMessage } from '@/shared/types/api';

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: {
            client_id: string;
            callback: (response: { credential: string }) => void;
            auto_select?: boolean;
            cancel_on_tap_outside?: boolean;
          }) => void;
          renderButton: (
            parent: HTMLElement,
            options: {
              type?: 'standard' | 'icon';
              theme?: 'outline' | 'filled_blue' | 'filled_black';
              size?: 'large' | 'medium' | 'small';
              text?: 'signin_with' | 'signup_with' | 'continue_with' | 'signin';
              shape?: 'rectangular' | 'pill' | 'circle' | 'square';
              logo_alignment?: 'left' | 'center';
              width?: number;
              locale?: string;
            }
          ) => void;
          prompt: () => void;
        };
      };
    };
  }
}

interface GoogleSignInButtonProps {
  mode?: 'signin' | 'signup';
  accountType?: 'student' | 'parent';
  className?: string;
}

export const GoogleSignInButton: React.FC<GoogleSignInButtonProps> = ({
  mode = 'signin',
  accountType = 'student',
  className = '',
}) => {
  const navigate = useNavigate();
  const location = useLocation();
  const setSession = useAuthStore((s) => s.setSession);

  const [loading, setLoading] = useState(false);
  const [showDevModal, setShowDevModal] = useState(false);
  const [devEmail, setDevEmail] = useState('hocsinh.google@gmail.com');
  const [devFullName, setDevFullName] = useState('Học Sinh Google');

  const googleButtonRef = useRef<HTMLDivElement>(null);
  const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || '';
  const isRealClientIdConfigured =
    clientId &&
    !clientId.includes('your-google-client-id') &&
    clientId.endsWith('.apps.googleusercontent.com');

  const handleAuthSuccess = (accessToken: string, user: any) => {
    setSession(accessToken, user);
    toast.success(`Đăng nhập Google thành công! Chào mừng ${user.fullName || user.email}`);

    const from = (location.state as { from?: string } | null)?.from;
    if (from) {
      navigate(from, { replace: true });
    } else {
      const role = (user.role || 'student').toLowerCase();
      if (role === 'admin') navigate('/admin/dashboard', { replace: true });
      else if (role === 'teacher') navigate('/teacher/courses', { replace: true });
      else if (role === 'parent') navigate('/parent/portal', { replace: true });
      else navigate('/learning/my-courses', { replace: true });
    }
  };

  const processGoogleToken = async (idToken: string) => {
    setLoading(true);
    try {
      const result = await authApi.loginWithGoogle({
        idToken,
        accountType: mode === 'signup' ? accountType : undefined,
      });
      handleAuthSuccess(result.accessToken, result.user);
    } catch (err) {
      const msg = getApiErrorMessage(err, 'Đăng nhập Google không thành công. Vui lòng thử lại.');
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!isRealClientIdConfigured) return;

    const loadGsiScript = () => {
      if (window.google?.accounts?.id) {
        initGoogleSignIn();
        return;
      }

      const existingScript = document.getElementById('google-gsi-client');
      if (!existingScript) {
        const script = document.createElement('script');
        script.id = 'google-gsi-client';
        script.src = 'https://accounts.google.com/gsi/client';
        script.async = true;
        script.defer = true;
        script.onload = () => initGoogleSignIn();
        document.body.appendChild(script);
      }
    };

    const initGoogleSignIn = () => {
      if (!window.google?.accounts?.id || !googleButtonRef.current) return;

      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: (response) => {
          if (response.credential) {
            processGoogleToken(response.credential);
          }
        },
      });

      window.google.accounts.id.renderButton(googleButtonRef.current, {
        type: 'standard',
        theme: 'outline',
        size: 'large',
        text: mode === 'signup' ? 'signup_with' : 'signin_with',
        shape: 'rectangular',
        logo_alignment: 'left',
        width: 380,
        locale: 'vi',
      });
    };

    loadGsiScript();
  }, [clientId, isRealClientIdConfigured, mode, accountType]);

  const handleDevMockLogin = (e: React.FormEvent) => {
    e.preventDefault();
    if (!devEmail.trim()) {
      toast.error('Vui lòng nhập email');
      return;
    }
    const mockSub = 'google_sub_' + Math.abs(hashCode(devEmail.trim()));
    const mockToken = `mock_google_token:${mockSub}:${devEmail.trim().toLowerCase()}:true:${encodeURIComponent(
      devFullName.trim()
    )}`;
    setShowDevModal(false);
    processGoogleToken(mockToken);
  };

  const handleButtonClick = () => {
    if (isRealClientIdConfigured && window.google?.accounts?.id) {
      window.google.accounts.id.prompt();
    } else {
      // Khi chưa cấu hình Client ID thật hoặc đang dev localhost
      setShowDevModal(true);
    }
  };

  return (
    <div className={`w-full ${className}`}>
      {isRealClientIdConfigured ? (
        <div className="flex justify-center">
          <div ref={googleButtonRef} />
        </div>
      ) : (
        <button
          type="button"
          onClick={handleButtonClick}
          disabled={loading}
          className="relative flex w-full items-center justify-center gap-3 rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-xs transition hover:bg-slate-50 hover:border-slate-400 active:scale-[0.99] disabled:opacity-60 cursor-pointer"
        >
          <svg className="h-5 w-5" viewBox="0 0 24 24">
            <path
              fill="#4285F4"
              d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
            />
            <path
              fill="#34A853"
              d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
            />
            <path
              fill="#FBBC05"
              d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
            />
            <path
              fill="#EA4335"
              d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
            />
          </svg>
          <span>
            {loading
              ? 'Đang xác thực với Google...'
              : mode === 'signup'
              ? 'Đăng ký nhanh bằng Google'
              : 'Đăng nhập bằng Google'}
          </span>
        </button>
      )}

      {/* Dev Mode Modal - Cho phép lập trình viên kiểm thử Google Auth ngay lập tức */}
      {showDevModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 backdrop-blur-xs">
          <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl border border-slate-200 animate-in fade-in zoom-in-95 duration-150">
            <div className="flex items-center gap-2 mb-3">
              <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-50 text-blue-600">
                <svg className="h-5 w-5" viewBox="0 0 24 24">
                  <path
                    fill="#4285F4"
                    d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                  />
                  <path
                    fill="#34A853"
                    d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                  />
                  <path
                    fill="#FBBC05"
                    d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
                  />
                  <path
                    fill="#EA4335"
                    d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
                  />
                </svg>
              </div>
              <div>
                <h3 className="font-bold text-slate-900">Mô phỏng Đăng nhập Google (Dev Mode)</h3>
                <p className="text-xs text-slate-500">Kiểm thử toàn diện luồng xác thực Google</p>
              </div>
            </div>

            <p className="text-xs text-slate-600 mb-4 bg-slate-50 p-2.5 rounded-lg border border-slate-200">
              Chưa cấu hình Google Client ID thật trong <code className="text-amber-600 font-semibold">.env</code>. Hệ thống sẽ sinh Google ID Token hợp lệ để backend giải mã và liên kết qua <code className="text-amber-600 font-semibold">ExternalLogin</code>.
            </p>

            <form onSubmit={handleDevMockLogin} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Email tài khoản Google
                </label>
                <input
                  type="email"
                  value={devEmail}
                  onChange={(e) => setDevEmail(e.target.value)}
                  className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-amber-500 focus:outline-hidden"
                  placeholder="name@gmail.com"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Họ và tên hiển thị
                </label>
                <input
                  type="text"
                  value={devFullName}
                  onChange={(e) => setDevFullName(e.target.value)}
                  className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-amber-500 focus:outline-hidden"
                  placeholder="Nguyễn Văn A"
                  required
                />
              </div>

              {mode === 'signup' && (
                <div className="text-xs text-slate-500 bg-amber-50 border border-amber-200 p-2 rounded-lg">
                  Vai trò đăng ký: <strong className="uppercase text-amber-700">{accountType}</strong>
                </div>
              )}

              <div className="flex gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowDevModal(false)}
                  className="flex-1 rounded-lg border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-100"
                >
                  Hủy bỏ
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="flex-1 rounded-lg bg-blue-600 px-3 py-2 text-xs font-semibold text-white hover:bg-blue-700 disabled:opacity-50"
                >
                  {loading ? 'Đang gửi...' : 'Xác nhận Đăng nhập'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

function hashCode(str: string): number {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = (hash << 5) - hash + str.charCodeAt(i);
    hash |= 0;
  }
  return hash;
}
