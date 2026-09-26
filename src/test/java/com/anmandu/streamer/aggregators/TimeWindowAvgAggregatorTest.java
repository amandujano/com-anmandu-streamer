package com.anmandu.streamer.aggregators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.anmandu.streamer.dto.AggregationResult;
import com.anmandu.streamer.dto.Input;

class TimeWindowAvgAggregatorTest {

    private static final Instant T0 = Instant.parse("2026-09-24T15:00:00Z");
    private static final Duration FOUR_SECONDS = Duration.ofSeconds(4);

    private static Input input(String sensor, double value, Instant ts) {
        return new Input(1, sensor, value, ts);
    }

    @Test
    void emitsNothingWhileWindowIsOpen() {
        TimeWindowAvgAggregator aggregator = new TimeWindowAvgAggregator(FOUR_SECONDS);

        assertTrue(aggregator.aggregate(input("A", 10, T0)).isEmpty());
        assertTrue(aggregator.aggregate(input("A", 20, T0.plusMillis(3999))).isEmpty());
    }

    @Test
    void closesWindowAtExactDurationBoundary() {
        TimeWindowAvgAggregator aggregator = new TimeWindowAvgAggregator(FOUR_SECONDS);

        aggregator.aggregate(input("A", 10, T0));
        aggregator.aggregate(input("A", 20, T0.plusSeconds(1)));
        Optional<AggregationResult> result = aggregator.aggregate(input("A", 99, T0.plusSeconds(4)));

        assertTrue(result.isPresent());
        assertEquals(T0, result.get().windowStart());
        assertEquals(2, result.get().count());
        assertEquals(15, result.get().average(), 1e-9);
        assertNull(result.get().sensor());
    }

    @Test
    void closingItemOpensNextWindow() {
        TimeWindowAvgAggregator aggregator = new TimeWindowAvgAggregator(FOUR_SECONDS);

        aggregator.aggregate(input("A", 10, T0));
        AggregationResult first = aggregator.aggregate(input("A", 30, T0.plusSeconds(4))).orElseThrow();
        assertTrue(aggregator.aggregate(input("A", 50, T0.plusSeconds(5))).isEmpty());
        AggregationResult second = aggregator.aggregate(input("A", 0, T0.plusSeconds(8))).orElseThrow();

        assertEquals(1, first.count());
        assertEquals(10, first.average(), 1e-9);
        assertEquals(T0.plusSeconds(4), second.windowStart());
        assertEquals(2, second.count());
        assertEquals(40, second.average(), 1e-9);
    }

    @Test
    void supportsSubSecondDurations() {
        TimeWindowAvgAggregator aggregator = new TimeWindowAvgAggregator(Duration.ofMillis(1500));

        assertTrue(aggregator.aggregate(input("A", 10, T0)).isEmpty());
        assertTrue(aggregator.aggregate(input("A", 20, T0.plusMillis(1499))).isEmpty());
        Optional<AggregationResult> result = aggregator.aggregate(input("A", 30, T0.plusMillis(1500)));

        assertTrue(result.isPresent());
        assertEquals(2, result.get().count());
    }

    @Test
    void gapLongerThanWindowStartsNextWindowAtLateItem() {
        TimeWindowAvgAggregator aggregator = new TimeWindowAvgAggregator(FOUR_SECONDS);

        aggregator.aggregate(input("A", 10, T0));
        AggregationResult first = aggregator.aggregate(input("A", 30, T0.plusSeconds(20))).orElseThrow();
        assertEquals(T0, first.windowStart());
        assertEquals(1, first.count());
        assertEquals(10, first.average(), 1e-9);

        assertTrue(aggregator.aggregate(input("A", 50, T0.plusSeconds(23))).isEmpty());

        AggregationResult second = aggregator.aggregate(input("A", 0, T0.plusSeconds(24))).orElseThrow();
        assertEquals(T0.plusSeconds(20), second.windowStart());
        assertEquals(2, second.count());
        assertEquals(40, second.average(), 1e-9);
    }
}
