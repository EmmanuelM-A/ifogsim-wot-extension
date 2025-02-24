package com.extensions.vdcreation.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extensions.customfog.ActuatorAction;
import com.extensions.customfog.CustomSensor;
import com.extensions.customfog.GeneralActuator;
import com.extensions.sysconstructor.eventdriver.EventSensor;
import com.extensions.utils.presets.ActuatorPreset;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.utils.presets.SensorPreset;
import com.extensions.vdcreation.models.Event;
import com.extensions.vdcreation.models.Property;
import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.models.Action;
import com.extensions.vdcreation.parsers.ThingDescriptionParser;
import org.fog.entities.Actuator;

/**
 * Responsible for creating the virtual devices (VDs) from the thing descriptions.
 */
public class VirtualDeviceFactory {
    private final int userId;

    private final String appId;

    private final FogDevicePreset fogDevicePreset;

    private final SensorPreset sensorPreset;

    private final ActuatorPreset actuatorPreset;

    public static String GENERAL_ACTUATOR = "GENERAL-ACTUATOR";

    private VirtualDeviceFactory(int userId, String appId, FogDevicePreset fogDevicePreset, SensorPreset sensorPreset, ActuatorPreset actuatorPreset) {
        this.userId = userId;
        this.appId = appId;
        this.fogDevicePreset = fogDevicePreset;
        this.sensorPreset = sensorPreset;
        this.actuatorPreset = actuatorPreset;
    }

    public static List<VirtualDevice> createVirtualDevices(
            int userId,
            String appId,
            FogDevicePreset fogDevicePreset,
            SensorPreset sensorPreset,
            ActuatorPreset actuatorPreset,
            String thingRepoFolder,
            List<VirtualDeviceConfig> configs
    ) throws IOException {
        // The list to store virtual devices created
        List<VirtualDevice> createdVirtualDevices = new ArrayList<>();

        // Extract the metadata from the TDs
        List<ThingDescription> thingDescriptions = JsonFileProcessor.processJsonFiles(
                thingRepoFolder,
                new ThingDescriptionParser()
        );

        // Set up the VD factory to create VDs with the appropriate presets
        VirtualDeviceFactory virtualDeviceFactory = new VirtualDeviceFactory(
                userId,
                appId,
                fogDevicePreset,
                sensorPreset,
                actuatorPreset
        );

        // Create the virtual devices using the thing descriptions and factory method
        for(ThingDescription thingDescription : thingDescriptions) {
            VirtualDevice vd = virtualDeviceFactory.createVirtualDevice(
                    thingDescription,
                    configs
            );
            // IF YOU WISH TO VALIDATE VDS, DO IT HERE
            createdVirtualDevices.add(vd);
        }

        return createdVirtualDevices;
    }

