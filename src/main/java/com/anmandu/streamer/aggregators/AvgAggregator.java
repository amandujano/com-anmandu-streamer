package com.anmandu.streamer.aggregators;

import java.util.Optional;

import com.anmandu.streamer.dto.AggregationResult;
import com.anmandu.streamer.dto.Input;

public class AvgAggregator implements IAggregator{

  private double totalSum = 0d;
  private long totalCount = 0;

  @Override
  public synchronized Optional<AggregationResult> aggregate(Input data) {
    this.totalSum += data.value();
    this.totalCount++;
    return Optional.of(new AggregationResult(null, null, totalCount, (this.totalSum / this.totalCount)));
  }

  public synchronized double getAverage() {
    return this.totalCount == 0 ? 0 : this.totalSum / this.totalCount;
  }

  public synchronized long getCounter() {
    return this.totalCount;
  }
}
