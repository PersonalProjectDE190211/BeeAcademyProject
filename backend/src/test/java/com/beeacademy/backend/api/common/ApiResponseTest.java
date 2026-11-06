package com.beeacademy.backend.api.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    @DisplayName("ApiResponse tạo envelope thành công chuẩn")
    void testApiResponseEnvelope() {
        ApiResponse<String> response = ApiResponse.success("Dữ liệu thử nghiệm");
        assertThat(response.success()).isTrue();
        assertThat(response.data()).isEqualTo("Dữ liệu thử nghiệm");
        assertThat(response.timestamp()).isNotNull();

        ApiResponse<Void> okResponse = ApiResponse.ok("Xóa thành công");
        assertThat(okResponse.success()).isTrue();
        assertThat(okResponse.message()).isEqualTo("Xóa thành công");
        assertThat(okResponse.data()).isNull();
    }

    @Test
    @DisplayName("ApiErrorResponse tạo payload lỗi kèm danh sách trường vi phạm")
    void testApiErrorResponsePayload() {
        List<FieldErrorItem> errors = List.of(
                new FieldErrorItem("email", "Email không đúng định dạng", "sai-email")
        );

        ApiErrorResponse error = ApiErrorResponse.withFieldErrors("VALIDATION_FAILED", "Lỗi dữ liệu", errors);
        assertThat(error.success()).isFalse();
        assertThat(error.code()).isEqualTo("VALIDATION_FAILED");
        assertThat(error.fieldErrors()).hasSize(1);
        assertThat(error.fieldErrors().getFirst().field()).isEqualTo("email");
    }
}

