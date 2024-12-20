package com.extensions.vdcreation.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.extensions.customfog.ActuatorAction;
import com.extensions.customfog.SensorProperty;
import com.extensions.utils.FilePaths;
import com.extensions.utils.presets.ActuatorPreset;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.utils.presets.SensorPreset;
import com.extensions.vdcreation.models.Event;
import com.extensions.vdcreation.models.Property;
import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.models.Action;
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
        VirtualDevice virtualDevice = new VirtualDevice(thingDescription.getTitle(), fogDevicePreset);

        // Create the sensors for the TD properties
        for(Map.Entry<String, Property> propertyEntry : thingDescription.getProperties().entrySet()) {
            // Extract entry data
            String propertyName = propertyEntry.getKey();
            Property property = propertyEntry.getValue();

            // Map the property to a SensorProperty
            SensorProperty sensorProperty = new SensorProperty(propertyName, userId, appId, property, sensorPreset);

            // Add sensor property to the virtual device
            virtualDevice.getProperties().put(propertyName, sensorProperty);
        }

        // Create actuators for the TD actions
        for(Map.Entry<String, Action> actionEntry : thingDescription.getActions().entrySet()) {
            // Extract entry data
            String actionName = actionEntry.getKey();
            Action action = actionEntry.getValue();

            // Map action to an ActuatorAction
            ActuatorAction actuatorAction = new ActuatorAction(actionName, userId, appId, action, actuatorPreset);

            // Add actuator action to the virtual device
            virtualDevice.getActions().put(actionName, actuatorAction);
        }

        // Create event triggers for the events (Create a Trigger class and an EventTrigger class)
        for(Map.Entry<String, Event> eventEntry : thingDescription.getEvents().entrySet()) {
            // Extract entry data
            String eventName = eventEntry.getKey();
            Event event = eventEntry.getValue();

            // Map action to an ActuatorAction


            // Add actuator action to the virtual device
        }

        return virtualDevice;
    }
}
