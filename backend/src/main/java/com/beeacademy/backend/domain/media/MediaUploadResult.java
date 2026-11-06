package com.beeacademy.backend.domain.media;

import java.io.Serializable;

/**
 * Kết quả sau khi tải file thành công lên kho lưu trữ đám mây Cloudinary.
 */
public record MediaUploadResult(
        String url,
        String publicId,
        String resourceType,
        String format,
        long bytes,
        Integer durationSec
) implements Serializable {
}
