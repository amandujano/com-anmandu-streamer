package com.anmandu.streamer.aggregators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.anmandu.streamer.dto.AggregationResult;
import com.anmandu.streamer.dto.Input;

class PartitionTimeWindowAvgAggregatorTest {

    private static final Instant T0 = Instant.parse("2026-09-24T15:00:00Z");
    private static final Duration FOUR_SECONDS = Duration.ofSeconds(4);

    private static Input input(String sensor, double value, Instant ts) {
        return new Input(1, sensor, value, ts);
    }

    @Test
    void tagsClosedWindowWithSensor() {
        PartitionTimeWindowAvgAggregator aggregator = new PartitionTimeWindowAvgAggregator(FOUR_SECONDS);

        assertTrue(aggregator.aggregate(input("A", 10, T0)).isEmpty());
        assertTrue(aggregator.aggregate(input("A", 20, T0.plusSeconds(1))).isEmpty());
        Optional<AggregationResult> result = aggregator.aggregate(input("A", 99, T0.plusSeconds(4)));

        assertTrue(result.isPresent());
        assertEquals("A", result.get().sensor());
        assertEquals(T0, result.get().windowStart());
        assertEquals(2, result.get().count());
        assertEquals(15, result.get().average(), 1e-9);
    }

    @Test
    void keepsPartitionsIndependent() {
        PartitionTimeWindowAvgAggregator aggregator = new PartitionTimeWindowAvgAggregator(FOUR_SECONDS);

        assertTrue(aggregator.aggregate(input("A", 10, T0)).isEmpty());
        assertTrue(aggregator.aggregate(input("B", 20, T0.plusSeconds(5))).isEmpty());
        assertTrue(aggregator.aggregate(input("A", 30, T0.plusSeconds(3))).isEmpty());
        Optional<AggregationResult> result = aggregator.aggregate(input("B", 40, T0.plusSeconds(9)));

        assertTrue(result.isPresent());
        assertEquals("B", result.get().sensor());
        assertEquals(T0.plusSeconds(5), result.get().windowStart());
        assertEquals(1, result.get().count());
        assertEquals(20, result.get().average(), 1e-9);
    }

    @Test
    void interleavedSensorsCloseOwnWindows() {
        PartitionTimeWindowAvgAggregator aggregator = new PartitionTimeWindowAvgAggregator(FOUR_SECONDS);

        assertTrue(aggregator.aggregate(input("A", 1, T0)).isEmpty());
        assertTrue(aggregator.aggregate(input("B", 100, T0.plusSeconds(1))).isEmpty());
        assertTrue(aggregator.aggregate(input("A", 3, T0.plusSeconds(2))).isEmpty());
        assertTrue(aggregator.aggregate(input("B", 300, T0.plusSeconds(3))).isEmpty());

        AggregationResult closedA = aggregator.aggregate(input("A", 0, T0.plusSeconds(4))).orElseThrow();
        assertEquals("A", closedA.sensor());
        assertEquals(T0, closedA.windowStart());
        assertEquals(2, closedA.count());
        assertEquals(2, closedA.average(), 1e-9);

        AggregationResult closedB = aggregator.aggregate(input("B", 0, T0.plusSeconds(5))).orElseThrow();
        assertEquals("B", closedB.sensor());
        assertEquals(T0.plusSeconds(1), closedB.windowStart());
        assertEquals(2, closedB.count());
        assertEquals(200, closedB.average(), 1e-9);
    }
}
