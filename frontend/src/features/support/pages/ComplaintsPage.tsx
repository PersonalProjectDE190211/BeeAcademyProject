import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { CheckCircle2, LifeBuoy, Plus, Send, XCircle } from 'lucide-react';
import { ComplaintDto, supportApi } from '../api/supportApi';
import { useAuthStore } from '@/shared/store/useAuthStore';
import { getApiErrorMessage } from '@/shared/types/api';
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Input,
  Modal,
  Select,
  Spinner,
  Textarea,
} from '@/shared/ui/primitives';

export const ComplaintsPage: React.FC = () => {
  const { user } = useAuthStore();
  const isAdmin = user?.role === 'admin';

  const [complaints, setComplaints] = useState<ComplaintDto[]>([]);
  const [selected, setSelected] = useState<ComplaintDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState('PAYMENT');
  const [priority, setPriority] = useState('MEDIUM');
  const [content, setContent] = useState('');
  const [creating, setCreating] = useState(false);

  const [replyContent, setReplyContent] = useState('');
  const [sendingReply, setSendingReply] = useState(false);

  const [resolutionSummary, setResolutionSummary] = useState('');
  const [resolving, setResolving] = useState(false);

  const fetchComplaints = async () => {
    setLoading(true);
    setError(null);
    try {
      const list = isAdmin
        ? await supportApi.getAllComplaints()
        : await supportApi.getMyComplaints();
      setComplaints(list);
      if (list.length > 0 && !selected) {
        setSelected(list[0]);
      }
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải danh sách phiếu khiếu nại hỗ trợ.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchComplaints();
  }, [isAdmin]);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !content.trim()) return;
    setCreating(true);
    try {
      const created = await supportApi.createComplaint({
        title: title.trim(),
        category,
        priority,
        content: content.trim(),
      });
      setComplaints((prev) => [created, ...prev]);
      setSelected(created);
      setShowCreateModal(false);
      setTitle('');
      setContent('');
      toast.success('Đã gửi phiếu yêu cầu hỗ trợ thành công!');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể tạo phiếu khiếu nại.'));
    } finally {
      setCreating(false);
    }
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selected || !replyContent.trim()) return;
    setSendingReply(true);
    try {
      const updated = await supportApi.addMessage(selected.id, replyContent.trim());
      setSelected(updated);
      setComplaints((prev) => prev.map((c) => (c.id === updated.id ? updated : c)));
      setReplyContent('');
      toast.success('Đã gửi tin nhắn trao đổi!');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể gửi tin nhắn.'));
    } finally {
      setSendingReply(false);
    }
  };

  const handleResolve = async () => {
    if (!selected) return;
    setResolving(true);
    try {
      const updated = await supportApi.resolveComplaint(
        selected.id,
        resolutionSummary.trim() || 'Đã kiểm tra và xử lý hoàn tất yêu cầu của người dùng.'
      );
      setSelected(updated);
      setComplaints((prev) => prev.map((c) => (c.id === updated.id ? updated : c)));
      setResolutionSummary('');
      toast.success('Đã đánh dấu Giải quyết (RESOLVED) phiếu khiếu nại!');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể giải quyết phiếu khiếu nại.'));
    } finally {
      setResolving(false);
    }
  };

  const handleClose = async () => {
    if (!selected) return;
    try {
      const updated = await supportApi.closeComplaint(selected.id);
      setSelected(updated);
      setComplaints((prev) => prev.map((c) => (c.id === updated.id ? updated : c)));
      toast.success('Đã đóng phiếu hỗ trợ.');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể đóng phiếu hỗ trợ.'));
    }
  };

  if (loading) return <Spinner label="Đang tải trung tâm Hỗ trợ & Khiếu nại..." />;
  if (error) return <ErrorState message={error} onRetry={fetchComplaints} />;

  return (
    <div className="space-y-6">
      <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">
            Trung tâm Hỗ trợ & Giải quyết Khiếu nại (UC23, UC28, UC37, UC45)
          </h1>
          <p className="text-sm text-slate-600">
            Trao đổi 2 chiều trực tiếp với Ban Quản trị BeeAcademy về khóa học, thanh toán và kỹ thuật
          </p>
        </div>
        <Button onClick={() => setShowCreateModal(true)}>
          <Plus className="h-4 w-4" />
          Tạo phiếu hỗ trợ mới
        </Button>
      </div>

      {complaints.length === 0 ? (
        <EmptyState
          title="Chưa có phiếu hỗ trợ / khiếu nại nào"
          description="Nếu bạn cần hỗ trợ về đơn hàng VietQR, bài giảng hoặc tài khoản, hãy tạo phiếu yêu cầu mới."
          action={
            <Button onClick={() => setShowCreateModal(true)}>
              <LifeBuoy className="h-4 w-4" />
              Gửi yêu cầu hỗ trợ
            </Button>
          }
        />
      ) : (
        <div className="grid gap-6 lg:grid-cols-3">
          <div className="space-y-3">
            {complaints.map((c) => (
              <Card
                key={c.id}
                className={`cursor-pointer p-4 transition-all ${
                  selected?.id === c.id
                    ? 'border-amber-500 ring-2 ring-amber-500/20'
                    : 'hover:border-slate-300'
                }`}
              >
                <div onClick={() => setSelected(c)}>
                  <div className="flex items-center justify-between gap-2">
                    <Badge
                      tone={
                        c.status === 'RESOLVED'
                          ? 'success'
                          : c.status === 'OPEN'
                            ? 'warning'
                            : 'info'
                      }
                    >
                      {c.status}
                    </Badge>
                    <Badge tone="neutral">{c.category}</Badge>
                  </div>
                  <h3 className="mt-2 line-clamp-1 text-sm font-bold text-slate-900">
                    {c.title}
                  </h3>
                  <p className="mt-1 text-[11px] text-slate-400">
                    Ưu tiên: {c.priority} • {new Date(c.createdAt).toLocaleDateString('vi-VN')}
                  </p>
                </div>
              </Card>
            ))}
          </div>

          <div className="lg:col-span-2">
            {selected && (
              <Card className="p-6 space-y-4">
                <div className="flex flex-col justify-between gap-3 border-b border-slate-100 pb-4 sm:flex-row sm:items-center">
                  <div>
                    <div className="flex items-center gap-2">
                      <h2 className="text-lg font-bold text-slate-900">{selected.title}</h2>
                      <Badge tone="brand">{selected.status}</Badge>
                    </div>
                    <p className="mt-1 text-xs text-slate-500">
                      Phân loại: {selected.category} • Mức ưu tiên: {selected.priority} • Vai trò gửi:{' '}
                      {selected.senderRole}
                    </p>
                  </div>
                  {selected.status !== 'CLOSED' && (
                    <Button size="sm" variant="outline" onClick={handleClose}>
                      <XCircle className="h-4 w-4 text-slate-600" />
                      Đóng phiếu
                    </Button>
                  )}
                </div>

                {selected.resolutionSummary && (
                  <div className="rounded-xl border border-emerald-200 bg-emerald-50/70 p-3.5 text-xs text-emerald-900">
                    <strong>Kết luận xử lý từ Admin: </strong>
                    {selected.resolutionSummary}
                  </div>
                )}

                <div className="max-h-80 space-y-3 overflow-y-auto pr-1">
                  {selected.messages.map((m) => {
                    const isMsgAdmin = m.authorRole === 'ADMIN';
                    return (
                      <div
                        key={m.id}
                        className={`rounded-xl p-3.5 text-xs ${
                          isMsgAdmin
                            ? 'border border-amber-200 bg-amber-50/70 text-slate-900'
                            : 'bg-slate-100 text-slate-800'
                        }`}
                      >
                        <div className="flex items-center justify-between font-bold">
                          <span>{isMsgAdmin ? 'Ban Quản trị (ADMIN)' : `Người gửi (${m.authorRole})`}</span>
                          <span className="text-[10px] font-normal text-slate-400">
                            {new Date(m.createdAt).toLocaleString('vi-VN')}
                          </span>
                        </div>
                        <p className="mt-1 whitespace-pre-line">{m.content}</p>
                      </div>
                    );
                  })}
                </div>

                {selected.status !== 'CLOSED' && (
                  <form onSubmit={handleSendMessage} className="space-y-3 border-t border-slate-100 pt-4">
                    <Textarea
                      placeholder="Nhập nội dung phản hồi trao đổi..."
                      rows={2}
                      value={replyContent}
                      onChange={(e) => setReplyContent(e.target.value)}
                      required
                    />
                    <div className="flex justify-end">
                      <Button type="submit" size="sm" loading={sendingReply}>
                        <Send className="h-3.5 w-3.5" />
                        Gửi tin nhắn
                      </Button>
                    </div>
                  </form>
                )}

                {isAdmin && selected.status !== 'RESOLVED' && selected.status !== 'CLOSED' && (
                  <div className="rounded-xl border border-slate-200 bg-slate-50 p-4 space-y-3">
                    <h4 className="text-xs font-bold uppercase text-slate-700">
                      Xác nhận Giải quyết Khiếu nại (Dành cho Admin)
                    </h4>
                    <Input
                      placeholder="Nhập kết luận giải quyết cho người dùng..."
                      value={resolutionSummary}
                      onChange={(e) => setResolutionSummary(e.target.value)}
                    />
                    <Button size="sm" loading={resolving} onClick={handleResolve}>
                      <CheckCircle2 className="h-4 w-4" />
                      Đánh dấu Đã giải quyết (RESOLVED)
                    </Button>
                  </div>
                )}
              </Card>
            )}
          </div>
        </div>
      )}

      <Modal
        open={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        title="Tạo Phiếu Yêu cầu Hỗ trợ / Khiếu nại mới"
      >
        <form onSubmit={handleCreate} className="space-y-3">
          <Input
            label="Tiêu đề yêu cầu"
            placeholder="Cần hỗ trợ kiểm tra giao dịch thanh toán đơn hàng..."
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />
          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Danh mục"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
            >
              <option value="PAYMENT">Thanh toán & Đơn hàng</option>
              <option value="COURSE_CONTENT">Nội dung Khóa học</option>
              <option value="TECHNICAL">Lỗi Kỹ thuật</option>
              <option value="ACCOUNT">Tài khoản & Bảo mật</option>
              <option value="OTHER">Khác</option>
            </Select>

            <Select
              label="Mức độ ưu tiên"
              value={priority}
              onChange={(e) => setPriority(e.target.value)}
            >
              <option value="LOW">Thấp (LOW)</option>
              <option value="MEDIUM">Trung bình (MEDIUM)</option>
              <option value="HIGH">Cao (HIGH)</option>
              <option value="URGENT">Khẩn cấp (URGENT)</option>
            </Select>
          </div>
          <Textarea
            label="Nội dung chi tiết"
            rows={4}
            placeholder="Mô tả rõ vấn đề bạn đang gặp phải để được hỗ trợ nhanh nhất..."
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
          />
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="ghost" onClick={() => setShowCreateModal(false)}>
              Hủy
            </Button>
            <Button type="submit" loading={creating}>
              Gửi phiếu hỗ trợ
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
