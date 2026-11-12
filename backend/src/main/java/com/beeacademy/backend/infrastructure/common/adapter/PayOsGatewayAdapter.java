package com.beeacademy.backend.infrastructure.common.adapter;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;
import vn.payos.type.Transaction;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Adapter tích hợp trực tiếp cổng thanh toán PayOS (VietQR Napas 24/7).
 * - Khởi tạo link thanh toán với mã QR động EMVCo khóa cứng số tiền & nội dung (<= 25 ký tự).
 * - Render ảnh QR Base64 PNG trực tiếp từ chuỗi EMVCo của PayOS bằng Google ZXing.
 * - Hỗ trợ đối chiếu trạng thái giao dịch trực tiếp từ PayOS API (getPaymentLinkInformation).
 * - Hỗ trợ hủy link thanh toán ngay lập tức khi Rollback hoặc khi người dùng hủy đơn (cancelPaymentLink).
 * - Xác thực chữ ký HMAC-SHA256 của PayOS Webhook bằng checksumKey.
 */
@Component
public class PayOsGatewayAdapter {

    private static final Logger log = LoggerFactory.getLogger(PayOsGatewayAdapter.class);

    private static final String DEFAULT_BIN = "970422";
    private static final String DEFAULT_BANK_NAME = "Ngân hàng Quân Đội (MBBank)";
    private static final String DEFAULT_ACCOUNT_NUMBER = "0348756972";
    private static final String DEFAULT_ACCOUNT_NAME = "BEE ACADEMY";

    private static final Map<String, String> BIN_TO_BANK_NAME = Map.ofEntries(
            Map.entry("970422", "Ngân hàng Quân Đội (MBBank)"),
            Map.entry("970436", "Ngân hàng Ngoại thương (Vietcombank)"),
            Map.entry("970407", "Ngân hàng Kỹ thương (Techcombank)"),
            Map.entry("970415", "Ngân hàng Công thương (VietinBank)"),
            Map.entry("970418", "Ngân hàng Đầu tư & Phát triển (BIDV)"),
            Map.entry("970416", "Ngân hàng Á Châu (ACB)"),
            Map.entry("970432", "Ngân hàng Việt Nam Thịnh Vượng (VPBank)"),
            Map.entry("970423", "Ngân hàng Tiên Phong (TPBank)"),
            Map.entry("970403", "Ngân hàng Sài Gòn Thương Tín (Sacombank)"),
            Map.entry("970448", "Ngân hàng Phương Đông (OCB)"),
            Map.entry("963388", "Ngân hàng số Timo / BVBank"),
            Map.entry("546034", "Ngân hàng số Cake by VPBank")
    );

    private final String clientId;
    private final String apiKey;
    private final String checksumKey;
    private final String frontendUrl;
    private final PayOS payOS;

    // Lưu tạm thông tin QR/Checkout của các đơn PENDING đang hoạt động để tái sử dụng nhanh
    private final Map<Long, PayOsPaymentLinkInfo> activeLinkCache = new ConcurrentHashMap<>();

    @Autowired
    public PayOsGatewayAdapter(
            @Value("${payos.client-id:}") String clientId,
            @Value("${payos.api-key:}") String apiKey,
            @Value("${payos.checksum-key:}") String checksumKey,
            @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl
    ) {
        this.clientId = clientId != null ? clientId.trim() : "";
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.checksumKey = checksumKey != null ? checksumKey.trim() : "";
        this.frontendUrl = frontendUrl != null ? frontendUrl.trim() : "http://localhost:3000";

        if (isConfigured()) {
            this.payOS = new PayOS(this.clientId, this.apiKey, this.checksumKey);
            log.info("Đã khởi tạo cổng thanh toán PayOS SDK với clientId={}", maskKey(this.clientId));
        } else {
            this.payOS = null;
            log.warn("Chưa cấu hình đầy đủ bộ key PayOS (client-id/api-key/checksum-key). Hệ thống chạy chế độ VietQR tiêu chuẩn.");
        }
    }

    public boolean isConfigured() {
        return !clientId.isBlank() && !apiKey.isBlank() && !checksumKey.isBlank();
    }

