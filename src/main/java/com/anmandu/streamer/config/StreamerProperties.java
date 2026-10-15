package com.anmandu.streamer.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "streamer")
public record StreamerProperties(List<Sensor> sensors) {

  public StreamerProperties {
    if(sensors == null || sensors.isEmpty()){
      throw new IllegalArgumentException("sensors array must contain at least one element");
    }
  }

  public record Sensor(String name, Duration period) {
    public Sensor {
      if(name == null || name.isBlank()){
        throw new IllegalArgumentException("sensor name required");
      }
      if (period == null || period.isNegative() || period.isZero()) {
        throw new IllegalArgumentException("period required");
      }
    }
  }
}
