package com.extensions.customfog;

import com.extensions.utils.presets.CharacteristicsPreset;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.vdcreation.core.VirtualDeviceConfig;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FogDeviceFactory {
    /**
     * Creates a vanilla FogDevice in the simulation with the specified attributes.
     *
     * @param name the name of the FogDevice
     * @param mips the Millions of Instructions Per Second (MIPS) capacity of the FogDevice
     * @param uplinkBandwidth the uplink bandwidth (in Mbps) of the FogDevice for communication with its parent
     * @param downlinkBandwidth the down link bandwidth (in Mbps) of the FogDevice for communication with its children
     * @param latency the network latency (in milliseconds) between the FogDevice and its parent
     * @param ratePerMips the cost rate (in currency units) per MIPS for executing tasks on this FogDevice
     * @return the created FogDevice instance with the specified configuration
     */
    public static FogDevice createFogDevice(String name, int mips, double uplinkBandwidth, double downlinkBandwidth, double latency, double ratePerMips) {
        // Create a list of Processing Elements (PEs) with the specified MIPS value from the preset
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerOverbooking(mips)));

        // Generate a unique host ID and define host properties
        int hostId = FogUtils.generateEntityId();

        // The host memory in MB
        int hostRam = 2048;

        // The storage capacity in bytes
        long hostStorage = 10000000;

        // The bandwidth of the host
        int hostBandwidth = 10000;

        int hostBusyPower = 100;

        int hostIdlePower = 40;

        // Create a PowerHost instance with the given specifications
        PowerHost host = new PowerHost(
                hostId,
                new RamProvisionerSimple(hostRam),
                new BwProvisionerOverbooking(hostBandwidth),
                hostStorage,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(hostBusyPower, hostIdlePower)
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

        FogDevice fogdevice = null;
        try {
            fogdevice = new FogDevice(
                    name,
                    characteristics,
                    new AppModuleAllocationPolicy(hostList),
                    storageList,
                    10,
                    uplinkBandwidth,
                    downlinkBandwidth,
                    latency,
                    ratePerMips);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fogdevice;
    }

    /**
     * Creates a FogDevice instance using the specified name and preset configuration.
     *
     * @param name   The name of the FogDevice to be created.
     * @param preset The preset configuration containing the default values for the device.
     * @return The created FogDevice object, or null if an exception occurs.
     */
    public static FogDevice createFogDevice(String name, FogDevicePreset preset) {
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
    public static FogDevice createFogDevice(String name, FogDevicePreset preset, VirtualDeviceConfig config) {
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
}
