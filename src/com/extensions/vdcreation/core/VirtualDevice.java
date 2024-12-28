package com.extensions.vdcreation.core;

import com.extensions.customfog.FogDeviceFactory;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.vdcreation.EventTrigger;
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
    private FogDevice fogDevice;

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
    private final List<EventTrigger> events;

    /**
     * Represents the TD used to create this virtual device.
     */
    private ThingDescription thingDescription;

    public VirtualDevice(String name, FogDevicePreset preset) {
        this.fogDevice = FogDeviceFactory.createFogDevice(name, preset);
        this.sensorProperties = new ArrayList<>();
        this.actuatorActions = new ArrayList<>();
        this.events = new ArrayList<>();
        this.thingDescription = null;
    }

    public VirtualDevice(String name, FogDevicePreset preset, VirtualDeviceConfig config) {
        this.fogDevice = FogDeviceFactory.createFogDevice(name, preset, config);
        this.sensorProperties = new ArrayList<>();
        this.actuatorActions = new ArrayList<>();
        this.events = new ArrayList<>();
        this.thingDescription = null;
    }

    public FogDevice getFogDevice() {
        return fogDevice;
    }

    public void setFogDevice(FogDevice fogDevice) {
        this.fogDevice = fogDevice;
    }

    public List<Sensor> getSensorProperties() {
        return sensorProperties;
    }

    public List<Actuator> getActuatorActions() {
        return actuatorActions;
    }

    public List<EventTrigger> getEvents() {
        return events;
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
}
