package com.beeacademy.backend.domain.common;

/**
 * Exception nghiệp vụ nền tảng trong tầng Domain.
 * Được ném ra khi có bất kỳ quy tắc hoặc điều kiện tiên quyết (Invariants / Pre-conditions) nào bị vi phạm.
 */
public class DomainException extends RuntimeException {

    private final String errorCode;

    public DomainException(String message) {
        super(message);
        this.errorCode = "DOMAIN_RULE_VIOLATION";
    }

    public DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

