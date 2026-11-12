import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import {
  Check,
  CheckCircle2,
  Clock,
  Copy,
  ExternalLink,
  Lock,
  QrCode,
  RefreshCw,
  ShieldCheck,
  X,
} from 'lucide-react';
import { commerceApi } from '../api/commerceApi';
import { formatVnd } from '@/shared/types/api';
import { Button } from '@/shared/ui/primitives';

interface VietQrPaymentModalProps {
  isOpen: boolean;
  onClose: () => void;
  order: {
    id: string;
    orderCode: number;
    paymentRef: string;
    totalAmountVnd: number;
    expiresAt?: string;
    status?: string;
    qrCodeUrl?: string;
    checkoutUrl?: string;
    bankBin?: string;
    bankName?: string;
    accountNumber?: string;
    accountName?: string;
  } | null;
  onPaymentSuccess?: () => void;
}

const DEFAULT_BANK_INFO = {
  bankId: '970422',
  bankName: 'Ngân hàng Quân Đội (MBBank)',
  accountNumber: '0348756972',
  accountHolder: 'BEE ACADEMY',
};

export const VietQrPaymentModal: React.FC<VietQrPaymentModalProps> = ({
  isOpen,
  onClose,
  order,
  onPaymentSuccess,
}) => {
  const navigate = useNavigate();
  const [copiedField, setCopiedField] = useState<string | null>(null);
  const [checking, setChecking] = useState(false);
  const [isPaid, setIsPaid] = useState(false);
  const [timeLeftSec, setTimeLeftSec] = useState<number>(15 * 60);

  // Initialize countdown timer
  useEffect(() => {
    if (!isOpen || !order) return;
    setIsPaid(order.status === 'PAID');

    if (order.expiresAt) {
      const expires = new Date(order.expiresAt).getTime();
      const diffSec = Math.max(0, Math.floor((expires - Date.now()) / 1000));
      setTimeLeftSec(diffSec > 0 ? diffSec : 15 * 60);
    } else {
      setTimeLeftSec(15 * 60);
    }

    const timer = setInterval(() => {
      setTimeLeftSec((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);

    return () => clearInterval(timer);
  }, [isOpen, order]);

  // Periodic active verification polling against PayOS via Backend every 5 seconds
  useEffect(() => {
    if (!isOpen || !order || isPaid) return;

    const interval = setInterval(async () => {
      try {
        const verified = await commerceApi.verifyOrderPayment(order.id);
        if (verified && verified.status === 'PAID') {
          setIsPaid(true);
          toast.success('Xác nhận thanh toán thành công qua PayOS VietQR!');
          if (onPaymentSuccess) onPaymentSuccess();
        }
      } catch {
        // Silently ignore background poll errors
      }
    }, 5000);

    return () => clearInterval(interval);
  }, [isOpen, order, isPaid, onPaymentSuccess]);

  if (!isOpen || !order) return null;

  const bankBin = order.bankBin || DEFAULT_BANK_INFO.bankId;
  const bankName = order.bankName || DEFAULT_BANK_INFO.bankName;
  const accountNumber = order.accountNumber || DEFAULT_BANK_INFO.accountNumber;
  const accountHolder = order.accountName || DEFAULT_BANK_INFO.accountHolder;

  const handleCopy = (text: string, fieldName: string) => {
    navigator.clipboard.writeText(text);
    setCopiedField(fieldName);
    toast.success(`Đã sao chép ${fieldName}!`);
    setTimeout(() => {
      setCopiedField(null);
    }, 2000);
  };

  const handleManualCheck = async () => {
    setChecking(true);
    try {
      const verified = await commerceApi.verifyOrderPayment(order.id);
      if (verified && verified.status === 'PAID') {
        setIsPaid(true);
        toast.success('Chúc mừng! Đơn hàng đã được thanh toán thành công.');
        if (onPaymentSuccess) onPaymentSuccess();
      } else {
        toast(
          'Hệ thống đang chờ tín hiệu từ ngân hàng/PayOS. Vui lòng kiểm tra lại sau ít giây hoặc bảo đảm bạn đã quét đúng mã QR.',
          { icon: '⏳' }
        );
      }
    } catch {
      toast.error('Không thể kiểm tra trạng thái thanh toán lúc này.');
    } finally {
      setChecking(false);
    }
  };

  const formatCountdown = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  // Ưu tiên ảnh QR động EMVCo khóa cứng số tiền trả về từ PayOS SDK ở Backend
  const qrImageUrl =
    order.qrCodeUrl ||
    `https://img.vietqr.io/image/${bankBin}-${accountNumber}-compact2.png?amount=${order.totalAmountVnd}&addInfo=${encodeURIComponent(order.paymentRef)}&accountName=${encodeURIComponent(accountHolder)}`;

  return (
    <div
      role="dialog"
      aria-modal="true"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-in fade-in duration-200"
    >
      <div className="relative w-full max-w-2xl overflow-hidden rounded-2xl bg-white shadow-2xl border border-sand">
        {/* Modal Header */}
        <div className="flex items-center justify-between border-b border-sand-soft bg-cream px-6 py-4">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-brand-700 to-brand-500 text-white shadow-xs">
              <QrCode className="h-5 w-5" />
            </div>
            <div>
              <h3 className="text-lg font-bold text-ink">Thanh toán VietQR 24/7 (PayOS)</h3>
              <p className="text-xs text-ink-muted">
                Đơn hàng #{order.orderCode} • Mã QR khóa cứng số tiền & tự động mở khóa
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="flex h-8 w-8 items-center justify-center rounded-lg text-ink-muted hover:bg-sand-soft hover:text-ink transition cursor-pointer"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Modal Content */}
        <div className="p-6 max-h-[80vh] overflow-y-auto">
          {isPaid ? (
            <div className="py-8 text-center space-y-4">
              <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-emerald-100 text-emerald-600 animate-bounce">
                <CheckCircle2 className="h-10 w-10" />
              </div>
              <h4 className="text-2xl font-bold text-ink">Thanh toán Thành công!</h4>
              <p className="max-w-md mx-auto text-sm text-ink-muted">
                Hệ thống BeeAcademy đã ghi nhận thanh toán của bạn cho đơn hàng #{order.orderCode}. Khóa học đã được mở khóa tự động vào tài khoản học sinh.
              </p>
              <div className="pt-4 flex justify-center gap-3">
                <Button
                  variant="primary"
                  size="lg"
                  onClick={() => {
                    onClose();
                    navigate('/learning/my-courses');
                  }}
                >
                  Vào Góc học tập ngay
                </Button>
              </div>
            </div>
          ) : (
            <div className="space-y-6">
              {/* Countdown Alert */}
              <div className="flex items-center justify-between rounded-xl bg-amber-50/80 border border-amber-200 px-4 py-2.5 text-xs text-amber-900">
                <div className="flex items-center gap-2 font-medium">
                  <Clock className="h-4 w-4 text-amber-600 animate-pulse" />
                  <span>Thời gian giữ mã thanh toán:</span>
                </div>
                <span className="font-mono font-bold text-sm text-brand-700">
                  {formatCountdown(timeLeftSec)}
                </span>
              </div>

              <div className="grid gap-6 md:grid-cols-2 items-center">
                {/* QR Code Column */}
                <div className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-sand bg-cream p-5 text-center">
                  <div className="relative overflow-hidden rounded-xl bg-white p-2 shadow-sm border border-sand">
                    <img
                      src={qrImageUrl}
                      alt="PayOS Dynamic VietQR Code"
                      className="h-56 w-56 object-contain"
                    />
                  </div>
                  <div className="mt-3 flex items-center gap-1.5 text-xs font-semibold text-brand-700">
                    <ShieldCheck className="h-4 w-4" />
                    <span>Mã QR động PayOS (Khóa cứng số tiền)</span>
                  </div>
                  <p className="mt-1 text-[11px] text-ink-muted max-w-[220px]">
                    Quét bằng App Ngân hàng bất kỳ — Số tiền và nội dung được cố định tự động, không thể sửa đổi
                  </p>
                  {order.checkoutUrl && (
                    <a
                      href={order.checkoutUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="mt-2.5 inline-flex items-center gap-1 text-xs font-semibold text-brand-700 hover:underline"
                    >
                      <span>Mở trang cổng thanh toán PayOS</span>
                      <ExternalLink className="h-3 w-3" />
                    </a>
                  )}
                </div>

                {/* Transfer Info Column */}
                <div className="space-y-3.5 text-sm">
                  <div className="rounded-xl border border-sand bg-white p-3.5 space-y-2.5">
                    {/* Bank Name */}
                    <div>
                      <span className="text-xs text-ink-muted">Ngân hàng thụ hưởng</span>
                      <p className="font-semibold text-ink">{bankName}</p>
                    </div>

                    {/* Account Number */}
                    <div className="flex items-center justify-between border-t border-sand-soft pt-2">
                      <div>
                        <span className="text-xs text-ink-muted">Số tài khoản nhận (VA PayOS)</span>
                        <p className="font-mono text-base font-bold text-ink">
                          {accountNumber}
                        </p>
                      </div>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleCopy(accountNumber, 'Số tài khoản')}
                      >
                        {copiedField === 'Số tài khoản' ? (
                          <Check className="h-3.5 w-3.5 text-emerald-600" />
                        ) : (
                          <Copy className="h-3.5 w-3.5" />
                        )}
                        {copiedField === 'Số tài khoản' ? 'Đã chép' : 'Chép'}
                      </Button>
                    </div>

                    {/* Account Holder */}
                    <div className="border-t border-sand-soft pt-2">
                      <span className="text-xs text-ink-muted">Tên chủ tài khoản</span>
                      <p className="font-bold text-ink uppercase tracking-wider">
                        {accountHolder}
                      </p>
                    </div>

                    {/* Fixed Amount (Read-Only Locked) */}
                    <div className="flex items-center justify-between border-t border-sand-soft pt-2">
                      <div>
                        <span className="text-xs text-ink-muted">Số tiền cố định (Không chỉnh sửa)</span>
                        <p className="text-lg font-black text-brand-700">
                          {formatVnd(order.totalAmountVnd)}
                        </p>
                      </div>
                      <span className="inline-flex items-center gap-1 rounded-md bg-slate-100 px-2 py-1 text-[11px] font-semibold text-slate-600">
                        <Lock className="h-3 w-3" />
                        Cố định
                      </span>
                    </div>

                    {/* Transfer Content */}
                    <div className="flex items-center justify-between border-t border-sand-soft pt-2 bg-amber-50/70 -mx-3.5 -mb-3.5 p-3 rounded-b-xl border-t-amber-100">
                      <div>
                        <span className="text-xs font-semibold text-amber-900">
                          Nội dung định danh tự động
                        </span>
                        <p className="font-mono text-sm font-black text-brand-700">
                          {order.paymentRef}
                        </p>
                      </div>
                      <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => handleCopy(order.paymentRef, 'Nội dung chuyển khoản')}
                      >
                        {copiedField === 'Nội dung chuyển khoản' ? (
                          <Check className="h-3.5 w-3.5 text-emerald-600" />
                        ) : (
                          <Copy className="h-3.5 w-3.5" />
                        )}
                        {copiedField === 'Nội dung chuyển khoản' ? 'Đã chép' : 'Chép'}
                      </Button>
                    </div>
                  </div>

                  <p className="text-xs text-ink-muted leading-relaxed">
                    🔒 <strong>Bảo vệ giao dịch:</strong> Vui lòng quét trực tiếp mã QR để số tiền và nội dung được khóa cứng chính xác. Hệ thống tự động đối soát và mở khóa học trong 5 giây.
                  </p>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="flex flex-col sm:flex-row items-center justify-between gap-3 border-t border-sand-soft pt-4">
                <Button
                  variant="outline"
                  size="md"
                  onClick={onClose}
                  className="w-full sm:w-auto"
                >
                  Đóng lại & thanh toán sau
                </Button>

                <Button
                  variant="primary"
                  size="md"
                  loading={checking}
                  onClick={handleManualCheck}
                  className="w-full sm:w-auto shadow-md"
                >
                  <RefreshCw className={`h-4 w-4 ${checking ? 'animate-spin' : ''}`} />
                  Tôi đã chuyển khoản thành công
                </Button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
