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

const DEFAULT_GOOGLE_CLIENT_ID =
  '562390685704-gn1134f6gql8dcfianuspf859q3tmvmm.apps.googleusercontent.com';

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
  const [gsiReady, setGsiReady] = useState(false);

  const googleButtonRef = useRef<HTMLDivElement>(null);
  const rawClientId = (import.meta.env.VITE_GOOGLE_CLIENT_ID as string | undefined)?.trim();
  const clientId =
    rawClientId &&
    !rawClientId.includes('YOUR_GOOGLE_CLIENT_ID') &&
    !rawClientId.includes('your-google-client-id') &&
    rawClientId.endsWith('.apps.googleusercontent.com')
      ? rawClientId
      : DEFAULT_GOOGLE_CLIENT_ID;

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
    let mounted = true;

    const initGoogleSignIn = () => {
      if (!mounted || !window.google?.accounts?.id || !googleButtonRef.current) return;

      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: (response) => {
          if (response.credential) {
            processGoogleToken(response.credential);
          }
        },
      });

      googleButtonRef.current.innerHTML = '';
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
      setGsiReady(true);
    };

    if (window.google?.accounts?.id) {
      initGoogleSignIn();
      return () => {
        mounted = false;
      };
    }

    const existingScript = document.getElementById('google-gsi-client') as HTMLScriptElement | null;
    if (!existingScript) {
      const script = document.createElement('script');
      script.id = 'google-gsi-client';
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;
      script.onload = initGoogleSignIn;
      document.body.appendChild(script);
    } else {
      existingScript.addEventListener('load', initGoogleSignIn);
    }

    return () => {
      mounted = false;
      existingScript?.removeEventListener('load', initGoogleSignIn);
    };
  }, [clientId, mode, accountType]);

  const handleButtonClick = () => {
    if (window.google?.accounts?.id) {
      window.google.accounts.id.prompt();
    } else {
      toast.error('Đang tải dịch vụ Đăng nhập Google. Vui lòng thử lại sau giây lát.');
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
        {!loading && (
          <div
            aria-hidden="true"
            className={`absolute inset-0 z-10 flex items-center justify-center overflow-hidden rounded-[14px] opacity-[0.01] cursor-pointer ${
              gsiReady ? 'pointer-events-auto' : 'pointer-events-none'
            }`}
          >
            <div ref={googleButtonRef} className="w-full scale-x-115 flex justify-center" />
          </div>
        )}
      </div>
    </div>
  );
};
