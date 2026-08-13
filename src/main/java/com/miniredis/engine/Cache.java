package com.miniredis.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Cache<K, V> {

    private final Map<K, Entry<V>> store = new ConcurrentHashMap<>();
    private final EvictionPolicy<K> evictionPolicy;
    private final int capacity;
    private final ReentrantLock lock =  new ReentrantLock();
    private final ScheduledExecutorService expiryScheduler = Executors.newSingleThreadScheduledExecutor(r->{
        Thread t = new Thread(r, "cache-expiry-sweeper");
        t.setDaemon(true);
        return t;
    });

    private long hitCount = 0;
    private long missCount = 0;
    private long evictionCount = 0;

    public Cache(int capacity, EvictionPolicy<K> evictionPolicy, long sweepIntervalMillis) {
        this.capacity = capacity;
        this.evictionPolicy = evictionPolicy;
        this.expiryScheduler.scheduleAtFixedRate(
                this::activeExpirySweep, sweepIntervalMillis, sweepIntervalMillis, TimeUnit.MILLISECONDS);
    }

    public void set(K key, V value, Long ttlMillis) {
        lock.lock();
        try{
            boolean isNewKey = !store.containsKey(key);
            if(isNewKey && store.size() >= capacity) {
                evictOne();
            }
            store.put(key, new Entry<>(value, ttlMillis));
            evictionPolicy.onInsert(key);
        } finally {
            lock.unlock();
        }
    }

    public V get(K key) {
        lock.lock();
        try{
            Entry<V> entry = store.get(key);
            if(entry == null) {
                missCount++;
                return null;
            }
            if(entry.isExpired()) {
                store.remove(key);
                evictionPolicy.onRemove(key);
                missCount++;
                return null;
            }
            entry.recordAccess();
            evictionPolicy.onAccess(key);
            hitCount++;
            return entry.getValue();
        } finally {
            lock.unlock();
        }
    }

    public boolean delete(K key) {
        lock.lock();
        try {
            Entry<V> removed = store.remove(key);
            evictionPolicy.onRemove(key);
            return removed != null;
        } finally {
            lock.unlock();
        }
    }


    public Long ttlMillis(K key) {
        lock.lock();
        try {
            Entry<V> entry = store.get(key);
            if (entry == null || entry.isExpired()) return null;
            Long expiresAt = entry.getExpiresAtEpochMillis();
            return expiresAt == null ? -1L : expiresAt - System.currentTimeMillis();
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return store.size();
        } finally {
            lock.unlock();
        }
    }

    public CacheStats stats() {
        lock.lock();
        try {
            return new CacheStats(store.size(), capacity, hitCount, missCount, evictionCount);
        } finally {
            lock.unlock();
        }
    }

    private void evictOne() {
        K victim = evictionPolicy.evictionCandidate();
        if (victim != null) {
            store.remove(victim);
            evictionPolicy.onRemove(victim);
            evictionCount++;
        }
    }

    private void activeExpirySweep() {
        lock.lock();
        try {
            store.entrySet().removeIf(e -> {
                boolean expired = e.getValue().isExpired();
                if (expired) evictionPolicy.onRemove(e.getKey());
                return expired;
            });
        } finally {
            lock.unlock();
        }
    }

    public void shutdown() {
        expiryScheduler.shutdownNow();
    }

    public record CacheStats(int size, int capacity, long hitCount, long missCount, long evictionCount) {}


}



