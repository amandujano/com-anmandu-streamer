package com.anmandu.streamer.dto;

import java.time.Instant;

public record Input(String sensor, double value, Instant timestamp) {}
