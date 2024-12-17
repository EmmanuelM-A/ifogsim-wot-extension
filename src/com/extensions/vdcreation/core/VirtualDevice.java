package com.extensions.vdcreation.core;

import com.extensions.vdcreation.EventTrigger;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;

import java.util.ArrayList;
import java.util.List;

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
    private List<Sensor> properties;

    /**
     * Represents actions in the TD.
     */
    private List<Actuator> actions;

    /**
     * Represents events in the TD.
     */
    private List<EventTrigger> events;

    public VirtualDevice(String name, FogDeviceCharacteristics characteristics) {
        // Instantiate the fog device to represent the IoT device
            // Define fog characteristics
            // Define fog device arguments


        //fogDevice = new FogDevice(name, characteristics, )

        properties = new ArrayList<>();
        actions = new ArrayList<>();
        events = new ArrayList<>();
    }

    public FogDevice getFogDevice() {
        return fogDevice;
    }

    public void setFogDevice(FogDevice fogDevice) {
        this.fogDevice = fogDevice;
    }

    public List<Sensor> getProperties() {
        return properties;
    }

    public void setProperties(List<Sensor> properties) {
        this.properties = properties;
    }

    public List<Actuator> getActions() {
        return actions;
    }

    public void setActions(List<Actuator> actions) {
        this.actions = actions;
    }

    public List<EventTrigger> getEvents() {
        return events;
    }

    public void setEvents(List<EventTrigger> events) {
        this.events = events;
    }
}
