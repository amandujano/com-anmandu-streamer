package com.anmandu.streamer.aggregators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.anmandu.streamer.dto.AggregationResult;
import com.anmandu.streamer.dto.Input;

class AvgAggregatorTest {

    private static final Instant T0 = Instant.parse("2026-09-24T15:00:00Z");

    private static Input input(String sensor, double value, Instant ts) {
        return new Input(sensor, value, ts);
    }

    @Test
    void emitsRunningAverageForEveryItem() {
        AvgAggregator aggregator = new AvgAggregator();

        Optional<AggregationResult> first = aggregator.aggregate(input("A", 10, T0));
        Optional<AggregationResult> second = aggregator.aggregate(input("A", 20, T0.plusSeconds(1)));
        Optional<AggregationResult> third = aggregator.aggregate(input("A", 30, T0.plusSeconds(2)));

        assertTrue(first.isPresent());
        assertTrue(second.isPresent());
        assertTrue(third.isPresent());
        assertEquals(10, first.get().average(), 1e-9);
        assertEquals(15, second.get().average(), 1e-9);
        assertEquals(20, third.get().average(), 1e-9);
        assertEquals(1, first.get().count());
        assertEquals(2, second.get().count());
        assertEquals(3, third.get().count());
    }

    @Test
    void resultIsNotTiedToSensorOrWindow() {
        AvgAggregator aggregator = new AvgAggregator();

        AggregationResult result = aggregator.aggregate(input("A", 10, T0)).orElseThrow();

        assertNull(result.sensor());
        assertNull(result.windowStart());
    }
}
