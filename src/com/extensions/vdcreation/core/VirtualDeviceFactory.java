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

public class VirtualDeviceFactory {
    private final int userId;

    private final String appId;

    private final int parentId;

    private final FogDevicePreset fogDevicePreset;

    private final SensorPreset sensorPreset;

    private final ActuatorPreset actuatorPreset;

    public VirtualDeviceFactory(int userId, String appId, int parentId, FogDevicePreset fogDevicePreset, SensorPreset sensorPreset, ActuatorPreset actuatorPreset) {
        this.userId = userId;
        this.appId = appId;
        this.parentId = parentId;
        this.fogDevicePreset = fogDevicePreset;
        this.sensorPreset = sensorPreset;
        this.actuatorPreset = actuatorPreset;
    }

    /**
     * Creates a virtual device based on the provided {@link ThingDescription}.
     *
     * @param thingDescription The extracted information from a IoT TD.
     * @return A virtual device that contains all necessary information about the TD.
     */
    public VirtualDevice createVirtualDevice(ThingDescription thingDescription, List<VirtualDeviceConfig> configs) {
        // Instantiate a virtual device with no sensors or actuators
        VirtualDevice virtualDevice = defineVirtualDevice(thingDescription, configs);

        // Set the parent ID of the VD
        virtualDevice.getFogDevice().setParentId(parentId);

        // Create the sensors for the TD properties
        for(Map.Entry<String, Property> propertyEntry : thingDescription.getProperties().entrySet()) {
            // Extract entry data
            String propertyName = propertyEntry.getKey();
            Property property = propertyEntry.getValue();

            // Map the property to a SensorProperty
            SensorProperty sensorProperty = new SensorProperty(propertyName, userId, appId, property, sensorPreset);

            // Add sensor property to the virtual device
            virtualDevice.getProperties().put(propertyName, sensorProperty);

            // Set the sensor's gateway device ID to the VD's ID
            sensorProperty.setGatewayDeviceId(virtualDevice.getFogDevice().getId());
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

            // Set the actuator's gateway device ID to the VD's ID
            actuatorAction.setGatewayDeviceId(virtualDevice.getFogDevice().getId());
        }

        // Create event triggers for the events (Create a Trigger class and an EventTrigger class)
        /*for(Map.Entry<String, Event> eventEntry : thingDescription.getEvents().entrySet()) {
            // Extract entry data
            String eventName = eventEntry.getKey();
            Event event = eventEntry.getValue();

            // Map action to an ActuatorAction


            // Add actuator action to the virtual device
        }*/

        return virtualDevice;
    }

    private VirtualDevice defineVirtualDevice(ThingDescription thingDescription, List<VirtualDeviceConfig> configs) {
        // Create a virtual device using defined presets only
        VirtualDevice virtualDevice = new VirtualDevice(thingDescription.getTitle(), fogDevicePreset);

        // Search through the configs to find a VD config file for the TD
        for(VirtualDeviceConfig config : configs) {
            // Check if the TD has a config file for its VD
            if(config.getTags().contains(thingDescription.getTitle())) {
                // Create a virtual device using the defined presets and the config data for this VD
                virtualDevice = new VirtualDevice(thingDescription.getTitle(), fogDevicePreset, config);
                break;
            }
        }
        return virtualDevice;
    }
}
