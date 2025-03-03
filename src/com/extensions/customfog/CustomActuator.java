package com.extensions.customfog;

import com.extensions.utils.presets.ActuatorPreset;
import org.fog.entities.Actuator;

/**
 * Represents a custom actuator extending the base Actuator class.
 */
public class CustomActuator extends Actuator {
    /**
     * The preset configurations used to configure this actuator.
     */
    private final ActuatorPreset preset;

    /**
     * The name of the actuator.
     */
    private String name;

    /**
     * The type of the actuator.
     */
    private final String actuatorType;

    /**
     * Constructs a {@code CustomActuator} instance.
     * @param name The name of the actuator.
     * @param userId The user ID associated with the application.
     * @param appId The application ID the actuator is linked to.
     * @param preset The preset used to configure ths actuator.
     */
    public CustomActuator(String name, int userId, String appId, ActuatorPreset preset) {
        super(name, userId, appId, name);
        this.name = name;
        this.actuatorType = name;
        this.preset = preset;
    }

    /**
     * Retrieves the name of the actuator.
     *
     * @return The name of the actuator.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the actuator.
     * @param name The new name of the actuator.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a string representation of the actuator, displaying all the necessary
     * information about the actuator.
     * @return A formatted string containing the actuator's details.
     */
    @Override
    public String toString() {
        return "{" + "Sensor Name: " + name + " | Actuator Type: " + actuatorType + " | Latency: " + preset.LATENCY + "}";
    }
}
