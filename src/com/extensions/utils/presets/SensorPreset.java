package com.extensions.utils.presets;

import org.fog.utils.distribution.DeterministicDistribution;
import org.fog.utils.distribution.Distribution;

public enum SensorPreset {
    DEFAULT(
            0.0,
            new DeterministicDistribution(5)
    );

    public final double LATENCY;
    public final Distribution DISTRIBUTION;

    SensorPreset(double latency, Distribution distribution) {
        this.LATENCY = latency;
        this.DISTRIBUTION = distribution;
    }
}
