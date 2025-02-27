package com.extensions.utils.presets;

/**
 * Defines presets for actuator configurations in the iFogSim simulation.
 */
public enum ActuatorPreset {
    /**
     * The default actuator preset with moderate latency.
     */
    DEFAULT(
            5.0
    ),

    /**
     * An actuator preset that simulates a high-performing actuator with low latency.
     */
    ASSUMED_PERFECT(
            0.5
    ),

    /**
     * An actuator preset that simulates a constrained actuator with higher latency.
     */
    CONSTRAINED(
            50.0
    );

    /**
     * The latency of the sensor in milliseconds.
     */
    public final double LATENCY;

    /**
     * Constructs an ActuatorPreset with the specified latency.
     *
     * @param latency The latency of the actuator in milliseconds.
     */
    ActuatorPreset(double latency) {
        this.LATENCY = latency;
    }
}
