package com.beeacademy.backend.api.common;

import com.beeacademy.backend.domain.common.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.ArrayList;
import java.util.List;

/**
 * Xử lý ngoại lệ tập trung (Global Exception Handler) cho toàn bộ hệ thống.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainException(DomainException ex) {
        log.warn("Ngoại lệ nghiệp vụ DomainException: [{}] {}", ex.getErrorCode(), ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if ("NOT_FOUND".equalsIgnoreCase(ex.getErrorCode()) || ex.getErrorCode().endsWith("_NOT_FOUND")) {
            status = HttpStatus.NOT_FOUND;
        } else if ("UNAUTHORIZED".equalsIgnoreCase(ex.getErrorCode()) || "FORBIDDEN".equalsIgnoreCase(ex.getErrorCode())) {
            status = HttpStatus.FORBIDDEN;
        }
        return ResponseEntity.status(status).body(ApiErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldErrorItem> fieldErrors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(new FieldErrorItem(error.getField(), error.getDefaultMessage(), error.getRejectedValue()));
        }
        log.warn("Lỗi xác thực dữ liệu đầu vào (Validation): {} lỗi trường", fieldErrors.size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.withFieldErrors("VALIDATION_FAILED", "Dữ liệu gửi lên không hợp lệ", fieldErrors));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Đăng nhập thất bại: Thông tin xác thực không chính xác");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of("INVALID_CREDENTIALS", "Email hoặc mật khẩu không chính xác"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Truy cập bị từ chối: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of("ACCESS_DENIED", "Bạn không có quyền thực hiện hành động này"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("File tải lên vượt quá dung lượng cho phép");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiErrorResponse.of("FILE_TOO_LARGE", "Dung lượng file tải lên vượt quá giới hạn tối đa của hệ thống"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex) {
        log.error("Lỗi hệ thống chưa được phân loại (Unhandled Exception): ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of("INTERNAL_SERVER_ERROR", "Đã xảy ra lỗi nội bộ máy chủ. Vui lòng thử lại sau."));
    }
}

