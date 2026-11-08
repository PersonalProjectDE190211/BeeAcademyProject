import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { CheckCircle2, KeyRound, ShieldCheck, UserX } from 'lucide-react';
import { familyApi, ParentLinkDto } from '../api/familyApi';
import { getApiErrorMessage } from '@/shared/types/api';
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Input,
  Spinner,
} from '@/shared/ui/primitives';

export const StudentFamilyPage: React.FC = () => {
  const [links, setLinks] = useState<ParentLinkDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [inviteToken, setInviteToken] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const fetchStudentLinks = async () => {
    setLoading(true);
    setError(null);
    try {
      const list = await familyApi.getStudentLinks();
      setLinks(list);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải danh sách liên kết gia đình.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStudentLinks();
  }, []);

  const handleAcceptInvite = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inviteToken.trim()) return;
    setSubmitting(true);
    try {
      await familyApi.acceptInvite(inviteToken.trim().toUpperCase());
      setInviteToken('');
      toast.success('Đã chấp thuận liên kết và cấp quyền theo dõi tiến độ học tập cho Phụ huynh!');
      fetchStudentLinks();
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Mã mời không hợp lệ hoặc đã hết hạn.'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleRejectInvite = async () => {
    if (!inviteToken.trim()) return;
    setSubmitting(true);
    try {
      await familyApi.rejectInvite(
        inviteToken.trim().toUpperCase(),
        'Học sinh từ chối lời mời liên kết'
      );
      setInviteToken('');
      toast.success('Đã từ chối lời mời liên kết.');
      fetchStudentLinks();
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể từ chối mã mời này.'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleUnlink = async (linkId: string) => {
    try {
      await familyApi.studentUnlink(linkId, 'Học sinh chủ động thu hồi quyền liên kết');
      toast.success('Đã thu hồi quyền theo dõi và hủy liên kết với Phụ huynh.');
      fetchStudentLinks();
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể hủy liên kết.'));
    }
  };

  if (loading) return <Spinner label="Đang tải thông tin Liên kết Gia đình..." />;
  if (error) return <ErrorState message={error} onRetry={fetchStudentLinks} />;

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">
          Liên kết Tài khoản Gia đình & Quyền riêng tư (UC24, UC29)
        </h1>
        <p className="text-sm text-slate-600">
          Xác nhận mã mời 6 ký tự từ Phụ huynh để đồng thuận chia sẻ tiến độ học tập
        </p>
      </div>

      <Card className="p-6">
        <div className="mb-3 flex items-center gap-2">
          <KeyRound className="h-5 w-5 text-amber-600" />
          <h2 className="text-base font-bold text-slate-900">
            Nhập Mã mời 6 Ký tự từ Phụ huynh
          </h2>
        </div>

        <form onSubmit={handleAcceptInvite} className="space-y-3">
          <Input
            placeholder="Nhập mã 6 ký tự (VD: AB12CD)..."
            maxLength={10}
            value={inviteToken}
            onChange={(e) => setInviteToken(e.target.value.toUpperCase())}
            required
          />
          <p className="text-xs text-slate-500">
            Bằng việc bấm <strong>Chấp thuận Liên kết</strong>, bạn đồng ý cho phép Phụ huynh xem báo cáo tiến độ hoàn thành khóa học của bạn. Bạn có thể thu hồi quyền này bất cứ lúc nào.
          </p>
          <div className="flex flex-wrap gap-2">
            <Button type="submit" loading={submitting}>
              <CheckCircle2 className="h-4 w-4" />
              Chấp thuận Liên kết (Consent)
            </Button>
            <Button
              type="button"
              variant="outline"
              loading={submitting}
              onClick={handleRejectInvite}
            >
              Từ chối Mã mời
            </Button>
          </div>
        </form>
      </Card>

      <div className="space-y-3">
        <h2 className="text-base font-bold text-slate-900">
          Danh sách Liên kết Phụ huynh ({links.length})
        </h2>

        {links.length === 0 ? (
          <EmptyState
            title="Chưa có tài khoản Phụ huynh nào đang liên kết"
            description="Khi Phụ huynh gửi lời mời liên kết, bạn có thể nhập mã xác thực 6 ký tự tại đây."
          />
        ) : (
          links.map((link) => (
            <Card key={link.id} className="p-4">
              <div className="flex items-center justify-between gap-3">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <ShieldCheck className="h-4 w-4 text-emerald-600" />
                    <span className="text-sm font-bold text-slate-900">
                      Mã Phụ huynh: {link.parentId.slice(0, 8)}...
                    </span>
                    <Badge tone={link.status === 'ACTIVE' ? 'success' : 'neutral'}>
                      {link.status}
                    </Badge>
                  </div>
                  <p className="text-xs text-slate-500">
                    Đồng thuận chia sẻ dữ liệu (Consent):{' '}
                    <strong>{link.consentGranted ? 'Đã cấp quyền' : 'Đã thu hồi'}</strong>
                  </p>
                </div>

                {link.status === 'ACTIVE' && (
                  <Button
                    size="sm"
                    variant="danger"
                    onClick={() => handleUnlink(link.id)}
                  >
                    <UserX className="h-4 w-4" />
                    Thu hồi Liên kết
                  </Button>
                )}
              </div>
            </Card>
          ))
        )}
      </div>
    </div>
  );
};
