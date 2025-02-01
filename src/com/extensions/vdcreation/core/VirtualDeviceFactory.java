package com.extensions.vdcreation.core;

import java.util.List;
import java.util.Map;

import com.extensions.customfog.ActuatorAction;
import com.extensions.customfog.SensorProperty;
import com.extensions.utils.presets.ActuatorPreset;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.utils.presets.SensorPreset;
import com.extensions.vdcreation.models.Event;
import com.extensions.vdcreation.models.Property;
import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.models.Action;
import org.fog.entities.Sensor;

public class VirtualDeviceFactory {
    private final int userId;

    private final String appId;

    private final FogDevicePreset fogDevicePreset;

    private final SensorPreset sensorPreset;

    private final ActuatorPreset actuatorPreset;

    public VirtualDeviceFactory(int userId, String appId, FogDevicePreset fogDevicePreset, SensorPreset sensorPreset, ActuatorPreset actuatorPreset) {
        this.userId = userId;
        this.appId = appId;
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

        // TODO ENSURE WRITE-PROPERTIES ARE ADDED AS ACTUATORS - REMEMBER

        // Create the sensors for the TD properties
        for(Map.Entry<String, Property> propertyEntry : thingDescription.getProperties().entrySet()) {
            // Extract entry data
            String propertyName = propertyEntry.getKey();
            Property property = propertyEntry.getValue();

            // Map the property to a SensorProperty
            Sensor sensorProperty = new SensorProperty(propertyName, userId, appId, property, sensorPreset);

            // Set the sensor's gateway device ID to the VD's ID
            sensorProperty.setGatewayDeviceId(virtualDevice.getFogDevice().getId());

            // Set the latency of the sensor communication
            sensorProperty.setLatency(sensorPreset.LATENCY);

            // Add sensor property to the virtual device
            virtualDevice.getSensorProperties().add(sensorProperty);
        }

        // Create actuators for the TD actions
        for(Map.Entry<String, Action> actionEntry : thingDescription.getActions().entrySet()) {
            // Extract entry data
            String actionName = actionEntry.getKey();
            Action action = actionEntry.getValue();

            // Map action to an ActuatorAction
            ActuatorAction actuatorAction = new ActuatorAction(actionName, userId, appId, action, actuatorPreset);

            // Add actuator action to the virtual device
            virtualDevice.getActuatorActions().add(actuatorAction);

            // Set the actuator's gateway device ID to the VD's ID
            actuatorAction.setGatewayDeviceId(virtualDevice.getFogDevice().getId());

            // Set the latency of the actuator communication
            actuatorAction.setLatency(actuatorPreset.LATENCY);
        }

        // Create event for the events
        for(Map.Entry<String, Event> eventEntry : thingDescription.getEvents().entrySet()) {
            // Extract entry data
            String eventName = eventEntry.getKey();
            Event event = eventEntry.getValue();

            // Set event name
            event.setTitle(eventName);

            // Add event to virtual device
            virtualDevice.getEvents().add(event);
        }

        return virtualDevice;
    }

    private VirtualDevice defineVirtualDevice(ThingDescription thingDescription, List<VirtualDeviceConfig> configs) {
        // Create a virtual device using defined presets only
        VirtualDevice virtualDevice = new VirtualDevice(thingDescription.getTitle(), fogDevicePreset);

        // Store the VD's TD
        virtualDevice.setThingDescription(thingDescription);

        if(configs == null || configs.isEmpty()) return virtualDevice;

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
