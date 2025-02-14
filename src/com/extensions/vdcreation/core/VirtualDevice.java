package com.extensions.vdcreation.core;

import com.extensions.customfog.CustomFogDevice;
import com.extensions.customfog.FogDeviceFactory;
import com.extensions.sysconstructor.eventdriver.EventSensor;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.vdcreation.models.Event;
import com.extensions.vdcreation.models.ThingDescription;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;

import java.util.*;

/**
 * This class acts as a high level representation of a Thing Description (TD) in iFogSim. When simulated it will be able
 * to simulate the behaviour of the device as described by its TD.
 */
public class VirtualDevice {
    /**
     * Represents the core virtual device in the simulation, mapping to the actual IoT device.
     */
    private CustomFogDevice fogDevice;

    /**
     * Represents properties of the TD.
     */
    private final List<Sensor> sensorProperties;

    /**
     * Represents actions in the TD.
     */
    private final List<Actuator> actuatorActions;

    /**
     * Represents events in the TD.
     */
    private final List<Sensor> eventSensors;

    /**
     * Represents the TD used to create this virtual device.
     */
    private ThingDescription thingDescription;

    public VirtualDevice(String name, FogDevicePreset preset) {
        this.fogDevice = FogDeviceFactory.createFogDevice(name, preset);
        this.sensorProperties = new ArrayList<>();
        this.actuatorActions = new ArrayList<>();
        this.eventSensors = new ArrayList<>();
        this.thingDescription = null;
    }

    public VirtualDevice(String name, FogDevicePreset preset, VirtualDeviceConfig config) {
        this.fogDevice = FogDeviceFactory.createFogDevice(name, preset, config);
        this.sensorProperties = new ArrayList<>();
        this.actuatorActions = new ArrayList<>();
        this.eventSensors = new ArrayList<>();
        this.thingDescription = null;
    }

    public CustomFogDevice getFogDevice() {
        return fogDevice;
    }

    public void setFogDevice(CustomFogDevice fogDevice) {
        this.fogDevice = fogDevice;
    }

    public List<Sensor> getSensorProperties() {
        return sensorProperties;
    }

    public List<Actuator> getActuatorActions() {
        return actuatorActions;
    }

    public List<Sensor> getEventSensors() {
        return eventSensors;
    }

    public ThingDescription getThingDescription() {
        return thingDescription;
    }

    public void setThingDescription(ThingDescription thingDescription) {
        this.thingDescription = thingDescription;
    }

    public Sensor getSensorProperty(String name) {
        for(Sensor sensor : sensorProperties) {
            if(sensor.getName().equalsIgnoreCase(name)) return sensor;
        }
        return null;
    }

    public Actuator getActuatorAction(String name) {
        for(Actuator actuator : actuatorActions) {
            if(actuator.getName().equalsIgnoreCase(name)) return actuator;
        }
        return null;
    }

    /**
     * Prints the data of the Virtual Device (VD).
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
