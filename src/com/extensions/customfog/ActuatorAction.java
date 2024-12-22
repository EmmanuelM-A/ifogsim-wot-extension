package com.extensions.customfog;

import com.extensions.utils.presets.ActuatorPreset;
import com.extensions.vdcreation.models.Action;
import org.fog.entities.Actuator;

public class ActuatorAction extends Actuator {
    private final Action action;
    private final ActuatorPreset preset;

    public ActuatorAction(String name, int userId, String appId, Action action, ActuatorPreset preset) {
        super(name, userId, appId, name);

        // Define actuator configs in file

        this.action = action;
        this.preset = preset;
    }
}
