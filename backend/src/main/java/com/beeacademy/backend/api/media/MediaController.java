package com.beeacademy.backend.api.media;

import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.media.UploadMediaUseCase;
import com.beeacademy.backend.domain.media.MediaSignature;
import com.beeacademy.backend.domain.media.MediaUploadResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controller xử lý Upload Ảnh & Video lên kho lưu trữ đám mây Cloudinary.
 * Cung cấp:
 * 1. Upload ảnh (Thumbnail, Avatar) qua Backend.
 * 2. Upload video bài giảng qua Backend (tự động trích xuất thời lượng video).
 * 3. Sinh chữ ký số Signed Upload để Client tải trực tiếp video lớn lên CDN Cloudinary.
 */
@Tag(name = "7. Media & Tải tệp (Cloudinary CDN)", description = "Các API tải ảnh thumbnail, avatar và video bài giảng lên Cloudinary")
@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final UploadMediaUseCase uploadMediaUseCase;

    public MediaController(UploadMediaUseCase uploadMediaUseCase) {
        this.uploadMediaUseCase = uploadMediaUseCase;
    }

    /**
     * Tải hình ảnh (Thumbnail khóa học, hình minh họa) lên Cloudinary.
     */
    @Operation(summary = "Tải hình ảnh (Thumbnail)", description = "Tải ảnh lên Cloudinary và trả về đường dẫn URL CDN an toàn")
    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MediaUploadResult>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        MediaUploadResult result = uploadMediaUseCase.uploadCourseThumbnail(file.getBytes(), file.getOriginalFilename());
        return ResponseEntity.ok(ApiResponse.success("Tải hình ảnh thành công", result));
    }

    /**
     * Tải video bài giảng lên Cloudinary (tự động tính thời lượng video).
     */
    @Operation(summary = "Tải video bài giảng", description = "Tải video lên Cloudinary, tự động tính toán thời lượng video (duration) tính bằng giây")
    @PostMapping(value = "/upload/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<MediaUploadResult>> uploadVideo(@RequestParam("file") MultipartFile file) throws IOException {
        MediaUploadResult result = uploadMediaUseCase.uploadLessonVideo(file.getBytes(), file.getOriginalFilename());
        return ResponseEntity.ok(ApiResponse.success("Tải video bài giảng thành công", result));
    }

    /**
     * Sinh chữ ký số Signed Upload để Frontend tải trực tiếp video lớn lên Cloudinary API.
     */
    @Operation(summary = "Lấy chữ ký số Signed Upload", description = "Dùng khi Frontend muốn upload trực tiếp file video lớn từ trình duyệt lên Cloudinary")
    @GetMapping("/signature")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<MediaSignature>> getUploadSignature(
            @RequestParam(required = false, defaultValue = "beeacademy/courses/videos") String folder,
            @RequestParam(required = false, defaultValue = "video") String resourceType
    ) {
        MediaSignature signature = uploadMediaUseCase.getUploadSignature(folder, resourceType);
        return ResponseEntity.ok(ApiResponse.success(signature));
    }
}