    /**
     * Creates a virtual device based on the provided {@link ThingDescription}. If there are configurations present
     * for any VD, they will be applied.
     *
     * @param thingDescription The extracted information from a IoT TD.
     * @param configs A list of VD configurations
     * @return A virtual device that contains all necessary information about the TD.
     */
    public VirtualDevice createVirtualDevice(ThingDescription thingDescription, List<VirtualDeviceConfig> configs) {
        // Instantiate a virtual device with no sensors or actuators
        VirtualDevice virtualDevice = defineVirtualDevice(thingDescription, configs);

        Map<String, Property> writeProperties = new HashMap<>();

        // Create the sensors for the TD properties
        for(Map.Entry<String, Property> propertyEntry : thingDescription.getProperties().entrySet()) {
            // Extract entry data
            String propertyName = propertyEntry.getKey();
            Property property = propertyEntry.getValue();

            if(property.isWriteOnly() || property.isWriteable()) {
                // If a writeable property, record it and skip
                writeProperties.put(propertyName, property);
            } else {
                // Map the property to a SensorProperty
                CustomSensor sensorProperty = new CustomSensor(propertyName, userId, appId, sensorPreset);
                sensorProperty.setProperty(property);

                // Set the sensor's gateway device ID to the VD's ID
                sensorProperty.setGatewayDeviceId(virtualDevice.getFogDevice().getId());

                // Set the latency of the sensor communication
                sensorProperty.setLatency(sensorPreset.LATENCY);

                // Add sensor property to the virtual device
                virtualDevice.getSensorProperties().add(sensorProperty);
            }
        }

        // Create actuators for the TD actions
        for(Map.Entry<String, Action> actionEntry : thingDescription.getActions().entrySet()) {
            // Set writeable properties as actions
            for(Map.Entry<String, Property> writeableProperty : writeProperties.entrySet()) {
                // Extract entry data
                String propertyName = writeableProperty.getKey();
                //Property property = writeableProperty.getValue();

                // Create a writeable property actuator action
                ActuatorAction writePropertyAction = new ActuatorAction(propertyName, userId, appId, null, actuatorPreset);

                // Set the actuator's gateway device ID to the VD's ID
                writePropertyAction.setGatewayDeviceId(virtualDevice.getFogDevice().getId());

                // Set the latency of the actuator communication
                writePropertyAction.setLatency(actuatorPreset.LATENCY);

                // Add actuator action to the virtual device
                virtualDevice.getActuatorActions().add(writePropertyAction);
            }

            // Extract entry data
            String actionName = actionEntry.getKey();
            Action action = actionEntry.getValue();

            // Map action to an ActuatorAction
            ActuatorAction actuatorAction = new ActuatorAction(actionName, userId, appId, action, actuatorPreset);

            // Set the actuator's gateway device ID to the VD's ID
            actuatorAction.setGatewayDeviceId(virtualDevice.getFogDevice().getId());

            // Set the latency of the actuator communication
            actuatorAction.setLatency(actuatorPreset.LATENCY);

            // Add actuator action to the virtual device
            virtualDevice.getActuatorActions().add(actuatorAction);
        }

        // Create sensors for the TD events
        for(Map.Entry<String, Event> eventEntry : thingDescription.getEvents().entrySet()) {
            // Extract entry data
            String eventName = eventEntry.getKey();
            Event event = eventEntry.getValue();

            // Map event to an EventSensor
            EventSensor eventSensor = new EventSensor(eventName, userId, appId, sensorPreset);
            eventSensor.setEvent(event);

            // Set the sensor's gateway device ID to the VD's ID
            eventSensor.setGatewayDeviceId(virtualDevice.getFogDevice().getId());

            // Set the latency of the sensor communication
            eventSensor.setLatency(sensorPreset.LATENCY);

            // Add the event sensor to the virtual device
            virtualDevice.getEventSensors().add(eventSensor);
        }

        // Connect General Actuator to VD
        GeneralActuator generalActuator = new GeneralActuator(GENERAL_ACTUATOR, userId, appId, actuatorPreset, virtualDevice.getFogDevice().getId());

        // Set the actuator's gateway device ID to the VD's ID
        generalActuator.setGatewayDeviceId(virtualDevice.getFogDevice().getId());

        // Set the latency of the actuator communication
        generalActuator.setLatency(actuatorPreset.LATENCY);

        // Add actuator action to the virtual device
        virtualDevice.getActuatorActions().add(generalActuator);

        return virtualDevice;
    }

    /**
     * Creates a virtual device and sets its configurations if a VD config file is present for that VD.
     *
     * @param thingDescription The thing description used to map thing attributes into a virtual device.
     * @param configs A list of VD configurations
     * @return The created VD from the TD with is configurations and components defined and set.
     */
    private VirtualDevice defineVirtualDevice(ThingDescription thingDescription, List<VirtualDeviceConfig> configs) {
        // Create a virtual device using defined presets only
        VirtualDevice virtualDevice = new VirtualDevice(thingDescription.getTitle(), fogDevicePreset);

        // Store the VD's TD
        virtualDevice.setThingDescription(thingDescription);

        if(configs == null || configs.isEmpty()) {
            System.out.println("No VD Configurations Set!");
            return virtualDevice;
        }

        // Search through the configs to find a VD config file for the TD
        for(VirtualDeviceConfig config : configs) {
            // Format TD title, so it matches the format of the VD title
            String tdName = thingDescription.getTitle().replace(" ", "");

            // Check if the TD has a config file for its VD
            if(config.tags().contains(tdName)) {
                // Create a virtual device using the defined presets and the config data for this VD
                virtualDevice = new VirtualDevice(thingDescription.getTitle(), fogDevicePreset, config);
                break;
            }
        }
        return virtualDevice;
    }
}
