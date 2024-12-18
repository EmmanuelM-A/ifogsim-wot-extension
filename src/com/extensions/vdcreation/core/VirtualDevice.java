package com.extensions.vdcreation.core;

import com.extensions.utils.Preset;
import com.extensions.vdcreation.EventTrigger;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
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
    private FogDevice fogDevice = null;

    /**
     * Represents properties of the TD.
     */
    private List<Sensor> properties = null;

    /**
     * Represents actions in the TD.
     */
    private List<Actuator> actions = null;

    /**
     * Represents events in the TD.
     */
    private List<EventTrigger> events = null;

    public VirtualDevice(String name, FogDeviceCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval, Preset preset) {
        // Instantiate an "empty" fog device with the passed in preset configurations
        try {
            fogDevice = new FogDevice(
                    name,
                    characteristics,
                    vmAllocationPolicy,
                    storageList,
                    schedulingInterval,
                    preset.UPLINK_BW,
                    preset.DOWNLINK_BW,
                    preset.UPLINK_LATENCY,
                    preset.RATE_PER_MIPS
            );
        } catch(Exception e) {
            e.printStackTrace();
        }
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
