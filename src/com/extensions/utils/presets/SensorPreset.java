package com.extensions.utils.presets;

import org.fog.utils.distribution.DeterministicDistribution;
import org.fog.utils.distribution.Distribution;

/**
 * Defines presets for sensor configurations in the iFogSim simulation.
 */
public enum SensorPreset {
    /**
     * The default sensor preset with moderate configurations.
     */
    DEFAULT(
            5.0,
            new DeterministicDistribution(5.0)
    ),

    /**
     * The sensor preset that simulates an ideal sensor in a perfect environment
     */
    ASSUMED_PERFECT(
            0.5,
            new DeterministicDistribution(100.0)
    ),

    /**
     * The sensor preset that simulates a constrained sensor with higher latency and a uniform distribution for data generation.
     */
    CONSTRAINED(
            50.0,
            new DeterministicDistribution(0.5)
    );

    /**
     * The latency of the sensor in milliseconds.
     */
    public final double LATENCY;

    /**
     * The distribution used to generate sensor data or events.
     */
    public final Distribution DISTRIBUTION;

    /**
     * Constructs a SensorPreset with the specified latency and distribution.
     *
     * @param latency The latency of the sensor in milliseconds.
     * @param distribution The distribution used to generate sensor data.
     */
    SensorPreset(double latency, Distribution distribution) {
        this.LATENCY = latency;
        this.DISTRIBUTION = distribution;
    }
}
