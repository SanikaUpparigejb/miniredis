package com.miniredis.dto;

public record SetRequest(Object value, Long ttlMillis) {}