    /**
     * Khởi tạo link thanh toán trên cổng PayOS với số tiền cố định và thời hạn 15 phút.
     * Chuỗi description tự động được cắt gọn <= 25 ký tự theo quy định bắt buộc của PayOS/Napas.
     */
    public PayOsPaymentLinkInfo createPaymentLink(
            long orderCode,
            int amountVnd,
            String rawDescription,
            List<PayOsItem> items,
            Instant expiresAt
    ) {
        String safeDescription = sanitizeDescription(rawDescription, orderCode);

        if (payOS != null && amountVnd >= 2000) {
            try {
                PaymentData.PaymentDataBuilder builder = PaymentData.builder()
                        .orderCode(orderCode)
                        .amount(amountVnd)
                        .description(safeDescription)
                        .returnUrl(frontendUrl + "/student/orders")
                        .cancelUrl(frontendUrl + "/student/orders");

                if (expiresAt != null) {
                    long expEpochSec = expiresAt.getEpochSecond();
                    long nowSec = Instant.now().getEpochSecond();
                    if (expEpochSec > nowSec && expEpochSec <= Integer.MAX_VALUE) {
                        builder.expiredAt((int) expEpochSec);
                    }
                }

                if (items != null && !items.isEmpty()) {
                    for (PayOsItem item : items) {
                        builder.item(ItemData.builder()
                                .name(truncateItemName(item.name()))
                                .quantity(Math.max(1, item.quantity()))
                                .price(Math.max(0, item.priceVnd()))
                                .build());
                    }
                }

                CheckoutResponseData response = payOS.createPaymentLink(builder.build());
                if (response != null) {
                    String qrRaw = response.getQrCode();
                    String bin = response.getBin() != null && !response.getBin().isBlank() ? response.getBin() : DEFAULT_BIN;
                    String accNo = response.getAccountNumber() != null && !response.getAccountNumber().isBlank()
                            ? response.getAccountNumber() : DEFAULT_ACCOUNT_NUMBER;
                    String accName = response.getAccountName() != null && !response.getAccountName().isBlank()
                            ? response.getAccountName() : DEFAULT_ACCOUNT_NAME;
                    String transferDesc = response.getDescription() != null && !response.getDescription().isBlank()
                            ? response.getDescription() : safeDescription;

                    // Render QR image data URI từ chuỗi EMVCo động khóa cứng số tiền của PayOS
                    String qrImageUrl = (qrRaw != null && !qrRaw.isBlank())
                            ? renderEmvCoQrToDataUri(qrRaw, 360)
                            : buildFallbackVietQrUrl(bin, accNo, accName, amountVnd, transferDesc);

                    PayOsPaymentLinkInfo info = new PayOsPaymentLinkInfo(
                            orderCode,
                            response.getPaymentLinkId() != null ? response.getPaymentLinkId() : "PAYOS_" + orderCode,
                            response.getCheckoutUrl() != null ? response.getCheckoutUrl() : "https://pay.payos.vn/web/" + response.getPaymentLinkId(),
                            qrImageUrl,
                            qrRaw,
                            bin,
                            resolveBankName(bin),
                            accNo,
                            accName,
                            response.getAmount() != null ? response.getAmount() : amountVnd,
                            transferDesc,
                            true
                    );
                    activeLinkCache.put(orderCode, info);
                    log.info("Đã tạo link thanh toán PayOS thật thành công: orderCode={}, linkId={}, amount={}",
                            orderCode, info.paymentLinkId(), info.amountVnd());
                    return info;
                }
            } catch (Exception e) {
                log.warn("Không thể tạo link trực tiếp trên PayOS cho orderCode={} (reason={}). Sử dụng VietQR dự phòng.",
                        orderCode, e.getMessage());
            }
        }

        String fallbackQrUrl = buildFallbackVietQrUrl(DEFAULT_BIN, DEFAULT_ACCOUNT_NUMBER, DEFAULT_ACCOUNT_NAME, amountVnd, safeDescription);
        PayOsPaymentLinkInfo fallbackInfo = new PayOsPaymentLinkInfo(
                orderCode,
                "PAYOS_" + orderCode,
                "https://pay.payos.vn/web/PAYOS_" + orderCode,
                fallbackQrUrl,
                null,
                DEFAULT_BIN,
                DEFAULT_BANK_NAME,
                DEFAULT_ACCOUNT_NUMBER,
                DEFAULT_ACCOUNT_NAME,
                amountVnd,
                safeDescription,
                false
        );
        activeLinkCache.put(orderCode, fallbackInfo);
        return fallbackInfo;
    }

