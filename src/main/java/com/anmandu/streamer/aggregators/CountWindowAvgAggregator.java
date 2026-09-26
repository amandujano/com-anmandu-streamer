package com.anmandu.streamer.aggregators;

import java.util.Optional;

import com.anmandu.streamer.dto.AggregationResult;
import com.anmandu.streamer.dto.Input;

public class CountWindowAvgAggregator implements IAggregator{

  private double totalSum;
  private long windowCounter;
  final private long windowSize;

  public CountWindowAvgAggregator(long size){
    if (size <= 0)
      throw new IllegalArgumentException("window size must be positive");

    this.windowSize = size;
    this.windowCounter = 0;
    this.totalSum = 0d;
  }

  @Override
  public synchronized Optional<AggregationResult> aggregate(Input measurement) {
    this.totalSum += measurement.value();
    this.windowCounter++;

    if (windowCounter < windowSize) { 
      return Optional.empty();
    }
      
    AggregationResult closed = new AggregationResult(null, null, windowCounter, getAverage());
    reset();
    return Optional.of(closed);
  }

  public double getAverage() {
    return this.windowCounter == 0 ? 0 : this.totalSum / this.windowCounter;
  }

  public long getWindowCounter() {
    return this.windowCounter;
  }

  public long getWindowSize(){
    return this.windowSize;
  }

  private void reset() {
    this.totalSum = 0d;
    this.windowCounter = 0;
  }

}
