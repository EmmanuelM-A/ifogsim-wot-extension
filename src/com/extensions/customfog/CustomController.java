package com.extensions.customfog;

import com.extensions.custommetrics.CustomMetricManager;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.placement.Controller;

import java.util.List;

public class CustomController extends Controller {
    private final CustomMetricManager customMetricManager;
    public CustomController(String name, List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators) {
        super(name, fogDevices, sensors, actuators);

        this.customMetricManager = new CustomMetricManager();
    }

    public CustomMetricManager getCustomMetricManager() {
        return customMetricManager;
    }
}
