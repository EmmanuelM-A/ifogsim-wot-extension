package com.extensions.vdcreation.core;

import com.extensions.customfog.ActuatorAction;
import com.extensions.customfog.FogDeviceFactory;
import com.extensions.customfog.SensorProperty;
import com.extensions.utils.presets.CharacteristicsPreset;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.vdcreation.EventTrigger;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;

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
    private final List<Sensor> properties;

    /**
     * Represents actions in the TD.
     */
    private final List<Actuator> actions;

    /**
     * Represents events in the TD.
     */
    private final List<EventTrigger> events;

    public VirtualDevice(String name, FogDevicePreset preset) {
        this.fogDevice = FogDeviceFactory.createFogDevice(name, preset);
        this.properties = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.events = new ArrayList<>();
    }

    public VirtualDevice(String name, FogDevicePreset preset, VirtualDeviceConfig config) {
        this.fogDevice = FogDeviceFactory.createFogDevice(name, preset, config);
        this.properties = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.events = new ArrayList<>();
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

    public List<Actuator> getActions() {
        return actions;
    }

    public List<EventTrigger> getEvents() {
        return events;
    }
}
