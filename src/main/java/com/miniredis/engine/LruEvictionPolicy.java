package com.miniredis.engine;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruEvictionPolicy<K> implements EvictionPolicy<K> {

    private final LinkedHashMap<K, Boolean> recencyOrder = new LinkedHashMap<>(13, 0.75f, true);

    @Override
    public void onAccess(K key) {
        recencyOrder.get(key);
    }

    @Override
    public void onInsert(K key) {
        recencyOrder.put(key, Boolean.TRUE);
    }

    @Override
    public void onRemove(K key) {
        recencyOrder.remove(key);
    }

    @Override
    public K evictionCandidate() {
        Map.Entry<K, Boolean> oldest = recencyOrder.entrySet().stream().findFirst().orElse(null);
        return oldest == null ? null : oldest.getKey();
    }

}
