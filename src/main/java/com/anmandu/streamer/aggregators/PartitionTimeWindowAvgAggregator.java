package com.anmandu.streamer.aggregators;

import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;

import com.anmandu.streamer.dto.AggregationResult;
import com.anmandu.streamer.dto.Input;

public class PartitionTimeWindowAvgAggregator implements IAggregator{   
    private HashMap<String, TimeWindowAvgAggregator> partitions = new HashMap<>();
    final private Duration duration;

    public PartitionTimeWindowAvgAggregator(Duration duration){
        if (duration.equals(null))
            throw new IllegalArgumentException("window size must be positive");

        this.duration = duration;
    }

    @Override
    public synchronized Optional<AggregationResult> aggregate(Input data) {
        String sensor = data.sensor();
        return partitions.computeIfAbsent(sensor, s -> new TimeWindowAvgAggregator(duration))
            .aggregate(data)
            .map(r -> new AggregationResult(sensor, r.windowStart(), r.count(), r.average()));
    }
}
