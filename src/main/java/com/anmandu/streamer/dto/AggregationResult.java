package com.anmandu.streamer.dto;

import java.time.Instant;

public record AggregationResult(String sensor, Instant windowStart, long count, double average) {}