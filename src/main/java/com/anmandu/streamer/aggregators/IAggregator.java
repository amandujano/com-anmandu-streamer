package com.anmandu.streamer.aggregators;

import java.util.Optional;

import com.anmandu.streamer.dto.AggregationResult;
import com.anmandu.streamer.dto.Input;

public interface IAggregator {
  Optional<AggregationResult> aggregate(Input measurement);
}
