import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { Landmark, Shield, TrendingUp, Wallet } from 'lucide-react';
import {
  commerceApi,
  PayoutPeriodDto,
  TeacherBankAccountDto,
  TeacherRevenueStatsDto,
} from '../api/commerceApi';
import { formatVnd, getApiErrorMessage } from '@/shared/types/api';
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Input,
  Spinner,
} from '@/shared/ui/primitives';

export const TeacherRevenuePage: React.FC = () => {
  const [stats, setStats] = useState<TeacherRevenueStatsDto | null>(null);
  const [bank, setBank] = useState<TeacherBankAccountDto | null>(null);
  const [payouts, setPayouts] = useState<PayoutPeriodDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [bankName, setBankName] = useState('Vietcombank');
  const [accountNumber, setAccountNumber] = useState('');
  const [accountHolder, setAccountHolder] = useState('');
  const [branch, setBranch] = useState('Chi nhánh TP.HCM');
  const [savingBank, setSavingBank] = useState(false);

  const fetchFinanceData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [statsRes, bankRes, payoutsRes] = await Promise.all([
        commerceApi.getTeacherRevenueStatistics(),
        commerceApi.getTeacherBankAccount().catch(() => null),
        commerceApi.getTeacherPayouts().catch(() => []),
      ]);
      setStats(statsRes);
      setBank(bankRes);
      setPayouts(payoutsRes);
      if (bankRes) {
        setBankName(bankRes.bankName);
        setAccountHolder(bankRes.accountHolder);
        setBranch(bankRes.branch || '');
      }
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải dữ liệu tài chính giáo viên.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFinanceData();
  }, []);

  const handleSaveBank = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!accountNumber.trim() || !accountHolder.trim()) return;
    setSavingBank(true);
    try {
      const updated = await commerceApi.saveTeacherBankAccount({
        bankName: bankName.trim(),
        accountNumber: accountNumber.trim(),
        accountHolder: accountHolder.trim().toUpperCase(),
        branch: branch.trim() || undefined,
      });
      setBank(updated);
      setAccountNumber('');
      toast.success(
        'Đã lưu tài khoản ngân hàng (Mã hóa AES-256-GCM) và gửi Admin thẩm định!'
      );
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể cập nhật tài khoản ngân hàng.'));
    } finally {
      setSavingBank(false);
    }
  };

  if (loading) return <Spinner label="Đang tải thống kê doanh thu và quyết toán..." />;
  if (error) return <ErrorState message={error} onRetry={fetchFinanceData} />;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">
          Doanh thu Giáo viên & Tài khoản Ngân hàng (UC32, UC35)
        </h1>
        <p className="text-sm text-slate-600">
          Cơ chế chia sẻ doanh thu minh bạch: Giáo viên thực nhận 80% • Phí vận hành nền tảng 20%
        </p>
      </div>

      {stats && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <Card className="p-5">
            <p className="text-xs font-semibold text-slate-500">Tổng Doanh thu Gộp (100%)</p>
            <p className="mt-2 text-2xl font-extrabold text-slate-900">
              {formatVnd(stats.totalGrossVnd)}
            </p>
            <p className="mt-1 text-xs text-slate-400">
              {stats.totalTransactions} giao dịch thành công
            </p>
          </Card>

          <Card className="p-5 border-emerald-200 bg-emerald-50/40">
            <p className="text-xs font-semibold text-emerald-800">
              Doanh thu Thực nhận GV (80%)
            </p>
            <p className="mt-2 text-2xl font-extrabold text-emerald-700">
              {formatVnd(stats.totalTeacherVnd)}
            </p>
            <p className="mt-1 text-xs text-emerald-600">
              Phí nền tảng (20%): {formatVnd(stats.totalPlatformFeeVnd)}
            </p>
          </Card>

          <Card className="p-5">
            <p className="text-xs font-semibold text-amber-700">Chờ quyết toán kỳ tới</p>
            <p className="mt-2 text-2xl font-extrabold text-amber-600">
              {formatVnd(stats.pendingPayoutVnd)}
            </p>
            <p className="mt-1 text-xs text-slate-400">Kỳ hiện tại: {stats.currentPeriod}</p>
          </Card>

          <Card className="p-5">
            <p className="text-xs font-semibold text-slate-500">Đã giải ngân về Ngân hàng</p>
            <p className="mt-2 text-2xl font-extrabold text-slate-900">
              {formatVnd(stats.paidPayoutVnd)}
            </p>
            <p className="mt-1 text-xs text-slate-400">Đối soát tự động hàng tháng</p>
          </Card>
        </div>
      )}

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="p-5">
          <div className="mb-4 flex items-center justify-between border-b border-slate-100 pb-3">
            <div className="flex items-center gap-2">
              <Landmark className="h-5 w-5 text-amber-600" />
              <h2 className="text-base font-bold text-slate-900">Tài khoản Nhận quyết toán</h2>
            </div>
            {bank && (
              <Badge
                tone={
                  bank.verifyStatus === 'VERIFIED'
                    ? 'success'
                    : bank.verifyStatus === 'REJECTED'
                      ? 'danger'
                      : 'warning'
                }
              >
                {bank.verifyStatus}
              </Badge>
            )}
          </div>

          {bank && (
            <div className="mb-4 rounded-xl bg-slate-50 p-3.5 text-xs space-y-1">
              <p className="font-bold text-slate-900">{bank.bankName}</p>
              <p className="font-mono text-sm font-semibold text-amber-700">
                STK (AES-256-GCM Masked): {bank.accountNumberMasked}
              </p>
              <p className="text-slate-600">Chủ TK: {bank.accountHolder}</p>
            </div>
          )}

          <form onSubmit={handleSaveBank} className="space-y-3">
            <Input
              label="Tên Ngân hàng"
              value={bankName}
              onChange={(e) => setBankName(e.target.value)}
              required
            />
            <Input
              label="Số tài khoản mới (Sẽ mã hóa AES-256-GCM)"
              placeholder="Nhập số tài khoản ngân hàng..."
              value={accountNumber}
              onChange={(e) => setAccountNumber(e.target.value)}
              required
            />
            <Input
              label="Họ tên Chủ tài khoản (Viết hoa không dấu)"
              placeholder="NGUYEN VAN A"
              value={accountHolder}
              onChange={(e) => setAccountHolder(e.target.value)}
              required
            />
            <Input
              label="Chi nhánh"
              value={branch}
              onChange={(e) => setBranch(e.target.value)}
            />
            <Button type="submit" loading={savingBank} className="w-full">
              <Shield className="h-4 w-4" />
              Cập nhật & Gửi thẩm định
            </Button>
          </form>
        </Card>

        <div className="space-y-4 lg:col-span-2">
          <div className="flex items-center gap-2">
            <Wallet className="h-5 w-5 text-amber-600" />
            <h2 className="text-lg font-bold text-slate-900">
              Lịch sử Kỳ Quyết toán Hàng tháng ({payouts.length})
            </h2>
          </div>

          {payouts.length === 0 ? (
            <EmptyState
              title="Chưa có kỳ quyết toán nào"
              description="Các giao dịch bán khóa học sẽ được tổng hợp vào kỳ đối soát cuối mỗi tháng."
            />
          ) : (
            <div className="space-y-3">
              {payouts.map((p) => (
                <Card key={p.id} className="p-4">
                  <div className="flex flex-col justify-between gap-2 sm:flex-row sm:items-center">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold text-slate-900">
                          Kỳ tháng {p.monthYear}
                        </span>
                        <Badge tone={p.status === 'PAID' ? 'success' : 'warning'}>
                          {p.status}
                        </Badge>
                      </div>
                      <p className="mt-1 text-xs text-slate-500">
                        Số giao dịch: {p.transactionCount} • Tổng thu: {formatVnd(p.totalGrossVnd)}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="text-xs text-slate-500">Thực nhận (80%):</p>
                      <p className="text-base font-extrabold text-emerald-700">
                        {formatVnd(p.totalTeacherVnd)}
                      </p>
                    </div>
                  </div>
                  {p.transferRef && (
                    <div className="mt-2 border-t border-slate-100 pt-2 text-xs text-slate-600">
                      Mã Ủy nhiệm chi (UNC): <strong>{p.transferRef}</strong> — {p.transferContent}
                    </div>
                  )}
                </Card>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
