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
  text?: string;
  className?: string;
  style?: React.CSSProperties;
}

export const GoogleSignInButton: React.FC<GoogleSignInButtonProps> = ({
  mode = 'signin',
  accountType = 'student',
  text,
  className = '',
  style,
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
    Boolean(clientId) &&
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
      } else {
        existingScript.addEventListener('load', initGoogleSignIn);
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
        text: 'continue_with',
        shape: 'rectangular',
        logo_alignment: 'left',
        width: 400,
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
    } else if (!isRealClientIdConfigured) {
      setShowDevModal(true);
    }
  };

  return (
    <div className={`w-full ${className}`} style={style}>
      <div className="relative w-full">
        {/* Custom Visual Button (.auth-gbtn) — 100% full width & aligned with form inputs */}
        <button
          type="button"
          onClick={handleButtonClick}
          disabled={loading}
          className="auth-gbtn flex w-full items-center justify-center gap-2.5 px-4 text-[15px] font-semibold cursor-pointer disabled:opacity-60"
        >
          <svg width="18" height="18" viewBox="0 0 48 48" aria-hidden="true">
            <path
              fill="#EA4335"
              d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"
            />
            <path
              fill="#4285F4"
              d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"
            />
            <path
              fill="#FBBC05"
              d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"
            />
            <path
              fill="#34A853"
              d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"
            />
          </svg>
          <span>
            {loading ? 'Đang xác thực với Google...' : text || 'Tiếp tục với Google'}
          </span>
        </button>

        {/* Transparent Interactive Google GIS Overlay for real OAuth popup */}
        {isRealClientIdConfigured && !loading && (
          <div
            aria-hidden="true"
            className="absolute inset-0 z-10 flex items-center justify-center overflow-hidden rounded-[14px] opacity-[0.01] cursor-pointer"
          >
            <div ref={googleButtonRef} className="w-full scale-x-115 flex justify-center" />
          </div>
        )}
      </div>

      {/* Dev Mode Modal - Cho phép lập trình viên kiểm thử khi chưa cấu hình Client ID */}
      {showDevModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 backdrop-blur-xs">
          <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl border border-sand">
            <div className="flex items-center gap-2.5 mb-3">
              <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blush text-brand-700">
                <svg className="h-5 w-5" viewBox="0 0 48 48">
                  <path
                    fill="#EA4335"
                    d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"
                  />
                  <path
                    fill="#4285F4"
                    d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"
                  />
                  <path
                    fill="#FBBC05"
                    d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"
                  />
                  <path
                    fill="#34A853"
                    d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"
                  />
                </svg>
              </div>
              <div>
                <h3 className="font-bold text-ink">Mô phỏng Đăng nhập Google (Dev Mode)</h3>
                <p className="text-xs text-ink-muted">Kiểm thử toàn diện luồng xác thực Google</p>
              </div>
            </div>

            <p className="text-xs text-ink-muted mb-4 bg-cream p-3 rounded-xl border border-sand">
              Chưa cấu hình Google Client ID thật trong{' '}
              <code className="text-brand-700 font-semibold">.env</code>. Hệ thống sẽ sinh Google ID
              Token hợp lệ để backend giải mã và liên kết qua{' '}
              <code className="text-brand-700 font-semibold">ExternalLogin</code>.
            </p>

            <form onSubmit={handleDevMockLogin} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-ink mb-1">
                  Email tài khoản Google
                </label>
                <input
                  type="email"
                  value={devEmail}
                  onChange={(e) => setDevEmail(e.target.value)}
                  className="auth-input"
                  placeholder="name@gmail.com"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-ink mb-1">
                  Họ và tên hiển thị
                </label>
                <input
                  type="text"
                  value={devFullName}
                  onChange={(e) => setDevFullName(e.target.value)}
                  className="auth-input"
                  placeholder="Nguyễn Văn A"
                  required
                />
              </div>

              {mode === 'signup' && (
                <div className="text-xs text-ink-muted bg-blush border border-sand p-2.5 rounded-xl">
                  Vai trò đăng ký:{' '}
                  <strong className="uppercase text-brand-700">{accountType}</strong>
                </div>
              )}

              <div className="flex gap-2.5 pt-2">
                <button
                  type="button"
                  onClick={() => setShowDevModal(false)}
                  className="flex-1 rounded-xl border border-sand px-3 py-2.5 text-xs font-semibold text-ink hover:bg-blush cursor-pointer"
                >
                  Hủy bỏ
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="auth-cta flex-1 rounded-xl px-3 py-2.5 text-xs font-semibold text-white cursor-pointer disabled:opacity-60"
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
