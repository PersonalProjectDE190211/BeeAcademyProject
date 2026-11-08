import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { CheckCircle2, Landmark, Receipt, XCircle } from 'lucide-react';
import {
  commerceApi,
  PayoutPeriodDto,
  TeacherBankAccountDto,
} from '../api/commerceApi';
import { formatVnd, getApiErrorMessage } from '@/shared/types/api';
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Input,
  Modal,
  Spinner,
} from '@/shared/ui/primitives';

export const AdminPayoutsPage: React.FC = () => {
  const [payouts, setPayouts] = useState<PayoutPeriodDto[]>([]);
  const [pendingBanks, setPendingBanks] = useState<TeacherBankAccountDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [selectedPayout, setSelectedPayout] = useState<PayoutPeriodDto | null>(null);
  const [transferRef, setTransferRef] = useState('');
  const [transferContent, setTransferContent] = useState('');
  const [uncUrl, setUncUrl] = useState('');
  const [confirmingPayout, setConfirmingPayout] = useState(false);

  const fetchAll = async () => {
    setLoading(true);
    setError(null);
    try {
      const [payoutList, bankList] = await Promise.all([
        commerceApi.getAdminPayouts(),
        commerceApi.getPendingBankAccounts(),
      ]);
      setPayouts(payoutList);
      setPendingBanks(bankList);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải dữ liệu đối soát tài chính Admin.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAll();
  }, []);

  const handleVerifyBank = async (id: string) => {
    try {
      await commerceApi.verifyBankAccount(id);
      toast.success('Đã xác thực tài khoản ngân hàng của giáo viên!');
      fetchAll();
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể xác thực tài khoản ngân hàng.'));
    }
  };

  const handleRejectBank = async (id: string) => {
    try {
      await commerceApi.rejectBankAccount(id, 'Thông tin chủ tài khoản chưa trùng khớp');
      toast.success('Đã từ chối tài khoản ngân hàng.');
      fetchAll();
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể từ chối tài khoản ngân hàng.'));
    }
  };

  const handleConfirmPayout = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedPayout || !transferRef.trim()) return;
    setConfirmingPayout(true);
    try {
      await commerceApi.confirmPayout(selectedPayout.id, {
        transferRef: transferRef.trim(),
        transferContent:
          transferContent.trim() || `Quyet toan doanh thu ky ${selectedPayout.monthYear}`,
        uncAttachmentUrl: uncUrl.trim() || undefined,
      });
      toast.success('Đã xác nhận Ủy nhiệm chi (UNC) quyết toán thành công!');
      setSelectedPayout(null);
      setTransferRef('');
      setTransferContent('');
      setUncUrl('');
      fetchAll();
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể xác nhận quyết toán.'));
    } finally {
      setConfirmingPayout(false);
    }
  };

  if (loading) return <Spinner label="Đang tải trung tâm đối soát tài chính Admin..." />;
  if (error) return <ErrorState message={error} onRetry={fetchAll} />;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">
          Quản trị Đối soát Tài chính & Ủy nhiệm chi (UC42, UC43)
        </h1>
        <p className="text-sm text-slate-600">
          Thẩm định tài khoản ngân hàng Giáo viên và xác nhận giải ngân doanh thu 80% định kỳ
        </p>
      </div>

      <Card className="p-5">
        <div className="mb-4 flex items-center gap-2 border-b border-slate-100 pb-3">
          <Landmark className="h-5 w-5 text-amber-600" />
          <h2 className="text-base font-bold text-slate-900">
            Tài khoản Ngân hàng Giáo viên Chờ thẩm định ({pendingBanks.length})
          </h2>
        </div>

        {pendingBanks.length === 0 ? (
          <p className="text-xs text-slate-500">
            Không có tài khoản ngân hàng nào đang chờ thẩm định.
          </p>
        ) : (
          <div className="space-y-3">
            {pendingBanks.map((b) => (
              <div
                key={b.id}
                className="flex flex-col justify-between gap-3 rounded-xl border border-slate-200 p-4 sm:flex-row sm:items-center"
              >
                <div className="text-xs space-y-1">
                  <p className="text-sm font-bold text-slate-900">
                    {b.bankName} — {b.accountNumberMasked}
                  </p>
                  <p className="text-slate-600">
                    Chủ TK: <strong>{b.accountHolder}</strong> • Chi nhánh: {b.branch || 'N/A'}
                  </p>
                </div>
                <div className="flex gap-2">
                  <Button size="sm" onClick={() => handleVerifyBank(b.id)}>
                    <CheckCircle2 className="h-4 w-4" />
                    Xác thực hợp lệ
                  </Button>
                  <Button size="sm" variant="danger" onClick={() => handleRejectBank(b.id)}>
                    <XCircle className="h-4 w-4" />
                    Từ chối
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>

      <div className="space-y-4">
        <h2 className="text-lg font-bold text-slate-900">
          Danh sách Kỳ Quyết toán Giáo viên ({payouts.length})
        </h2>

        {payouts.length === 0 ? (
          <EmptyState title="Chưa có kỳ quyết toán nào phát sinh" />
        ) : (
          <div className="space-y-3">
            {payouts.map((p) => (
              <Card key={p.id} className="p-5">
                <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="text-base font-bold text-slate-900">
                        Kỳ đối soát: {p.monthYear}
                      </span>
                      <Badge tone={p.status === 'PAID' ? 'success' : 'warning'}>
                        {p.status}
                      </Badge>
                    </div>
                    <p className="text-xs text-slate-500">
                      Tổng thu gộp: {formatVnd(p.totalGrossVnd)} • Phí sàn (20%):{' '}
                      {formatVnd(p.totalPlatformFeeVnd)} • Thực chi GV (80%):{' '}
                      <strong className="text-emerald-700">{formatVnd(p.totalTeacherVnd)}</strong>
                    </p>
                  </div>

                  {p.status !== 'PAID' && (
                    <Button
                      size="sm"
                      onClick={() => {
                        setSelectedPayout(p);
                        setTransferContent(`BeeAcademy quyet toan ky ${p.monthYear}`);
                      }}
                    >
                      <Receipt className="h-4 w-4" />
                      Xác nhận Chi trả (UNC)
                    </Button>
                  )}
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>

      <Modal
        open={Boolean(selectedPayout)}
        onClose={() => setSelectedPayout(null)}
        title={`Xác nhận Ủy nhiệm chi (UNC) — Kỳ ${selectedPayout?.monthYear || ''}`}
      >
        <form onSubmit={handleConfirmPayout} className="space-y-3">
          <Input
            label="Mã giao dịch chuyển khoản ngân hàng (Transfer Ref)"
            placeholder="FT26310998877"
            value={transferRef}
            onChange={(e) => setTransferRef(e.target.value)}
            required
          />
          <Input
            label="Nội dung chuyển khoản"
            value={transferContent}
            onChange={(e) => setTransferContent(e.target.value)}
            required
          />
          <Input
            label="URL Ảnh chụp Ủy nhiệm chi (UNC Attachment)"
            placeholder="https://..."
            value={uncUrl}
            onChange={(e) => setUncUrl(e.target.value)}
          />
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="ghost" onClick={() => setSelectedPayout(null)}>
              Hủy
            </Button>
            <Button type="submit" loading={confirmingPayout}>
              Hoàn tất Giải ngân
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
