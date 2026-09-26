package com.anmandu.streamer.aggregators;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import com.anmandu.streamer.dto.AggregationResult;
import com.anmandu.streamer.dto.Input;

public class TimeWindowAvgAggregator  implements IAggregator{
    private double totalSum;
    private long windowCounter;
    private Instant start;
    final private Duration duration;

    public TimeWindowAvgAggregator(Duration duration){
        if (duration.equals(null))
            throw new IllegalArgumentException("window size must be positive");
      
        this.totalSum = 0d;
        this.windowCounter = 0;
        this.start = null;
        this.duration = duration;
    }

    @Override
    public synchronized Optional<AggregationResult> aggregate(Input measurement) {
        if (start == null) {
            start = measurement.timestamp();
        }
        
        if(!this.insideWindow(measurement.timestamp())){
            AggregationResult closed = new AggregationResult(null, start, windowCounter, getAverage());
            openNewWindow(measurement);
            return Optional.of(closed);
        }

        this.totalSum += measurement.value();
        this.windowCounter++;
        return Optional.empty();
    }

    public double getAverage(){
        return this.windowCounter == 0 ? 0 : this.totalSum / this.windowCounter;
    }

    public long getWindowCounter(){
        return this.windowCounter;
    }

    public Instant getStart(){
        return this.start;
    }

    private boolean insideWindow(Instant timestamp){
        return Duration.between(start, timestamp).compareTo(duration) < 0;
    }

    private void openNewWindow(Input data){
        this.totalSum = data.value();
        this.windowCounter = 1;
        this.start = data.timestamp();
    }

}
