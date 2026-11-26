import React from 'react';
import { AlertTriangle, RefreshCw, Home } from 'lucide-react';

interface ErrorBoundaryState {
  hasError: boolean;
  errorMessage: string;
}

export class GlobalErrorBoundary extends React.Component<
  { children: React.ReactNode },
  ErrorBoundaryState
> {
  constructor(props: { children: React.ReactNode }) {
    super(props);
    this.state = { hasError: false, errorMessage: '' };
  }

  static getDerivedStateFromError(error: unknown): ErrorBoundaryState {
    const message =
      error instanceof Error ? error.message : 'Đã xảy ra sự cố hiển thị không mong muốn.';
    return { hasError: true, errorMessage: message };
  }

  componentDidCatch(error: unknown, errorInfo: React.ErrorInfo) {
    console.error('[BeeAcademy ErrorBoundary]', error, errorInfo);
  }

  private handleReload = () => {
    window.location.reload();
  };

  private handleGoHome = () => {
    window.location.href = '/courses';
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center bg-[#fffaf5] p-6 font-sans">
          <div className="w-full max-w-md rounded-2xl border border-[#e7ddd5] bg-white p-7 text-center shadow-xl">
            <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-amber-100 text-[#b02e00]">
              <AlertTriangle className="h-7 w-7" />
            </div>
            <h1 className="text-lg font-extrabold text-[#1c1917]">
               Trang đang được cập nhật hoặc gián đoạn kết nối
            </h1>
            <p className="mt-2 text-xs leading-relaxed text-[#57534e]">
              Hệ thống gặp sự cố khi hiển thị dữ liệu. Vui lòng tải lại trang để tiếp tục sử dụng BeeAcademy.
            </p>
            {this.state.errorMessage && (
              <p className="mt-3 rounded-lg bg-slate-50 border border-slate-200 px-3 py-2 text-[11px] font-mono text-slate-600 break-words">
                {this.state.errorMessage}
              </p>
            )}
            <div className="mt-6 flex items-center justify-center gap-3">
              <button
                type="button"
                onClick={this.handleGoHome}
                className="inline-flex items-center gap-1.5 rounded-xl border border-[#e7ddd5] bg-white px-4 py-2.5 text-xs font-semibold text-[#1c1917] hover:bg-[#f3ece6] transition cursor-pointer"
              >
                <Home className="h-4 w-4" />
                Về Trang chủ
              </button>
              <button
                type="button"
                onClick={this.handleReload}
                className="inline-flex items-center gap-1.5 rounded-xl bg-[#b02e00] px-4 py-2.5 text-xs font-semibold text-white hover:bg-[#d4501a] transition shadow-sm cursor-pointer"
              >
                <RefreshCw className="h-4 w-4" />
                Tải lại trang
              </button>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
