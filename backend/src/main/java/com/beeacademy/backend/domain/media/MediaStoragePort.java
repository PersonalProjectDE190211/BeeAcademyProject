package com.beeacademy.backend.domain.media;

/**
 * Cổng Domain Port cho dịch vụ Lưu trữ & Phân phối Media (Cloudinary Storage Port).
 * Bảo đảm tầng Domain và Application hoàn toàn độc lập với SDK Cloudinary cụ thể.
 */
public interface MediaStoragePort {

    /**
     * Tải ảnh lên Cloudinary (Thumbnail khóa học, Ảnh đại diện người dùng, ...).
     */
    MediaUploadResult uploadImage(byte[] bytes, String originalFilename, String folder);

    /**
     * Tải video lên Cloudinary (Bài giảng bài học, ...).
     * Tự động trích xuất thời lượng video (duration) tính bằng giây.
     */
    MediaUploadResult uploadVideo(byte[] bytes, String originalFilename, String folder);

    /**
     * Sinh chữ ký số Signed Upload để Frontend tải trực tiếp video lớn lên Cloudinary API.
     */
    MediaSignature generateUploadSignature(String folder, String resourceType);

    /**
     * Xóa tài nguyên media trên Cloudinary theo Public ID.
     */
    void delete(String publicId, String resourceType);
}

