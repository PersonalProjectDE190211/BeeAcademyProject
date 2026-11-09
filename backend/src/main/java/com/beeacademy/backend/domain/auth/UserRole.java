package com.beeacademy.backend.domain.auth;

import com.beeacademy.backend.domain.common.DomainException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Vai trò người dùng trong hệ sinh thái Bee Academy.
 * Ánh xạ với native enum Postgres 'user_role' (student, teacher, parent, admin).
 */
public enum UserRole {
    STUDENT("student"),
    TEACHER("teacher"),
    PARENT("parent"),
    ADMIN("admin");

    private final String dbValue;

    UserRole(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonValue
    public String getDbValue() {
        return dbValue;
    }

    public boolean isStudent() {
        return this == STUDENT;
    }

    public boolean isTeacher() {
        return this == TEACHER;
    }

    public boolean isParent() {
        return this == PARENT;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    @JsonCreator
    public static UserRole fromDbValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (UserRole role : values()) {
            if (role.dbValue.equalsIgnoreCase(value.trim()) || role.name().equalsIgnoreCase(value.trim())) {
                return role;
            }
        }
        throw new DomainException("UNKNOWN_ROLE", "Không nhận diện được vai trò: " + value);
    }
}

