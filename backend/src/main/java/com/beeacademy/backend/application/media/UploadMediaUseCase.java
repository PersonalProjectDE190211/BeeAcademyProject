package com.beeacademy.backend.application.media;

import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.media.MediaSignature;
import com.beeacademy.backend.domain.media.MediaStoragePort;
import com.beeacademy.backend.domain.media.MediaUploadResult;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use case Quản lý Upload Media (Ảnh Thumbnail, Video bài giảng, Avatar) lên Cloudinary.
 */
@Service
public class UploadMediaUseCase {

    private final MediaStoragePort mediaStoragePort;

    public UploadMediaUseCase(MediaStoragePort mediaStoragePort) {
        this.mediaStoragePort = mediaStoragePort;
    }

    /**
     * Tải ảnh thumbnail cho khóa học.
     */
    public MediaUploadResult uploadCourseThumbnail(byte[] bytes, String originalFilename) {
        validateFile(bytes, originalFilename, 10 * 1024 * 1024, "hình ảnh"); // tối đa 10MB cho ảnh
        return mediaStoragePort.uploadImage(bytes, originalFilename, "beeacademy/courses/thumbnails");
    }

    /**
     * Tải video cho bài giảng.
     */
    public MediaUploadResult uploadLessonVideo(byte[] bytes, String originalFilename) {
        validateFile(bytes, originalFilename, 500L * 1024 * 1024, "video"); // tối đa 500MB cho video
        return mediaStoragePort.uploadVideo(bytes, originalFilename, "beeacademy/courses/videos");
    }

    /**
     * Tải ảnh đại diện người dùng.
     */
    public MediaUploadResult uploadAvatar(byte[] bytes, String originalFilename, UUID userId) {
        validateFile(bytes, originalFilename, 5 * 1024 * 1024, "hình ảnh"); // tối đa 5MB cho avatar
        return mediaStoragePort.uploadImage(bytes, originalFilename, "beeacademy/avatars");
    }

    /**
     * Lấy thông tin chữ ký số để Frontend tải trực tiếp video lớn lên Cloudinary API.
     */
    public MediaSignature getUploadSignature(String folder, String resourceType) {
        return mediaStoragePort.generateUploadSignature(folder, resourceType);
    }

    private void validateFile(byte[] bytes, String filename, long maxBytes, String typeName) {
        if (bytes == null || bytes.length == 0) {
            throw new DomainException("EMPTY_FILE", "File " + typeName + " không được rỗng");
        }
        if (bytes.length > maxBytes) {
            throw new DomainException("FILE_TOO_LARGE",
                    "Dung lượng file vượt quá giới hạn tối đa cho phép (" + (maxBytes / (1024 * 1024)) + "MB)");
        }
    }
}

