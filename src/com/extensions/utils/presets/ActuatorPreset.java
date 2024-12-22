package com.extensions.utils.presets;

import org.fog.utils.distribution.DeterministicDistribution;
import org.fog.utils.distribution.Distribution;

public enum ActuatorPreset {
    DEFAULT(
            0.0
    );

    public final double LATENCY;

    ActuatorPreset(double latency) {
        this.LATENCY = latency;
    }
}
