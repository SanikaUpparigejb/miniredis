package com.miniredis.service;

import com.miniredis.engine.Cache;
import com.miniredis.engine.LruEvictionPolicy;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private final Cache<String, Object> cache;

    public CacheService(@Value("${miniredis.capacity:1000}") int capacity,
                        @Value("${miniredis.expiry-sweep-interval-ms:1000}") long sweepIntervalMillis) {
        this.cache = new Cache<>(capacity, new LruEvictionPolicy<>(), sweepIntervalMillis);
    }

    public void set(String key, Object value, Long ttlMillis) {
        cache.set(key, value, ttlMillis);
    }

    public Object get(String key) {
        return cache.get(key);
    }

    public boolean delete(String key) {
        return cache.delete(key);
    }

    public Long ttl(String key) {
        return cache.ttlMillis(key);
    }

    public Cache.CacheStats stats() {
        return cache.stats();
    }

    @PreDestroy
    public void onShutdown() {
        cache.shutdown();
    }
}
