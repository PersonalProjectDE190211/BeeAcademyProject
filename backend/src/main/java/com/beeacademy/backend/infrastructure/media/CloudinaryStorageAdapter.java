package com.beeacademy.backend.infrastructure.media;

import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.media.MediaSignature;
import com.beeacademy.backend.domain.media.MediaStoragePort;
import com.beeacademy.backend.domain.media.MediaUploadResult;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Adapter triển khai Domain Port {@link MediaStoragePort} kết nối Cloudinary.
 * Hỗ trợ:
 * 1. Tải ảnh & nén tối ưu webp (thumbnails, avatar).
 * 2. Tải video dung lượng lớn (chunked upload) & tự động lấy thời lượng video tính bằng giây.
 * 3. Tạo chữ ký số HMAC-SHA1 bảo mật cho Signed Client Upload (Frontend upload thẳng lên CDN).
 * 4. Chế độ Mock / Dev an toàn khi chưa cung cấp khóa bí mật.
 */
@Component
public class CloudinaryStorageAdapter implements MediaStoragePort {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageAdapter.class);

    private final Cloudinary cloudinary;

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    public CloudinaryStorageAdapter(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    private boolean isConfigured() {
        return cloudName != null && !cloudName.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && apiSecret != null && !apiSecret.isBlank();
    }

    @Override
    public MediaUploadResult uploadImage(byte[] bytes, String originalFilename, String folder) {
        if (bytes == null || bytes.length == 0) {
            throw new DomainException("EMPTY_FILE", "File hình ảnh không được để trống");
        }

        String targetFolder = (folder != null && !folder.isBlank()) ? folder : "beeacademy/courses/thumbnails";

        if (!isConfigured()) {
            log.warn("Cloudinary chưa được cấu hình key. Trả về Mock URL phục vụ Local Dev.");
            String mockPublicId = targetFolder + "/" + UUID.randomUUID();
            return new MediaUploadResult(
                    "https://res.cloudinary.com/demo/image/upload/sample.jpg",
                    mockPublicId,
                    "image",
                    "jpg",
                    bytes.length,
                    null
            );
        }

        try {
            Map<?, ?> uploadParams = ObjectUtils.asMap(
                    "folder", targetFolder,
                    "resource_type", "image",
                    "overwrite", true
            );

            Map<?, ?> result = cloudinary.uploader().upload(bytes, uploadParams);

            String secureUrl = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");
            String format = (String) result.get("format");
            long fileSize = result.get("bytes") instanceof Number n ? n.longValue() : bytes.length;

            return new MediaUploadResult(secureUrl, publicId, "image", format, fileSize, null);
        } catch (IOException e) {
            log.error("Lỗi khi tải ảnh lên Cloudinary: {}", e.getMessage(), e);
            throw new DomainException("UPLOAD_FAILED", "Không thể tải ảnh lên Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public MediaUploadResult uploadVideo(byte[] bytes, String originalFilename, String folder) {
        if (bytes == null || bytes.length == 0) {
            throw new DomainException("EMPTY_FILE", "File video không được để trống");
        }

        String targetFolder = (folder != null && !folder.isBlank()) ? folder : "beeacademy/courses/videos";

        if (!isConfigured()) {
            log.warn("Cloudinary chưa được cấu hình key. Trả về Mock Video URL phục vụ Local Dev.");
            String mockPublicId = targetFolder + "/" + UUID.randomUUID();
            return new MediaUploadResult(
                    "https://res.cloudinary.com/demo/video/upload/dog.mp4",
                    mockPublicId,
                    "video",
                    "mp4",
                    bytes.length,
                    600 // Mặc định 10 phút giả lập
            );
        }

        try {
            Map<?, ?> uploadParams = ObjectUtils.asMap(
                    "folder", targetFolder,
                    "resource_type", "video",
                    "chunk_size", 6000000 // 6MB chunking
            );

            // Dùng uploadLarge cho file video lớn
            Map<?, ?> result = cloudinary.uploader().uploadLarge(new ByteArrayInputStream(bytes), uploadParams);

            String secureUrl = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");
            String format = (String) result.get("format");
            long fileSize = result.get("bytes") instanceof Number n ? n.longValue() : bytes.length;

            // Cloudinary trả về duration dạng Double (giây)
            Integer durationSec = 0;
            if (result.get("duration") instanceof Number num) {
                durationSec = (int) Math.round(num.doubleValue());
            }

            return new MediaUploadResult(secureUrl, publicId, "video", format, fileSize, durationSec);
        } catch (Exception e) {
            log.error("Lỗi khi tải video lên Cloudinary: {}", e.getMessage(), e);
            throw new DomainException("UPLOAD_FAILED", "Không thể tải video lên Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public MediaSignature generateUploadSignature(String folder, String resourceType) {
        long timestamp = Instant.now().getEpochSecond();
        String targetFolder = (folder != null && !folder.isBlank()) ? folder : "beeacademy/uploads";
        String targetType = (resourceType != null && !resourceType.isBlank()) ? resourceType : "auto";

        if (!isConfigured()) {
            return new MediaSignature(
                    "mock_signature",
                    timestamp,
                    "mock_api_key",
                    "bee-academy",
                    targetFolder
            );
        }

        Map<String, Object> paramsToSign = new HashMap<>();
        paramsToSign.put("folder", targetFolder);
        paramsToSign.put("timestamp", timestamp);

        String signature = cloudinary.apiSignRequest(paramsToSign, apiSecret);

        return new MediaSignature(
                signature,
                timestamp,
                apiKey,
                cloudName,
                targetFolder
        );
    }

    @Override
    public void delete(String publicId, String resourceType) {
        if (!isConfigured() || publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            Map<?, ?> params = ObjectUtils.asMap(
                    "resource_type", (resourceType != null && !resourceType.isBlank()) ? resourceType : "image"
            );
            cloudinary.uploader().destroy(publicId, params);
            log.info("Đã xóa media trên Cloudinary: publicId={}", publicId);
        } catch (Exception e) {
            log.warn("Không thể xóa media trên Cloudinary: publicId={}, error={}", publicId, e.getMessage());
        }
    }
}

