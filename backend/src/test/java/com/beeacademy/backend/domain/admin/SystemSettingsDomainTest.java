package com.beeacademy.backend.domain.admin;

import com.beeacademy.backend.domain.common.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Tầng Domain Admin (SystemSettings Aggregate Root)")
class SystemSettingsDomainTest {

    @Test
    @DisplayName("Cấu hình bảo trì hệ thống: Khởi tạo, cập nhật giá trị và chuyển đổi boolean")
    void testSystemSettingsMaintenance() {
        SystemSettings setting = SystemSettings.of(SystemSettings.KEY_MAINTENANCE_MODE, "true");

        assertEquals(SystemSettings.KEY_MAINTENANCE_MODE, setting.getKey());
        assertEquals("true", setting.getValue());
        assertTrue(setting.getAsBoolean(false));

        // Tắt bảo trì
        setting.updateValue("false");
        assertEquals("false", setting.getValue());
        assertFalse(setting.getAsBoolean(true));

        // Khóa null hoặc rỗng phải ném DomainException
        assertThrows(DomainException.class, () -> SystemSettings.of("   ", "val"));
    }
}

