import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { FileCheck2, Power, Settings, ShieldAlert } from 'lucide-react';
import { adminApi, AdminAuditLogDto } from '../api/adminApi';
import { getApiErrorMessage } from '@/shared/types/api';
import {
  Badge,
  Button,
  Card,
  ErrorState,
  Input,
  Spinner,
} from '@/shared/ui/primitives';

export const AdminSettingsPage: React.FC = () => {
  const [settings, setSettings] = useState<Record<string, string>>({});
  const [auditLogs, setAuditLogs] = useState<AdminAuditLogDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [maintenanceEnabled, setMaintenanceEnabled] = useState(false);
  const [maintenanceMessage, setMaintenanceMessage] = useState(
    'Hệ thống đang bảo trì nâng cấp định kỳ. Vui lòng quay lại sau ít phút.'
  );
  const [togglingMaintenance, setTogglingMaintenance] = useState(false);

  const [settingKey, setSettingKey] = useState('platform.fee_percent');
  const [settingValue, setSettingValue] = useState('20');
  const [settingReason, setSettingReason] = useState('Cập nhật cấu hình vận hành');
  const [savingSetting, setSavingSetting] = useState(false);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [settingsMap, logsPage] = await Promise.all([
        adminApi.getAllSettings(),
        adminApi.getAuditLogs({ page: 0, size: 25 }),
      ]);
      setSettings(settingsMap);
      setAuditLogs(logsPage.content || []);
      setMaintenanceEnabled(settingsMap['system.maintenance_mode'] === 'true');
      if (settingsMap['system.maintenance_message']) {
        setMaintenanceMessage(settingsMap['system.maintenance_message']);
      }
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải cấu hình hệ thống và nhật ký kiểm toán.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleToggleMaintenance = async () => {
    const nextState = !maintenanceEnabled;
    setTogglingMaintenance(true);
    try {
      await adminApi.toggleMaintenanceMode({
        enabled: nextState,
        message: maintenanceMessage.trim(),
      });
      setMaintenanceEnabled(nextState);
      toast.success(
        nextState
          ? 'Đã BẬT chế độ bảo trì toàn hệ thống!'
          : 'Đã TẮT chế độ bảo trì, hệ thống hoạt động bình thường!'
      );
      fetchData();
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể chuyển đổi chế độ bảo trì.'));
    } finally {
      setTogglingMaintenance(false);
    }
  };

  const handleSaveSetting = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!settingKey.trim()) return;
    setSavingSetting(true);
    try {
      await adminApi.updateSetting(settingKey.trim(), {
        value: settingValue.trim(),
        reason: settingReason.trim(),
      });
      toast.success(`Đã cập nhật tham số "${settingKey}" và ghi vết Audit Log!`);
      fetchData();
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể cập nhật tham số cấu hình.'));
    } finally {
      setSavingSetting(false);
    }
  };

  if (loading) return <Spinner label="Đang tải cấu hình hệ thống & Audit Logs..." />;
  if (error) return <ErrorState message={error} onRetry={fetchData} />;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">
          Cấu hình Hệ thống, Chế độ Bảo trì & Nhật ký Kiểm toán (UC44)
        </h1>
        <p className="text-sm text-slate-600">
          Quản lý công tắc bảo trì khẩn cấp và theo dõi toàn bộ dấu vết kiểm toán (Audit Logs)
        </p>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card className="p-5 space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <div className="flex items-center gap-2">
              <ShieldAlert className="h-5 w-5 text-amber-600" />
              <h2 className="text-base font-bold text-slate-900">
                Chế độ Bảo trì Toàn hệ thống (Maintenance Mode)
              </h2>
            </div>
            <Badge tone={maintenanceEnabled ? 'danger' : 'success'}>
              {maintenanceEnabled ? 'ĐANG BẢO TRÌ' : 'HOẠT ĐỘNG BÌNH THƯỜNG'}
            </Badge>
          </div>

          <Input
            label="Thông điệp hiển thị cho người dùng khi bảo trì"
            value={maintenanceMessage}
            onChange={(e) => setMaintenanceMessage(e.target.value)}
          />

          <Button
            variant={maintenanceEnabled ? 'secondary' : 'danger'}
            loading={togglingMaintenance}
            onClick={handleToggleMaintenance}
            className="w-full"
          >
            <Power className="h-4 w-4" />
            {maintenanceEnabled
              ? 'Tắt Chế độ Bảo trì (Mở lại Hệ thống)'
              : 'Kích hoạt Chế độ Bảo trì Khẩn cấp'}
          </Button>
        </Card>

        <Card className="p-5 space-y-4">
          <div className="flex items-center gap-2 border-b border-slate-100 pb-3">
            <Settings className="h-5 w-5 text-amber-600" />
            <h2 className="text-base font-bold text-slate-900">
              Cập nhật Tham số Cấu hình Hệ thống
            </h2>
          </div>

          <form onSubmit={handleSaveSetting} className="space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <Input
                label="Khóa cấu hình (Key)"
                value={settingKey}
                onChange={(e) => setSettingKey(e.target.value)}
                required
              />
              <Input
                label="Giá trị (Value)"
                value={settingValue}
                onChange={(e) => setSettingValue(e.target.value)}
                required
              />
            </div>
            <Input
              label="Lý do thay đổi (Ghi vào Audit Log)"
              value={settingReason}
              onChange={(e) => setSettingReason(e.target.value)}
              required
            />
            <Button type="submit" loading={savingSetting} className="w-full">
              Lưu Tham số Cấu hình
            </Button>
          </form>

          {Object.keys(settings).length > 0 && (
            <div className="rounded-xl bg-slate-50 p-3 text-xs space-y-1">
              <p className="font-bold text-slate-700">Các cấu hình hiện hành:</p>
              {Object.entries(settings).map(([k, v]) => (
                <div key={k} className="flex justify-between font-mono text-[11px]">
                  <span className="text-slate-600">{k}</span>
                  <span className="font-semibold text-slate-900">{v}</span>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>

      <Card className="p-5">
        <div className="mb-4 flex items-center gap-2 border-b border-slate-100 pb-3">
          <FileCheck2 className="h-5 w-5 text-amber-600" />
          <h2 className="text-base font-bold text-slate-900">
            Nhật ký Kiểm toán Tập trung (Centralized Audit Logs — {auditLogs.length} bản ghi gần nhất)
          </h2>
        </div>

        <div className="divide-y divide-slate-100">
          {auditLogs.map((log) => (
            <div key={log.id} className="py-3 text-xs flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <div className="flex items-center gap-2">
                  <Badge tone="brand">{log.action}</Badge>
                  <span className="font-semibold text-slate-900">{log.entityType}</span>
                  {log.actorRole && <Badge tone="neutral">{log.actorRole}</Badge>}
                </div>
                {log.reason && <p className="mt-1 text-slate-600">Lý do: {log.reason}</p>}
              </div>
              <span className="text-[11px] text-slate-400">
                {new Date(log.createdAt).toLocaleString('vi-VN')}
              </span>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
};
