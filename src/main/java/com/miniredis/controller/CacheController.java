package com.miniredis.controller;

import com.miniredis.dto.SetRequest;
import com.miniredis.engine.Cache;
import com.miniredis.service.CacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheService cacheService;

    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @PutMapping("/{key}")
    public ResponseEntity<Void> set(@PathVariable String key, @RequestBody SetRequest request) {
        cacheService.set(key, request.value(), request.ttlMillis());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{key}")
    public ResponseEntity<Object> get(@PathVariable String key) {
        Object value = cacheService.get(key);
        return value == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(value);
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        boolean removed = cacheService.delete(key);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{key}/ttl")
    public ResponseEntity<Long> ttl(@PathVariable String key) {
        Long ttl = cacheService.ttl(key);
        return ttl == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(ttl);
    }

    @GetMapping("/stats")
    public ResponseEntity<Cache.CacheStats> stats() {
        return ResponseEntity.ok(cacheService.stats());
    }
}