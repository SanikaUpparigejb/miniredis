package com.miniredis.engine;

public interface EvictionPolicy<K> {

    void onAccess(K key);

    void onInsert(K key);

    void onRemove(K key);


    /**
     * @return the key that should be evicted next, or null if there's nothing to evict.
     */
   K evictionCandidate();
}

