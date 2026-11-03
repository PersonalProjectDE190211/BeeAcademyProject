package com.beeacademy.backend.domain.auth;

import com.beeacademy.backend.domain.common.DomainException;

/**
 * Trạng thái thẩm định tài khoản giáo viên:
 * - NONE: Áp dụng cho các vai trò khác (Student, Parent, Admin)
 * - PENDING: Giáo viên tự đăng ký, chờ Admin xét duyệt
 * - APPROVED: Đã được Admin duyệt hoặc được Admin trực tiếp cấp tài khoản
 * - REJECTED: Bị Admin từ chối phê duyệt
 */
public enum TeacherApprovalStatus {
    NONE("NONE"),
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED");

    private final String dbValue;

    TeacherApprovalStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public boolean isApproved() {
        return this == APPROVED;
    }

    public static TeacherApprovalStatus fromDbValue(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        for (TeacherApprovalStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value.trim()) || status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new DomainException("UNKNOWN_TEACHER_APPROVAL_STATUS", "Trạng thái duyệt giáo viên không hợp lệ: " + value);
    }
}

