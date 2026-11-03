package com.beeacademy.backend.domain.learning;

import com.beeacademy.backend.domain.common.DomainException;

/**
 * Khoảng thời gian học viên đã xem video bài học [startSec, endSec].
 */
public record WatchSegment(int startSec, int endSec) implements Comparable<WatchSegment> {

    public WatchSegment {
        if (startSec < 0) {
            throw new DomainException("INVALID_START_SEC", "Thời điểm bắt đầu không được âm: " + startSec);
        }
        if (endSec < startSec) {
            throw new DomainException("INVALID_END_SEC", "Thời điểm kết thúc (" + endSec + ") phải >= bắt đầu (" + startSec + ")");
        }
    }

    public int duration() {
        return endSec - startSec;
    }

    public boolean overlapsOrTouches(WatchSegment other) {
        return this.startSec <= other.endSec && other.startSec <= this.endSec;
    }

    public WatchSegment mergeWith(WatchSegment other) {
        return new WatchSegment(
                Math.min(this.startSec, other.startSec),
                Math.max(this.endSec, other.endSec)
        );
    }

    @Override
    public int compareTo(WatchSegment o) {
        int cmp = Integer.compare(this.startSec, o.startSec);
        return cmp != 0 ? cmp : Integer.compare(this.endSec, o.endSec);
    }
}
