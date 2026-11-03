package com.beeacademy.backend.domain.common;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Value Object biểu diễn tiền tệ Việt Nam Đồng (VND).
 * Đảm bảo tính bất biến (Immutable) và toàn vẹn của các phép toán tài chính:
 * - Số tiền luôn là số nguyên không âm (>= 0).
 * - Đóng gói phép trừ giảm giá an toàn (không âm).
 * - Đóng gói thuật toán phân chia doanh thu (Revenue Split: 80% Giáo viên / 20% Nền tảng).
 */
public record MoneyVnd(int amount) implements Serializable, Comparable<MoneyVnd> {

    public static final MoneyVnd ZERO = new MoneyVnd(0);

    public MoneyVnd {
        if (amount < 0) {
            throw new DomainException("INVALID_MONEY_AMOUNT", "Số tiền không được âm: " + amount);
        }
    }

    public static MoneyVnd of(Integer amount) {
        if (amount == null) {
            return ZERO;
        }
        return new MoneyVnd(amount);
    }

    public MoneyVnd plus(MoneyVnd other) {
        if (other == null) return this;
        return new MoneyVnd(Math.addExact(this.amount, other.amount));
    }

    public MoneyVnd minusOrZero(MoneyVnd other) {
        if (other == null) return this;
        int diff = this.amount - other.amount;
        return new MoneyVnd(Math.max(0, diff));
    }

    /**
     * Tính số tiền chiết khấu theo % (từ 0 đến 100).
     */
    public MoneyVnd applyDiscountPercent(int percent) {
        if (percent <= 0) return ZERO;
        if (percent >= 100) return this;
        long discount = Math.round(((double) this.amount * percent) / 100.0);
        return new MoneyVnd((int) discount);
    }

    /**
     * Tính phí nền tảng theo tỷ lệ % (mặc định 20%).
     */
    public MoneyVnd calculatePlatformFee(int platformFeePercent) {
        if (platformFeePercent <= 0) return ZERO;
        if (platformFeePercent >= 100) return this;
        long fee = Math.round(((double) this.amount * platformFeePercent) / 100.0);
        return new MoneyVnd((int) fee);
    }

    /**
     * Tính phần doanh thu thực nhận của giáo viên (tổng tiền trừ đi phí nền tảng).
     */
    public MoneyVnd calculateTeacherShare(int platformFeePercent) {
        MoneyVnd fee = calculatePlatformFee(platformFeePercent);
        return this.minusOrZero(fee);
    }

    public boolean isZero() {
        return this.amount == 0;
    }

    public boolean isGreaterThan(MoneyVnd other) {
        return other != null && this.amount > other.amount;
    }

    @Override
    public int compareTo(MoneyVnd o) {
        if (o == null) return 1;
        return Integer.compare(this.amount, o.amount);
    }

    @Override
    public String toString() {
        NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY); // định dạng phân cách 100.000 đ
        return nf.format(this.amount) + " đ";
    }
}

