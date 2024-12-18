package com.extensions.vdcreation.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.extensions.utils.FilePaths;
import com.extensions.utils.presets.ActuatorPreset;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.utils.presets.SensorPreset;
import com.extensions.vdcreation.models.Property;
import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.parsers.ThingDescriptionParser;
import org.fog.entities.Sensor;


public class VirtualDeviceFactory {
    private int userId;

    private String appId;

    private int gatewayDeviceId;

    private FogDevicePreset fogDevicePreset;

    private SensorPreset sensorPreset;

    private ActuatorPreset actuatorPreset;

    public VirtualDeviceFactory(int userId, String appId, int gatewayDeviceId, FogDevicePreset fogDevicePreset, SensorPreset sensorPreset, ActuatorPreset actuatorPreset) {
        this.userId = userId;
        this.appId = appId;
        this.gatewayDeviceId = gatewayDeviceId;
        this.fogDevicePreset = fogDevicePreset;
        this.sensorPreset = sensorPreset;
        this.actuatorPreset = actuatorPreset;
    }

    public VirtualDevice createVirtualDevice(ThingDescription thingDescription) {
        // Create an empty virtual device
        //VirtualDevice virtualDevice = new VirtualDevice()

        // Create the sensors for the TD properties
        for(Map.Entry<String, Property> propertyEntry : thingDescription.getProperties().entrySet()) {
            String propertyName = propertyEntry.getKey();
            Property property = propertyEntry.getValue();

            // Map the property to a Sensor
            Sensor sensorProperty = new Sensor(propertyName, property.getType(), userId, appId, sensorPreset.DISTRIBUTION);
        }

        // Create actuators for the TD actions

        // Create event triggers for the events (Create a Trigger class and an EventTrigger class)


        return null;
    }
}
