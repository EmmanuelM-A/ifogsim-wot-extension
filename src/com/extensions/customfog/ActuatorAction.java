package com.extensions.customfog;

import com.extensions.utils.presets.ActuatorPreset;
import com.extensions.vdcreation.models.Action;
import org.fog.entities.Actuator;

public class ActuatorAction extends Actuator {
    private final Action action;
    private final ActuatorPreset preset;
    private String name;
    private String actuatorType;

    public ActuatorAction(String name, int userId, String appId, Action action, ActuatorPreset preset) {
        super(name, userId, appId, name);

        this.name = name;
        this.actuatorType = name;

        // Define actuator configs in file

        this.action = action;
        this.preset = preset;
    }

    @Override
    public String toString() {
        return "{" + "Sensor Name: " + name + " | Actuator Type: " + actuatorType + " | Latency: " + preset.LATENCY + "}";
    }


}
