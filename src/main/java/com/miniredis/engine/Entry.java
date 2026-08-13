package com.miniredis.engine;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class Entry<V> {

    private final V value;
    private final Long expiresAtEpochMillis;
    private volatile long lastAccessedAtEpochMillis;
    private final AtomicLong accessCount = new AtomicLong(0);

    public Entry(V value, Long ttlMillis) {
        this.value = value;
        this.expiresAtEpochMillis = (ttlMillis == null) ? null : Instant.now().toEpochMilli() + ttlMillis;
        this.lastAccessedAtEpochMillis = Instant.now().toEpochMilli();
    }

    public V getValue() {
        return value;
    }

    public boolean isExpired() {
        return expiresAtEpochMillis != null && Instant.now().toEpochMilli() > expiresAtEpochMillis;
    }

    public Long getExpiresAtEpochMillis() {
        return expiresAtEpochMillis;
    }

    public void recordAccess() {
        this.lastAccessedAtEpochMillis = Instant.now().toEpochMilli();
        this.accessCount.incrementAndGet();
    }

    public long getLastAccessedAtEpochMillis() {
        return lastAccessedAtEpochMillis;
    }

    public long getAccessCount() {
        return accessCount.get();
    }
}
