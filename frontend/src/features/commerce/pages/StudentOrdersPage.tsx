import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { CreditCard, QrCode, ShoppingBag, XCircle } from 'lucide-react';
import { commerceApi, OrderDto } from '../api/commerceApi';
import { formatVnd, getApiErrorMessage } from '@/shared/types/api';
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Spinner,
} from '@/shared/ui/primitives';

export const StudentOrdersPage: React.FC = () => {
  const [orders, setOrders] = useState<OrderDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [cancelingId, setCancelingId] = useState<string | null>(null);

  const fetchOrders = async () => {
    setLoading(true);
    setError(null);
    try {
      const list = await commerceApi.getMyOrders();
      setOrders(list);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải lịch sử đơn hàng.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const handleCancelOrder = async (orderId: string) => {
    setCancelingId(orderId);
    try {
      const updated = await commerceApi.cancelOrder(orderId);
      setOrders((prev) => prev.map((o) => (o.id === orderId ? updated : o)));
      toast.success('Đã hủy đơn hàng thành công.');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể hủy đơn hàng này.'));
    } finally {
      setCancelingId(null);
    }
  };

  if (loading) return <Spinner label="Đang tải danh sách đơn hàng..." />;
  if (error) return <ErrorState message={error} onRetry={fetchOrders} />;

  return (
    <div className="space-y-6">
      <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">
            Lịch sử Đơn hàng & Thanh toán VietQR (UC09 - UC12)
          </h1>
          <p className="text-sm text-slate-600">
            Quản lý các giao dịch mua khóa học trực tuyến qua cổng PayOS VietQR
          </p>
        </div>
        <Link to="/courses">
          <Button variant="secondary" size="sm">
            <ShoppingBag className="h-4 w-4" />
            Tiếp tục chọn Khóa học
          </Button>
        </Link>
      </div>

      {orders.length === 0 ? (
        <EmptyState
          title="Bạn chưa có đơn hàng nào"
          description="Hãy chọn khóa học yêu thích tại danh mục Khóa học để khởi tạo đơn thanh toán VietQR."
          action={
            <Link to="/courses">
              <Button>Khám phá Khóa học</Button>
            </Link>
          }
        />
      ) : (
        <div className="space-y-4">
          {orders.map((order) => (
            <Card key={order.id} className="p-5">
              <div className="flex flex-col justify-between gap-3 border-b border-slate-100 pb-3 sm:flex-row sm:items-center">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="font-mono text-sm font-bold text-slate-900">
                    #{order.orderCode}
                  </span>
                  <Badge
                    tone={
                      order.status === 'PAID'
                        ? 'success'
                        : order.status === 'PENDING'
                          ? 'warning'
                          : 'neutral'
                    }
                  >
                    {order.status}
                  </Badge>
                  <span className="text-xs text-slate-500">
                    Mã tham chiếu: {order.paymentRef}
                  </span>
                </div>
                <span className="text-xs text-slate-400">
                  Tạo lúc: {new Date(order.createdAt).toLocaleString('vi-VN')}
                </span>
              </div>

              <div className="mt-3 divide-y divide-slate-100">
                {order.items.map((item) => (
                  <div
                    key={item.id}
                    className="flex items-center justify-between py-2 text-xs"
                  >
                    <span className="font-semibold text-slate-800">{item.courseTitle}</span>
                    <span className="font-medium text-slate-700">
                      {formatVnd(item.priceAtPurchase)}
                    </span>
                  </div>
                ))}
              </div>

              <div className="mt-4 flex flex-col justify-between gap-3 border-t border-slate-100 pt-3 sm:flex-row sm:items-center">
                <div>
                  <span className="text-xs text-slate-500">Tổng thanh toán: </span>
                  <span className="text-lg font-extrabold text-amber-600">
                    {formatVnd(order.totalAmountVnd)}
                  </span>
                  {order.discountAmountVnd > 0 && (
                    <span className="ml-2 text-xs text-emerald-600">
                      (Đã giảm {formatVnd(order.discountAmountVnd)})
                    </span>
                  )}
                </div>

                <div className="flex items-center gap-2">
                  {order.status === 'PENDING' && (
                    <>
                      <Button
                        size="sm"
                        variant="outline"
                        loading={cancelingId === order.id}
                        onClick={() => handleCancelOrder(order.id)}
                      >
                        <XCircle className="h-4 w-4 text-red-600" />
                        Hủy đơn
                      </Button>
                      <Button
                        size="sm"
                        onClick={() =>
                          toast.success(
                            `Quét mã VietQR với nội dung chuyển khoản: ${order.paymentRef}`
                          )
                        }
                      >
                        <QrCode className="h-4 w-4" />
                        Hiển thị mã VietQR ({order.paymentRef})
                      </Button>
                    </>
                  )}
                  {order.status === 'PAID' && (
                    <Link to="/learning/my-courses">
                      <Button size="sm" variant="secondary">
                        <CreditCard className="h-4 w-4" />
                        Vào học ngay
                      </Button>
                    </Link>
                  )}
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};
