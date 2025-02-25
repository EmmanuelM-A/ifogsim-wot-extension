package com.extensions.customfog;

import com.extensions.utils.presets.ActuatorPreset;
import com.extensions.vdcreation.models.Action;
import org.fog.entities.Actuator;

public class CustomActuator extends Actuator {
    private final ActuatorPreset preset;
    private String name;
    private String actuatorType;

    public CustomActuator(String name, int userId, String appId, ActuatorPreset preset) {
        super(name, userId, appId, name);

        this.name = name;
        this.actuatorType = name;

        this.preset = preset;
    }

    @Override
    public String toString() {
        return "{" + "Sensor Name: " + name + " | Actuator Type: " + actuatorType + " | Latency: " + preset.LATENCY + "}";
    }
}
