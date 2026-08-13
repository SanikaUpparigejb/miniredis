package com.miniredis.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

 class CacheTest {

     @Test
     void setAndGet_returnsStoredValue() {
         Cache<String, String> cache = new Cache<>(10, new LruEvictionPolicy<>(), 10_000);
         cache.set("name", "sandy", null);
         assertEquals("sandy", cache.get("name"));
         cache.shutdown();
     }

     @Test
     void get_missingKey_returnsNull() {
         Cache<String, String> cache = new Cache<>(10, new LruEvictionPolicy<>(), 10_000);
         assertNull(cache.get("nope"));
         cache.shutdown();
     }


     @Test
     void get_afterTtlExpires_returnsNull() throws InterruptedException {
         Cache<String, String> cache = new Cache<>(10, new LruEvictionPolicy<>(), 10_000);
         cache.set("temp", "value", 50L);
         Thread.sleep(100);
         assertNull(cache.get("temp"));
         cache.shutdown();
     }

     @Test
     void activeSweep_removesExpiredKeyWithoutGet() throws InterruptedException {
         Cache<String, String> cache = new Cache<>(10, new LruEvictionPolicy<>(), 50L);
         cache.set("temp", "value", 30L);
         Thread.sleep(150);
         assertEquals(0, cache.size());
         cache.shutdown();
     }

     @Test
     void whenCapacityExceeded_evictsLeastRecentlyUsed() {
         Cache<String, String> cache = new Cache<>(2, new LruEvictionPolicy<>(), 10_000);
         cache.set("a", "1", null);
         cache.set("b", "2", null);
         cache.get("a");            // "a" is now most-recently-used
         cache.set("c", "3", null); // should evict "b", not "a"

         assertEquals("1", cache.get("a"));
         assertNull(cache.get("b"));
         assertEquals("3", cache.get("c"));
         assertEquals(2, cache.size());
         cache.shutdown();
     }

     @Test
     void stats_trackHitsAndMisses() {
         Cache<String, String> cache = new Cache<>(5, new LruEvictionPolicy<>(), 10_000);
         cache.set("x", "1", null);
         cache.get("x"); // hit
         cache.get("y"); // miss

         Cache.CacheStats stats = cache.stats();
         assertEquals(1, stats.hitCount());
         assertEquals(1, stats.missCount());
         cache.shutdown();
     }
 }
