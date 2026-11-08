import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { Award, Download, Search, ShieldCheck } from 'lucide-react';
import { CertificateItem, learningApi } from '../api/learningApi';
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

export const CertificatesPage: React.FC = () => {
  const [certificates, setCertificates] = useState<CertificateItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [downloadingId, setDownloadingId] = useState<string | null>(null);

  const [verifyCode, setVerifyCode] = useState('');
  const [verifiedCert, setVerifiedCert] = useState<CertificateItem | null>(null);
  const [verifying, setVerifying] = useState(false);

  const fetchCertificates = async () => {
    setLoading(true);
    setError(null);
    try {
      const list = await learningApi.getMyCertificates();
      setCertificates(list);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Không thể tải danh sách chứng chỉ số.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCertificates();
  }, []);

  const handleDownloadPdf = async (cert: CertificateItem) => {
    setDownloadingId(cert.id);
    try {
      const blob = await learningApi.downloadCertificatePdf(cert.id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `BeeAcademy-Certificate-${cert.certificateCode}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      toast.success('Đã tải xuống file PDF Chứng chỉ số chuẩn Landscape A4!');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không thể tải file PDF chứng chỉ.'));
    } finally {
      setDownloadingId(null);
    }
  };

  const handleVerifyLookup = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!verifyCode.trim()) return;
    setVerifying(true);
    setVerifiedCert(null);
    try {
      const found = await learningApi.verifyCertificateByCode(verifyCode.trim());
      setVerifiedCert(found);
      toast.success('Chứng chỉ hợp lệ và đã được xác thực!');
    } catch (err) {
      toast.error(getApiErrorMessage(err, 'Không tìm thấy chứng chỉ với mã tra cứu này.'));
    } finally {
      setVerifying(false);
    }
  };

  if (loading) return <Spinner label="Đang tải danh sách Chứng chỉ số..." />;
  if (error) return <ErrorState message={error} onRetry={fetchCertificates} />;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">
          Chứng chỉ số & Tra cứu Xác thực (UC20)
        </h1>
        <p className="text-sm text-slate-600">
          Chứng chỉ hoàn thành khóa học định dạng PDF Landscape A4 kèm mã QR chống giả mạo
        </p>
      </div>

      <Card className="p-5">
        <h2 className="text-sm font-bold text-slate-900">
          Tra cứu Xác thực Chứng chỉ Công khai
        </h2>
        <form onSubmit={handleVerifyLookup} className="mt-3 flex flex-col gap-3 sm:flex-row">
          <div className="flex-1">
            <Input
              placeholder="Nhập mã định danh chứng chỉ (VD: BEE-2026-XXXX)..."
              value={verifyCode}
              onChange={(e) => setVerifyCode(e.target.value)}
            />
          </div>
          <Button type="submit" variant="secondary" loading={verifying}>
            <Search className="h-4 w-4" />
            Xác minh mã Chứng chỉ
          </Button>
        </form>

        {verifiedCert && (
          <div className="mt-4 rounded-xl border border-emerald-200 bg-emerald-50/70 p-4 text-xs text-emerald-900">
            <div className="flex items-center gap-2 font-bold text-emerald-800">
              <ShieldCheck className="h-4 w-4 text-emerald-600" />
              Chứng chỉ Hợp lệ — Xác thực bởi Hệ thống BeeAcademy
            </div>
            <p className="mt-1">
              Học viên: <strong>{verifiedCert.studentName}</strong> • Khóa học:{' '}
              <strong>{verifiedCert.courseTitle}</strong> • Mã số:{' '}
              <code>{verifiedCert.certificateCode}</code>
            </p>
          </div>
        )}
      </Card>

      {certificates.length === 0 ? (
        <EmptyState
          title="Bạn chưa có chứng chỉ số nào"
          description="Khi hoàn thành 100% các bài giảng trong một khóa học, hệ thống sẽ tự động cấp Chứng chỉ số PDF cho bạn."
        />
      ) : (
        <div className="grid gap-4 sm:grid-cols-2">
          {certificates.map((cert) => (
            <Card key={cert.id} className="flex flex-col justify-between p-5">
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <Badge tone="success">Chứng chỉ Chính thức</Badge>
                  <span className="font-mono text-xs font-bold text-amber-700">
                    {cert.certificateCode}
                  </span>
                </div>
                <h3 className="text-base font-bold text-slate-900">{cert.courseTitle}</h3>
                <p className="text-xs text-slate-600">
                  Cấp cho học viên: <strong>{cert.studentName}</strong>
                </p>
                <p className="text-xs text-slate-400">
                  Ngày cấp: {new Date(cert.issuedAt).toLocaleDateString('vi-VN')}
                </p>
              </div>

              <div className="mt-4 flex items-center justify-between border-t border-slate-100 pt-3">
                <Button
                  size="sm"
                  loading={downloadingId === cert.id}
                  onClick={() => handleDownloadPdf(cert)}
                >
                  <Download className="h-4 w-4" />
                  Tải PDF Landscape A4
                </Button>
                <Button
                  size="sm"
                  variant="ghost"
                  onClick={() => {
                    setVerifyCode(cert.certificateCode);
                    setVerifiedCert(cert);
                  }}
                >
                  <Award className="h-4 w-4 text-amber-600" />
                  Kiểm tra QR
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};
