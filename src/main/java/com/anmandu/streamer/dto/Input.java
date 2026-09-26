package com.anmandu.streamer.dto;

import java.time.Instant;

public record Input(long id, String sensor, double value, Instant timestamp) {}
