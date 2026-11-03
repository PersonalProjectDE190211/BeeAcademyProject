package com.beeacademy.backend.domain.learning;

import com.beeacademy.backend.domain.common.ScorePercent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value Object quản lý tập hợp các đoạn xem video của học viên (UC14).
 * Tự động gộp (merge) các đoạn chồng lấp hoặc liền kề để tính chính xác tổng số giây thực học,
 * ngăn chặn việc xem đi xem lại một đoạn ngắn hoặc tua nhanh để gian lận tiến độ.
 */
public class WatchSegments {

    private final List<WatchSegment> segments;

    public WatchSegments() {
        this.segments = new ArrayList<>();
    }

    public WatchSegments(List<WatchSegment> segments) {
        this.segments = new ArrayList<>();
        if (segments != null) {
            for (WatchSegment segment : segments) {
                addSegment(segment.startSec(), segment.endSec());
            }
        }
    }

    public static WatchSegments empty() {
        return new WatchSegments();
    }

    /**
     * Thêm một đoạn xem mới và tự động gộp với các đoạn hiện có.
     */
    public synchronized void addSegment(int startSec, int endSec) {
        WatchSegment newSeg = new WatchSegment(startSec, endSec);
        if (newSeg.duration() == 0) {
            return;
        }

        List<WatchSegment> merged = new ArrayList<>();
        WatchSegment current = newSeg;

        for (WatchSegment existing : this.segments) {
            if (current.overlapsOrTouches(existing)) {
                current = current.mergeWith(existing);
            } else {
                merged.add(existing);
            }
        }
        merged.add(current);
        Collections.sort(merged);

        this.segments.clear();
        this.segments.addAll(merged);
    }

    /**
     * Tính tổng số giây thực xem (không trùng lặp).
     */
    public int calculateTotalUniqueSeconds() {
        return this.segments.stream()
                .mapToInt(WatchSegment::duration)
                .sum();
    }

    /**
     * Tính tỷ lệ phần trăm đã xem so với tổng thời lượng bài học (thang điểm 0.0 - 100.0).
     */
    public ScorePercent calculateWatchPercent(int totalLessonDurationSec) {
        if (totalLessonDurationSec <= 0) {
            return ScorePercent.of(100.0);
        }
        int uniqueSecs = calculateTotalUniqueSeconds();
        int cappedSecs = Math.min(uniqueSecs, totalLessonDurationSec);
        return ScorePercent.fromRatio(cappedSecs, totalLessonDurationSec);
    }

    /**
     * Kiểm tra xem học viên đã xem đủ ngưỡng hoàn thành hay chưa (mặc định 80% theo UC14).
     */
    public boolean hasReachedCompletionThreshold(int totalLessonDurationSec, double thresholdRatio) {
        if (totalLessonDurationSec <= 0) {
            return true;
        }
        int requiredSecs = (int) Math.ceil(totalLessonDurationSec * thresholdRatio);
        return calculateTotalUniqueSeconds() >= requiredSecs;
    }

    public List<WatchSegment> getSegments() {
        return Collections.unmodifiableList(segments);
    }
}
