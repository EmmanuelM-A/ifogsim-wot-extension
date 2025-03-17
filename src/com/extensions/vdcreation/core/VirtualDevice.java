package com.extensions.vdcreation.core;

import com.extensions.customfog.*;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.vdcreation.models.ThingDescription;
import org.fog.entities.Actuator;
import org.fog.entities.Sensor;

import java.util.*;

/**
 * This class acts as a high level representation of a Thing, consisting of iFogSim entities. When simulated it will be able
 * behaviour similar to the device as described by its TD.
 */
public class VirtualDevice {
    /**
     * Represents the core virtual device in the simulation, mapping to the actual IoT device (Thing).
     */
    private CustomFogDevice fogDevice;

    /**
     * Acts as a centralized entity that represents all sensor (properties) for the VD.
     */
    private CustomSensor sensor;

    /**
     * Acts as a centralized entity that represents all actuator (actions) for the VD.
     */
    private CustomActuator actuator;

    /**
     * Represents all properties of the TD as if they were iFogSim sensors.
     */
    private final List<CustomSensor> sensorProperties;

    /**
     * Represents all actions from the TD as if they were iFogSim actuators.
     */
    private final List<ActuatorAction> actuatorActions;

    /**
     * Represents all events from the TD as if they were iFogSim sensors.
     */
    private final List<CustomSensor> eventSensors;

    /**
     * Records the TD used to create this virtual device.
     */
    private ThingDescription thingDescription;

    /**
     * Constructs a VirtualDevice using the specified name and preset.
     * @param name The name of the fog device.
     * @param preset The preset configuration for the fog device.
     */
    public VirtualDevice(String name, FogDevicePreset preset) {
        this.fogDevice = FogDeviceFactory.createFogDevice(name, preset);
        this.sensor = null;
        this.actuator = null;
        this.sensorProperties = new ArrayList<>();
        this.actuatorActions = new ArrayList<>();
        this.eventSensors = new ArrayList<>();
        this.thingDescription = null;
    }

    /**
     * Constructs a VirtualDevice using the specified name, preset, and configuration.
     * @param name The name of the fog device.
     * @param preset The preset configuration for the fog device.
     * @param config The configuration settings for the virtual device (e.g., tags, MIPS, RAM).
     */
    public VirtualDevice(String name, FogDevicePreset preset, VirtualDeviceConfig config) {
        this.fogDevice = FogDeviceFactory.createFogDevice(name, preset, config);
        this.sensor = null;
        this.actuator = null;
        this.sensorProperties = new ArrayList<>();
        this.actuatorActions = new ArrayList<>();
        this.eventSensors = new ArrayList<>();
        this.thingDescription = null;
    }

    /**
     * Gets the underlying fog device.
     * @return The fog device.
     */
    public CustomFogDevice getFogDevice() {
        return fogDevice;
    }

    /**
     * Sets the underlying fog device.
     * @param fogDevice The fog device to be set.
     */
    public void setFogDevice(CustomFogDevice fogDevice) {
        this.fogDevice = fogDevice;
    }

    /**
     * Gets the sensor associated with the device.
     * @return The sensor.
     */
    public CustomSensor getSensor() {
        return sensor;
    }

    /**
     * Sets the sensor associated with the device.
     * @param sensor The sensor to be set.
     */
    public void setSensor(CustomSensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Gets the actuator associated with the device.
     * @return The actuator.
     */
    public CustomActuator getActuator() {
        return actuator;
    }

    /**
     * Sets the actuator associated with the device.
     * @param actuator The actuator to be set.
     */
    public void setActuator(CustomActuator actuator) {
        this.actuator = actuator;
    }

    /**
     * Gets the list of sensor properties for the device.
     * @return A list of sensor properties.
     */
    public List<CustomSensor> getSensorProperties() {
        return sensorProperties;
    }

    /**
     * Gets the list of actuator actions for the device.
     * @return A list of actuator actions.
     */
    public List<ActuatorAction> getActuatorActions() {
        return actuatorActions;
    }

    /**
     * Gets the list of event sensors associated with the device.
     * @return A list of event sensors.
     */
    public List<CustomSensor> getEventSensors() {
        return eventSensors;
    }

    /**
     * Gets the Thing Description associated with the device.
     * @return The Thing Description.
     */
    public ThingDescription getThingDescription() {
        return thingDescription;
    }

    /**
     * Sets the Thing Description associated with the device.
     * @param thingDescription The Thing Description to be set.
     */
    public void setThingDescription(ThingDescription thingDescription) {
        this.thingDescription = thingDescription;
    }

    /**
     * Retrieves a specific sensor property by name.
     * @param name The name of the sensor property.
     * @return The sensor property, or null if not found.
     */
    public CustomSensor getSensorProperty(String name) {
        for (CustomSensor sensor : sensorProperties) {
            if (sensor.getName().equalsIgnoreCase(name)) {
                return sensor;
            }
        }
        return null;
    }

    /**
     * Retrieves a specific actuator action by name.
     * @param name The name of the actuator action.
     * @return The actuator action, or null if not found.
     */
    public Actuator getActuatorAction(String name) {
        for (Actuator actuator : actuatorActions) {
            if (actuator.getName().equalsIgnoreCase(name)) {
                return actuator;
            }
        }
        return null;
    }

    /**
     * Prints the data of the Virtual Device (VD), excluding the sensor and actuator variables.
     * This static method accesses the fields of a VD and outputs their data.
     *
     * @param virtualDevice the instance of VirtualDevice to print its data
     */
    public static void printVirtualDeviceData(VirtualDevice virtualDevice) {
        System.out.println("-----------------------------------------------------------------");
        if (virtualDevice == null) {
            System.out.println("Virtual Device is null");
            return;
        }

        // Print FogDevice data (assuming FogDevice has a meaningful toString method)
        if (virtualDevice.getFogDevice() != null) {
            System.out.println("FogDevice: " + virtualDevice.getFogDevice().getName());
        } else {
            System.out.println("FogDevice: Not available");
        }

        // Print sensor properties
        System.out.println("Sensor Properties:");
        if (virtualDevice.getSensorProperties() != null && !virtualDevice.getSensorProperties().isEmpty()) {
            for (Sensor sensor : virtualDevice.getSensorProperties()) {
                System.out.println("- " + sensor.getName()); // Assuming Sensor has a meaningful toString method
            }
        } else {
            System.out.println("No sensor properties available");
        }

        // Print actuator actions
        System.out.println("Actuator Actions:");
        if (virtualDevice.getActuatorActions() != null && !virtualDevice.getActuatorActions().isEmpty()) {
            for (Actuator actuator : virtualDevice.getActuatorActions()) {
                System.out.println("- " + actuator.getName()); // Assuming Actuator has a meaningful toString method
            }
        } else {
            System.out.println("No actuator actions available");
        }

        // Print events
        System.out.println("Events:");
        if (virtualDevice.getEventSensors() != null && !virtualDevice.getEventSensors().isEmpty()) {
            for (Sensor eventSensor : virtualDevice.getEventSensors()) {
                System.out.println("- " + eventSensor.toString());
            }
        } else {
            System.out.println("No event sensors available");
        }
        System.out.println("-----------------------------------------------------------------");
    }
}
