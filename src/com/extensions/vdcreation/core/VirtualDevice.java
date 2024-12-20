package com.extensions.vdcreation.core;

import com.extensions.customfog.ActuatorAction;
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
        this.fogDevice = createFogDevice(name, preset);
        this.properties = new HashMap<>();
        this.actions = new HashMap<>();
        this.events = new HashMap<>();
    }

    public VirtualDevice(String name, FogDevicePreset preset, VirtualDeviceConfig config) {
        this.fogDevice = createFogDevice(name, preset, config);
        this.properties = new HashMap<>();
        this.actions = new HashMap<>();
        this.events = new HashMap<>();
    }

    /**
     * Creates a FogDevice instance using the specified name and preset configuration.
     *
     * @param name   The name of the FogDevice to be created.
     * @param preset The preset configuration containing the default values for the device.
     * @return The created FogDevice object, or null if an exception occurs.
     */
    public FogDevice createFogDevice(String name, FogDevicePreset preset) {
        // Create a list of Processing Elements (PEs) with the specified MIPS value from the preset
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerOverbooking(preset.MIPS)));

        // Generate a unique host ID and define host properties
        int hostId = FogUtils.generateEntityId();
        long hostStorage = 10000000; // The storage capacity in bytes

        // Create a PowerHost instance with the given specifications
        PowerHost host = new PowerHost(
            hostId,
            new RamProvisionerSimple(preset.RAM),
            new BwProvisionerOverbooking(preset.BANDWIDTH),
            hostStorage,
            peList,
            new StreamOperatorScheduler(peList),
            new FogLinearPowerModel(preset.BUSY_POWER, preset.IDLE_POWER)
        );

        // Add the created host to a host list
        List<Host> hostList = new ArrayList<>();
        hostList.add(host);

        // Create an empty storage list
        List<Storage> storageList = new LinkedList<>();

        // Define the characteristics of the FogDevice
        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                CharacteristicsPreset.SYS_ARCH,
                CharacteristicsPreset.OS,
                CharacteristicsPreset.VMM,
                host,
                CharacteristicsPreset.TIME_ZONE,
                CharacteristicsPreset.COST,
                CharacteristicsPreset.COST_PER_MEMORY,
                CharacteristicsPreset.COST_PER_STORAGE,
                CharacteristicsPreset.COST_PER_BW
        );

        // Create and return the FogDevice instance
        FogDevice fogDevice = null;
        try {
            fogDevice = new FogDevice(
                    name,
                    characteristics,
                    new AppModuleAllocationPolicy(hostList), // CHANGE THIS SO USERS CAN DEFINE THEIR OWN VM ALLOCATION POLICY
                    storageList,
                    preset.SCHEDULING_INTERVAL,
                    preset.UPLINK_BW,
                    preset.DOWNLINK_BW,
                    preset.UPLINK_LATENCY,
                    preset.RATE_PER_MIPS
            );

            return fogDevice;
        } catch(Exception e) {
            // Return null if the FogDevice could not be created
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a FogDevice instance using the specified name, preset configuration, and custom virtual device configuration.
     *
     * @param name The name of the FogDevice to be created.
     * @param preset The preset configuration containing default values for the device.
     * @param config The VirtualDeviceConfig providing custom values for specific device properties.
     * @return The created FogDevice object, or null if an exception occurs.
     */
    public FogDevice createFogDevice(String name, FogDevicePreset preset, VirtualDeviceConfig config) {
        // Create a list of Processing Elements (PEs) with the MIPS value from the virtual device configuration
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerOverbooking(config.getMips())));

        // Generate a unique host ID and use it to create a PowerHost with the virtual device configuration
        int hostId = FogUtils.generateEntityId();
        PowerHost host = getPowerHost(config, hostId, peList);

        // Add the created host to a host list
        List<Host> hostList = new ArrayList<>();
        hostList.add(host);

        // Create an empty storage list
        List<Storage> storageList = new LinkedList<>();

        // Define the characteristics of the FogDevice
        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                CharacteristicsPreset.SYS_ARCH,
                CharacteristicsPreset.OS,
                CharacteristicsPreset.VMM,
                host,
                CharacteristicsPreset.TIME_ZONE,
                CharacteristicsPreset.COST,
                CharacteristicsPreset.COST_PER_MEMORY,
                CharacteristicsPreset.COST_PER_STORAGE,
                CharacteristicsPreset.COST_PER_BW
        );

        FogDevice fogDevice = null;
        try {
            fogDevice = new FogDevice(
                    name,
                    characteristics,
                    new AppModuleAllocationPolicy(hostList), // CHANGE THIS SO USERS CAN DEFINE THEIR OWN VM ALLOCATION POLICY
                    storageList,
                    preset.SCHEDULING_INTERVAL,
                    config.getUpBw(),  // Use custom uplink bandwidth
                    config.getDownBw(), // Use custom down link bandwidth
                    preset.UPLINK_LATENCY,
                    config.getRatePerMips() // Use custom rate per MIPS
            );

            // Return null if the FogDevice could not be created
            return fogDevice;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static PowerHost getPowerHost(VirtualDeviceConfig config, int hostId, List<Pe> peList) {
        long hostStorage = 10000000; // CHANGE THIS SO USERS CAN DEFINE THE HOST STORAGE
        int bandwidth = 10000; /// CHANGE THIS SO USERS CAN DEFINE THE BANDWIDTH

        return new PowerHost(
            hostId,
            new RamProvisionerSimple(config.getRam()),
            new BwProvisionerOverbooking(bandwidth),
            hostStorage,
            peList,
            new StreamOperatorScheduler(peList),
            new FogLinearPowerModel(config.getBusyPower(), config.getIdlePower())
        );
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
