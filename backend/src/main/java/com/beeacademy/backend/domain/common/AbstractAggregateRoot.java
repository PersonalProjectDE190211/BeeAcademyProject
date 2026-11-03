package com.beeacademy.backend.domain.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lớp trừu tượng cơ sở cho các Aggregate Root trong hệ thống Clean Architecture.
 * Quản lý danh sách Domain Events phát sinh trong quá trình thực thi nghiệp vụ.
 */
public abstract class AbstractAggregateRoot {

    private final transient List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        if (event != null) {
            this.domainEvents.add(event);
        }
    }

    protected void registerDomainEvent(DomainEvent event) {
        registerEvent(event);
    }

    /**
     * Lấy danh sách sự kiện chưa phát tán (unmodifiable view).
     */
    public List<DomainEvent> pollDomainEvents() {
        return Collections.unmodifiableList(this.domainEvents);
    }

    /**
     * Xóa danh sách sự kiện sau khi đã phát tán thành công.
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
