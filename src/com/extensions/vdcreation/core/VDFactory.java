package com.extensions.vdcreation.core;

import com.extensions.customfog.CustomActuator;
import com.extensions.customfog.CustomSensor;
import com.extensions.utils.presets.ActuatorPreset;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.utils.presets.SensorPreset;
import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.parsers.ThingDescriptionParser;
import org.fog.entities.Sensor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VDFactory {
    private final int userId;

    private final String appId;

    private final FogDevicePreset fogDevicePreset;

    private final SensorPreset sensorPreset;

    private final ActuatorPreset actuatorPreset;

    private VDFactory(int userId, String appId, FogDevicePreset fogDevicePreset, SensorPreset sensorPreset, ActuatorPreset actuatorPreset) {
        this.userId = userId;
        this.appId = appId;
        this.fogDevicePreset = fogDevicePreset;
        this.sensorPreset = sensorPreset;
        this.actuatorPreset = actuatorPreset;
    }

    public static List<VD> createVirtualDevices(
            int userId,
            String appId,
            FogDevicePreset fogDevicePreset,
            SensorPreset sensorPreset,
            ActuatorPreset actuatorPreset,
            String thingRepoFolder,
            List<VirtualDeviceConfig> configs
    ) throws IOException {
        // The list to store virtual devices created
        List<VD> createdVirtualDevices = new ArrayList<>();

        // Extract the metadata from the TDs
        List<ThingDescription> thingDescriptions = JsonFileProcessor.processJsonFiles(
                thingRepoFolder,
                new ThingDescriptionParser()
        );

        // Set up the VD factory to create VDs with the appropriate presets
        VDFactory virtualDeviceFactory = new VDFactory(
                userId,
                appId,
                fogDevicePreset,
                sensorPreset,
                actuatorPreset
        );

        // Create the virtual devices using the thing descriptions and factory method
        for(ThingDescription thingDescription : thingDescriptions) {
            VD vd = virtualDeviceFactory.createVirtualDevice(
                    thingDescription,
                    configs
            );
            // IF YOU WISH TO VALIDATE VDS, DO IT HERE
            createdVirtualDevices.add(vd);
        }

        return createdVirtualDevices;
    }

    private VD createVirtualDevice(ThingDescription thingDescription, List<VirtualDeviceConfig> configs) {
        VD virtualDevice = defineVirtualDevice(thingDescription, configs);

        virtualDevice.setThingDescription(thingDescription);

        // Format TD title, so it matches the format of the VD title
        String tdName = thingDescription.getTitle().replace(" ", "");

        // Represent all senors
        String vdSensorName = tdName + "_SENSOR";

        CustomSensor vdSensor = new CustomSensor(vdSensorName, userId, appId, sensorPreset);

        // Set the sensor's gateway device ID to the VD's ID
        vdSensor.setGatewayDeviceId(virtualDevice.getFogDevice().getId());

        // Set the latency of the sensor communication
        vdSensor.setLatency(sensorPreset.LATENCY);

        virtualDevice.setSensor(vdSensor);

        // Represent all actuators
        String vdActuatorName = tdName + "_ACTUATOR";

        CustomActuator vdActuator = new CustomActuator(vdActuatorName, userId, appId, actuatorPreset);

        // Set the actuator's gateway device ID to the VD's ID
        vdActuator.setGatewayDeviceId(virtualDevice.getFogDevice().getId());

        // Set the latency of the actuator communication
        vdActuator.setLatency(actuatorPreset.LATENCY);

        // Add actuator action to the virtual device
        virtualDevice.setActuator(vdActuator);

        return virtualDevice;
    }

    /**
     * Creates a virtual device and sets its configurations if a VD config file is present for that VD.
     *
     * @param thingDescription The thing description used to map thing attributes into a virtual device.
     * @param configs A list of VD configurations
     * @return The created VD from the TD with is configurations and components defined and set.
     */
    private VD defineVirtualDevice(ThingDescription thingDescription, List<VirtualDeviceConfig> configs) {
        // Create a virtual device using defined presets only
        VD virtualDevice = new VD(thingDescription.getTitle(), fogDevicePreset);

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
                virtualDevice = new VD(thingDescription.getTitle(), fogDevicePreset, config);
                break;
            }
        }
        return virtualDevice;
    }
}
