import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { Bell, CheckCheck } from 'lucide-react';
import { NotificationDto, supportApi } from '../api/supportApi';
import { getApiErrorMessage } from '@/shared/types/api';
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Spinner,
} from '@/shared/ui/primitives';

export const NotificationsPage: React.FC = () => {
  const [notifications, setNotifications] = useState<NotificationDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchNotifications = async () => {
    setLoading(true);
    setError(null);
    try {
      const list = await supportApi.getMyNotifications();
      setNotifications(list);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải danh sách thông báo.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const handleMarkRead = async (id: string) => {
    try {
      const updated = await supportApi.markNotificationRead(id);
      setNotifications((prev) => prev.map((n) => (n.id === id ? updated : n)));
      toast.success('Đã đánh dấu đã đọc.');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể cập nhật trạng thái thông báo.'));
    }
  };

  if (loading) return <Spinner label="Đang tải hộp thư thông báo..." />;
  if (error) return <ErrorState message={error} onRetry={fetchNotifications} />;

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Hộp thư Thông báo Hệ thống (UC22)</h1>
          <p className="text-sm text-slate-600">
            Cập nhật tức thì về đơn hàng, kết quả duyệt khóa học, hỏi đáp và chứng chỉ số
          </p>
        </div>
        <Badge tone="brand">
          Chưa đọc: {notifications.filter((n) => !n.read).length}
        </Badge>
      </div>

      {notifications.length === 0 ? (
        <EmptyState
          title="Bạn chưa có thông báo nào"
          description="Khi có sự kiện mới liên quan đến khóa học hoặc giao dịch của bạn, thông báo sẽ xuất hiện tại đây."
        />
      ) : (
        <div className="space-y-3">
          {notifications.map((n) => (
            <Card
              key={n.id}
              className={`p-4 transition-all ${
                !n.read ? 'border-amber-400 bg-amber-50/30' : 'opacity-80'
              }`}
            >
              <div className="flex items-start justify-between gap-4">
                <div className="flex items-start gap-3">
                  <div className="mt-0.5 rounded-full bg-amber-100 p-2 text-amber-700">
                    <Bell className="h-4 w-4" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <h3 className="text-sm font-bold text-slate-900">{n.title}</h3>
                      {!n.read && <Badge tone="warning">Mới</Badge>}
                    </div>
                    <p className="mt-1 text-xs text-slate-700">{n.message}</p>
                    <span className="mt-1.5 block text-[11px] text-slate-400">
                      {new Date(n.createdAt).toLocaleString('vi-VN')}
                    </span>
                  </div>
                </div>

                {!n.read && (
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => handleMarkRead(n.id)}
                  >
                    <CheckCheck className="h-4 w-4 text-emerald-600" />
                    Đã đọc
                  </Button>
                )}
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};