    /**
     * Lấy lại thông tin QR đã cache của đơn hàng đang PENDING (hoặc tái tạo nếu cache trống).
     */
    public PayOsPaymentLinkInfo getOrRebuildLinkInfo(long orderCode, int amountVnd, String paymentRef, String paymentLinkId) {
        PayOsPaymentLinkInfo cached = activeLinkCache.get(orderCode);
        if (cached != null) {
            return cached;
        }
        String safeDesc = sanitizeDescription(paymentRef, orderCode);
        String qrUrl = buildFallbackVietQrUrl(DEFAULT_BIN, DEFAULT_ACCOUNT_NUMBER, DEFAULT_ACCOUNT_NAME, amountVnd, safeDesc);
        String linkId = paymentLinkId != null ? paymentLinkId : "PAYOS_" + orderCode;
        return new PayOsPaymentLinkInfo(
                orderCode,
                linkId,
                "https://pay.payos.vn/web/" + linkId,
                qrUrl,
                null,
                DEFAULT_BIN,
                DEFAULT_BANK_NAME,
                DEFAULT_ACCOUNT_NUMBER,
                DEFAULT_ACCOUNT_NAME,
                amountVnd,
                safeDesc,
                false
        );
    }

    /**
     * Chủ động truy vấn trạng thái thanh toán của đơn hàng trực tiếp từ máy chủ PayOS.
     * Dùng cho:
     * 1. Nút "Tôi đã chuyển khoản" / Polling trên Localhost không cần ngrok.
     * 2. Kiểm tra trước khi cho phép hủy đơn hàng.
     * 3. Job tự động đối soát ngầm khi Server khởi động lại sau sự cố sập web.
     */
    public PayOsLinkStatus queryPaymentStatus(Long orderCode) {
        if (orderCode == null || payOS == null) {
            return PayOsLinkStatus.unknown(orderCode);
        }
        try {
            PaymentLinkData data = payOS.getPaymentLinkInformation(orderCode);
            if (data == null) {
                return PayOsLinkStatus.unknown(orderCode);
            }
            String status = data.getStatus() != null ? data.getStatus().toUpperCase() : "UNKNOWN";
            int amount = data.getAmount() != null ? data.getAmount() : 0;
            int amountPaid = data.getAmountPaid() != null ? data.getAmountPaid() : 0;

            String reference = null;
            if (data.getTransactions() != null && !data.getTransactions().isEmpty()) {
                Transaction tx = data.getTransactions().getFirst();
                reference = tx.getReference();
            }

            boolean isPaid = "PAID".equals(status) && (amount <= 0 || amountPaid >= amount);
            return new PayOsLinkStatus(
                    orderCode,
                    status,
                    amount,
                    amountPaid,
                    reference,
                    isPaid,
                    true
            );
        } catch (Exception e) {
            log.debug("Không thể truy vấn trạng thái đơn {} trên PayOS: {}", orderCode, e.getMessage());
            return PayOsLinkStatus.unknown(orderCode);
        }
    }

    /**
     * Hủy link thanh toán và vô hiệu hóa mã QR trên cổng PayOS ngay lập tức.
     * Được gọi khi:
     * 1. Rollback / Compensating Transaction khi DB gặp lỗi sau khi đã tạo link PayOS.
     * 2. Học sinh chủ động bấm "Hủy đơn hàng".
     * 3. Đơn hàng quá hạn 15 phút hoặc bị thay thế bởi đơn mới.
     */
    public boolean cancelPaymentLinkSafely(Long orderCode, String reason) {
        if (orderCode == null) {
            return false;
        }
        activeLinkCache.remove(orderCode);
        if (payOS == null) {
            return false;
        }
        try {
            String safeReason = (reason != null && !reason.isBlank()) ? reason : "Cancelled by system";
            payOS.cancelPaymentLink(orderCode, safeReason);
            log.info("Đã vô hiệu hóa mã QR PayOS cho orderCode={} (reason={})", orderCode, safeReason);
            return true;
        } catch (Exception e) {
            log.debug("Bỏ qua lỗi hủy link PayOS cho orderCode={}: {}", orderCode, e.getMessage());
            return false;
        }
    }

