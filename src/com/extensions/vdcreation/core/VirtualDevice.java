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
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
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
    private final Map<String, SensorProperty> properties;

    /**
     * Represents actions in the TD.
     */
    private final Map<String, ActuatorAction> actions;

    /**
     * Represents events in the TD.
     */
    private final Map<String, EventTrigger> events;

    public VirtualDevice(String name, FogDevicePreset preset) {
        this.fogDevice = FogDeviceFactory.createFogDevice(name, preset);
        this.properties = new HashMap<>();
        this.actions = new HashMap<>();
        this.events = new HashMap<>();
    }

    public VirtualDevice(String name, FogDevicePreset preset, VirtualDeviceConfig config) {
        this.fogDevice = FogDeviceFactory.createFogDevice(name, preset, config);
        this.properties = new HashMap<>();
        this.actions = new HashMap<>();
        this.events = new HashMap<>();
    }

    public FogDevice getFogDevice() {
        return fogDevice;
    }

    public void setFogDevice(FogDevice fogDevice) {
        this.fogDevice = fogDevice;
    }

    public Map<String, SensorProperty> getProperties() {
        return properties;
    }

    public Map<String, ActuatorAction> getActions() {
        return actions;
    }

    public Map<String, EventTrigger> getEvents() {
        return events;
    }
}
