package com.extensions.customfog;

import com.extensions.utils.presets.ActuatorPreset;
import com.extensions.vdcreation.models.Action;
import org.fog.entities.Actuator;

/**
 * Represents a TD action which has been mapped as an iFogSim actuator, extending
 * the base Actuator class.
 */
public class ActuatorAction extends Actuator {
    /**
     * The action associated to this actuator.
     */
    private final Action action;

    /**
     * The preset configuration used to configure the actuator.
     */
    private final ActuatorPreset preset;

    /**
     * The name of the actuator.
     */
    private String name;

    /**
     * The type of actuator.
     */
    private final String actuatorType;

    /**
     * Constructs an {@code ActuatorAction} instance.
     * @param name The name of the actuator.
     * @param userId The user ID of the application which the actuator belongs to.
     * @param appId The application ID the actuator is linked to.
     * @param action The action this actuator performs.
     * @param preset The preset used to configure the actuator.
     */
    public ActuatorAction(String name, int userId, String appId, Action action, ActuatorPreset preset) {
        super(name, userId, appId, name);
        this.name = name;
        this.actuatorType = name;
        this.action = action;
        this.preset = preset;
    }

    /**
     * Retrieves the action associated with this actuator.
     * @return The action for this actuator.
     */
    public Action getAction() {
        return action;
    }

    /**
     * Retrieves the name of the actuator.
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