    /**
     * Xác thực chữ ký HMAC-SHA256 của Webhook gửi từ PayOS bằng PAYOS_CHECKSUM_KEY.
     * Nếu payload có chữ ký hợp lệ theo chuẩn PayOS -> trả về true.
     */
    public boolean verifyWebhookSignature(Map<String, Object> dataMap, String signature) {
        if (!isConfigured()) {
            return true;
        }
        if (signature == null || signature.isBlank() || dataMap == null || dataMap.isEmpty()) {
            return false;
        }
        try {
            TreeMap<String, Object> sorted = new TreeMap<>(dataMap);
            String dataQueryStr = sorted.entrySet().stream()
                    .map(e -> {
                        Object val = e.getValue();
                        String strVal = (val == null || "null".equals(String.valueOf(val))) ? "" : String.valueOf(val);
                        return e.getKey() + "=" + strVal;
                    })
                    .collect(Collectors.joining("&"));

            String computed = hmacSha256Hex(checksumKey, dataQueryStr);
            return computed.equalsIgnoreCase(signature.trim());
        } catch (Exception e) {
            log.error("Lỗi xác thực chữ ký Webhook PayOS: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Chuyển chuỗi EMVCo QR (`000201010212...`) do PayOS sinh ra thành ảnh PNG Base64 (`data:image/png;base64,...`).
     * Đảm bảo quét trên mọi app ngân hàng đều khóa cứng số tiền và nội dung chuyển khoản.
     */
    public String renderEmvCoQrToDataUri(String emvCoQrContent, int sizePx) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(emvCoQrContent, BarcodeFormat.QR_CODE, sizePx, sizePx, hints);

            BufferedImage image = new BufferedImage(sizePx, sizePx, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, sizePx, sizePx);
            graphics.setColor(new Color(28, 25, 23)); // Ink color #1c1917

            for (int x = 0; x < sizePx; x++) {
                for (int y = 0; y < sizePx; y++) {
                    if (bitMatrix.get(x, y)) {
                        image.setRGB(x, y, 0x1C1917);
                    }
                }
            }
            graphics.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return "data:image/png;base64," + base64;
        } catch (Exception e) {
            log.warn("Lỗi render ảnh QR từ chuỗi EMVCo: {}", e.getMessage());
            return null;
        }
    }

    public static String sanitizeDescription(String raw, long orderCode) {
        String candidate = (raw != null && !raw.isBlank()) ? raw.trim() : ("BEE" + orderCode);
        // Chỉ giữ chữ cái ASCII, chữ số và khoảng trắng để tương thích 100% chuẩn Napas 24/7
        candidate = candidate.replaceAll("[^a-zA-Z0-9 ]", "").trim();
        if (candidate.isEmpty()) {
            candidate = "BEE" + orderCode;
        }
        // Giới hạn cứng <= 25 ký tự của PayOS
        if (candidate.length() > 25) {
            candidate = candidate.substring(0, 25).trim();
        }
        return candidate;
    }

    private String truncateItemName(String name) {
        if (name == null || name.isBlank()) {
            return "Khoa hoc BeeAcademy";
        }
        return name.length() > 80 ? name.substring(0, 80) : name;
    }

    private String buildFallbackVietQrUrl(String bin, String accountNo, String accountName, int amount, String addInfo) {
        return String.format(
                "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                bin,
                accountNo,
                Math.max(0, amount),
                 java.net.URLEncoder.encode(addInfo, StandardCharsets.UTF_8),
                java.net.URLEncoder.encode(accountName, StandardCharsets.UTF_8)
        );
    }

    private String resolveBankName(String bin) {
        if (bin == null) {
            return DEFAULT_BANK_NAME;
        }
        return BIN_TO_BANK_NAME.getOrDefault(bin.trim(), "Ngân hàng Napas (" + bin + ")");
    }

    private String hmacSha256Hex(String key, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(secretKey);
        byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String maskKey(String key) {
        if (key == null || key.length() <= 8) {
            return "***";
        }
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }

    public record PayOsItem(String name, int quantity, int priceVnd) {}

    public record PayOsPaymentLinkInfo(
            long orderCode,
            String paymentLinkId,
            String checkoutUrl,
            String qrCodeUrl,
            String qrCodeRaw,
            String bankBin,
            String bankName,
            String accountNumber,
            String accountName,
            int amountVnd,
            String transferDescription,
            boolean livePayOsLink
    ) {}

    public record PayOsLinkStatus(
            Long orderCode,
            String status,
            int amount,
            int amountPaid,
            String reference,
            boolean paid,
            boolean reachable
    ) {
        public static PayOsLinkStatus unknown(Long orderCode) {
            return new PayOsLinkStatus(orderCode, "UNKNOWN", 0, 0, null, false, false);
        }
    }
}

