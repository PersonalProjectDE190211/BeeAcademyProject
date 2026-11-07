package com.beeacademy.backend.infrastructure.learning.service;

import com.beeacademy.backend.domain.learning.CertificatePdfGeneratorPort;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Dịch vụ tạo file PDF Chứng chỉ số hoàn thành khóa học kèm mã QR xác minh.
 * - Khổ giấy ngang Landscape A4 chuẩn quốc tế.
 * - Tạo mã QR xác thực bảo mật trỏ đến URL kiểm tra công khai.
 * - Khung viền trang trọng (Academic border) màu vàng kim và xanh hải quân.
 */
@Service
public class PdfCertificateService implements CertificatePdfGeneratorPort {

    private static final Logger log = LoggerFactory.getLogger(PdfCertificateService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public byte[] generateCertificatePdf(
            String certificateCode,
            String studentName,
            String courseTitle,
            java.time.Instant issuedAt
    ) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Khổ A4 nằm ngang: PageSize.A4.rotate()
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // 1. Vẽ khung viền đôi phong cách chứng chỉ học thuật (Navy & Gold Border)
            PdfContentByte cb = writer.getDirectContent();
            cb.setLineWidth(5f);
            cb.setColorStroke(new Color(30, 58, 138)); // Navy Blue (#1e3a8a)
            cb.rectangle(20, 20, PageSize.A4.rotate().getWidth() - 40, PageSize.A4.rotate().getHeight() - 40);
            cb.stroke();

            cb.setLineWidth(1.5f);
            cb.setColorStroke(new Color(217, 119, 6)); // Gold Amber (#d97706)
            cb.rectangle(26, 26, PageSize.A4.rotate().getWidth() - 52, PageSize.A4.rotate().getHeight() - 52);
            cb.stroke();

            // 2. Định nghĩa Font
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, new Color(30, 58, 138));
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 13, new Color(100, 116, 139));
            Font studentFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, new Color(15, 23, 42));
            Font courseFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(2, 132, 199));
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 12, new Color(71, 85, 105));
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 116, 139));

            // Khoảng cách đầu trang
            Paragraph spaceTop = new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 10));
            spaceTop.setSpacingBefore(15);
            document.add(spaceTop);

            // Tên Viện / Học viện
            Paragraph orgName = new Paragraph("BEE ACADEMY LEARNING PLATFORM", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(217, 119, 6)));
            orgName.setAlignment(Element.ALIGN_CENTER);
            document.add(orgName);

            // Tiêu đề Chứng chỉ
            Paragraph title = new Paragraph("CERTIFICATE OF COMPLETION", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(8);
            document.add(title);

            Paragraph certSubtitle = new Paragraph("CHỨNG CHỈ HOÀN THÀNH KHÓA HỌC", subTitleFont);
            certSubtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(certSubtitle);

            // Lời đề tặng
            Paragraph presentedTo = new Paragraph("Chứng nhận học viên / This is proudly presented to:", labelFont);
            presentedTo.setAlignment(Element.ALIGN_CENTER);
            presentedTo.setSpacingBefore(18);
            document.add(presentedTo);

            // Tên Học viên
            Paragraph student = new Paragraph(studentName, studentFont);
            student.setAlignment(Element.ALIGN_CENTER);
            student.setSpacingBefore(6);
            document.add(student);

            // Nội dung hoàn thành
            Paragraph completedText = new Paragraph("Đã hoàn thành xuất sắc toàn bộ chương trình và bài kiểm tra khóa học:", labelFont);
            completedText.setAlignment(Element.ALIGN_CENTER);
            completedText.setSpacingBefore(10);
            document.add(completedText);

            // Tên Khóa học
            Paragraph course = new Paragraph(courseTitle, courseFont);
            course.setAlignment(Element.ALIGN_CENTER);
            course.setSpacingBefore(6);
            document.add(course);

            // 3. Bảng Chân trang: Mã số, Ngày cấp, Chữ ký và Mã QR xác minh
            Table footerTable = new Table(3);
            footerTable.setBorder(Table.NO_BORDER);
            footerTable.setWidth(100);
            footerTable.setWidths(new float[]{38, 24, 38});
            footerTable.setPadding(0);
            footerTable.setSpacing(0);

            // Cột Trái: Mã số và Ngày cấp
            String formattedDate = DATE_FORMATTER.format(issuedAt != null ? issuedAt : java.time.Instant.now());
            Cell leftCell = new Cell();
            leftCell.setBorder(Cell.NO_BORDER);
            leftCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            leftCell.add(new Paragraph("Mã số chứng chỉ (Serial): " + certificateCode, metaFont));
            leftCell.add(new Paragraph("Ngày cấp (Issue Date): " + formattedDate, metaFont));
            leftCell.add(new Paragraph("Tra cứu xác thực: " + frontendUrl + "/verify/" + certificateCode, metaFont));
            footerTable.addCell(leftCell);

            // Cột Giữa: Mã QR xác minh
            Cell qrCell = new Cell();
            qrCell.setBorder(Cell.NO_BORDER);
            qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            qrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            try {
                String verifyUrl = frontendUrl + "/verify-certificate/" + certificateCode;
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                BitMatrix bitMatrix = qrCodeWriter.encode(verifyUrl, BarcodeFormat.QR_CODE, 100, 100);
                ByteArrayOutputStream qrOut = new ByteArrayOutputStream();
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", qrOut);

                Image qrImage = Image.getInstance(qrOut.toByteArray());
                qrImage.scalePercent(80);
                qrCell.add(qrImage);
            } catch (Exception e) {
                log.warn("Không thể sinh ảnh QR code: {}", e.getMessage());
            }
            footerTable.addCell(qrCell);

            // Cột Phải: Chữ ký số
            Cell rightCell = new Cell();
            rightCell.setBorder(Cell.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            rightCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            Paragraph signRole = new Paragraph("BEE ACADEMY ACADEMIC BOARD", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(30, 58, 138)));
            signRole.setAlignment(Element.ALIGN_RIGHT);
            Paragraph verifiedStamp = new Paragraph("[ Digitally Certified & Verified ]", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, new Color(16, 185, 129)));
            verifiedStamp.setAlignment(Element.ALIGN_RIGHT);
            rightCell.add(signRole);
            rightCell.add(verifiedStamp);
            footerTable.addCell(rightCell);

            Paragraph tableWrapper = new Paragraph();
            tableWrapper.setSpacingBefore(30);
            tableWrapper.add(footerTable);
            document.add(tableWrapper);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Lỗi khi sinh PDF chứng chỉ: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo file PDF chứng chỉ: " + e.getMessage(), e);
        }
    }
}
