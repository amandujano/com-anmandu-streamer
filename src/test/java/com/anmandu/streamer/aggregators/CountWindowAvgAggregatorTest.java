package com.anmandu.streamer.aggregators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.anmandu.streamer.dto.AggregationResult;
import com.anmandu.streamer.dto.Input;

class CountWindowAvgAggregatorTest {

    private static final Instant T0 = Instant.parse("2026-09-24T15:00:00Z");

    private static Input input(String sensor, double value, Instant ts) {
        return new Input( sensor, value, ts);
    }

    @Test
    void rejectsNonPositiveWindowSize() {
        assertThrows(IllegalArgumentException.class, () -> new CountWindowAvgAggregator(0));
        assertThrows(IllegalArgumentException.class, () -> new CountWindowAvgAggregator(-1));
    }

    @Test
    void emitsNothingUntilWindowIsFull() {
        CountWindowAvgAggregator aggregator = new CountWindowAvgAggregator(3);

        assertTrue(aggregator.aggregate(input("A", 2, T0)).isEmpty());
        assertTrue(aggregator.aggregate(input("A", 4, T0.plusSeconds(1))).isEmpty());
    }

    @Test
    void emitsAverageWhenWindowIsFull() {
        CountWindowAvgAggregator aggregator = new CountWindowAvgAggregator(3);

        aggregator.aggregate(input("A", 2, T0));
        aggregator.aggregate(input("A", 4, T0.plusSeconds(1)));
        Optional<AggregationResult> result = aggregator.aggregate(input("A", 6, T0.plusSeconds(2)));

        assertTrue(result.isPresent());
        assertEquals(3, result.get().count());
        assertEquals(4, result.get().average(), 1e-9);
    }

    @Test
    void startsFreshWindowAfterEmitting() {
        CountWindowAvgAggregator aggregator = new CountWindowAvgAggregator(3);

        aggregator.aggregate(input("A", 2, T0));
        aggregator.aggregate(input("A", 4, T0.plusSeconds(1)));
        assertTrue(aggregator.aggregate(input("A", 6, T0.plusSeconds(2))).isPresent());

        assertTrue(aggregator.aggregate(input("A", 10, T0.plusSeconds(3))).isEmpty());
        assertTrue(aggregator.aggregate(input("A", 20, T0.plusSeconds(4))).isEmpty());
        Optional<AggregationResult> second = aggregator.aggregate(input("A", 30, T0.plusSeconds(5)));

        assertTrue(second.isPresent());
        assertEquals(3, second.get().count());
        assertEquals(20, second.get().average(), 1e-9);
    }

    @Test
    void windowOfOneEmitsEveryItem() {
        CountWindowAvgAggregator aggregator = new CountWindowAvgAggregator(1);

        AggregationResult first = aggregator.aggregate(input("A", 5, T0)).orElseThrow();
        AggregationResult second = aggregator.aggregate(input("A", 7, T0.plusSeconds(1))).orElseThrow();

        assertEquals(1, first.count());
        assertEquals(5, first.average(), 1e-9);
        assertEquals(1, second.count());
        assertEquals(7, second.average(), 1e-9);
    }
}
