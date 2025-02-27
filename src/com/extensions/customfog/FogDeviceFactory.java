package com.extensions.customfog;

import com.extensions.utils.presets.CharacteristicsPreset;
import com.extensions.utils.presets.FogDeviceHostPreset;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.vdcreation.core.VirtualDeviceConfig;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
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
    public static CustomFogDevice createFogDevice(
            String name,
            long mips,
            int ram,
            double uplinkBandwidth,
            double downlinkBandwidth,
            double latency,
            double ratePerMips,
            double busyPower,
            double idlePower
            ) {
        // Create a list of Processing Elements (PEs) with the specified MIPS value from the preset
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerOverbooking(mips)));

        // Generate a unique host ID and define host properties
        int hostId = FogUtils.generateEntityId();

        // Create a PowerHost instance with the given specifications
        PowerHost host = new PowerHost(
                hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerOverbooking(FogDeviceHostPreset.DEFAULT.HOST_BANDWIDTH),
                FogDeviceHostPreset.DEFAULT.HOST_STORAGE,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(busyPower, idlePower)
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

        CustomFogDevice fogdevice = null;
        String fogDeviceName = name.replace(" ", "");
        try {
            fogdevice = new CustomFogDevice(
                    fogDeviceName,
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
     * Creates a FogDevice instance using the specified name and preset configurations.
     *
     * @param name   The name of the FogDevice to be created.
     * @param preset The preset configuration containing the default values for the device.
     * @return The created FogDevice object, or null if an exception occurs.
     */
    public static CustomFogDevice createFogDevice(String name, FogDevicePreset preset) {
        // Create a list of Processing Elements (PEs) with the specified MIPS value from the preset
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerOverbooking(preset.MIPS)));

        // Generate a unique host ID and define host properties
        int hostId = FogUtils.generateEntityId();

        // Create a PowerHost instance with the given specifications
        PowerHost host = new PowerHost(
                hostId,
                new RamProvisionerSimple(preset.RAM),
                new BwProvisionerOverbooking(FogDeviceHostPreset.DEFAULT.HOST_BANDWIDTH),
                FogDeviceHostPreset.DEFAULT.HOST_STORAGE, // The storage capacity in bytes
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
        String fogDeviceName = name.replace(" ", "");
        try {
            return new CustomFogDevice(
                    fogDeviceName,
                    characteristics,
                    new AppModuleAllocationPolicy(hostList),
                    storageList,
                    preset.SCHEDULING_INTERVAL,
                    preset.UPLINK_BW,
                    preset.DOWN_LINK_BW,
                    preset.UPLINK_LATENCY,
                    preset.RATE_PER_MIPS
            );
        } catch(Exception e) {
            // Return null if the FogDevice could not be created
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a FogDevice instance using the specified name, preset configurations, and custom virtual device configuration.
     *
     * @param name The name of the FogDevice to be created.
     * @param preset The preset configuration containing default values for the device.
     * @param config The VirtualDeviceConfig providing custom values for specific device properties.
     * @return The created FogDevice object, or null if an exception occurs.
     */
    public static CustomFogDevice createFogDevice(String name, FogDevicePreset preset, VirtualDeviceConfig config) {
        // Create a list of Processing Elements (PEs) with the MIPS value from the virtual device configuration
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerOverbooking(config.mips())));

        // Generate a unique host ID and use it to create a PowerHost with the virtual device configuration
        int hostId = FogUtils.generateEntityId();
        PowerHost host = new PowerHost(
                hostId,
                new RamProvisionerSimple(config.ram()),
                new BwProvisionerOverbooking(FogDeviceHostPreset.DEFAULT.HOST_BANDWIDTH),
                FogDeviceHostPreset.DEFAULT.HOST_STORAGE,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(config.busyPower(), config.idlePower())
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

        CustomFogDevice fogDevice = null;
        String fogDeviceName = name.replace(" ", "");
        try {
            fogDevice = new CustomFogDevice(
                    fogDeviceName,
                    characteristics,
                    new AppModuleAllocationPolicy(hostList),
                    storageList,
                    preset.SCHEDULING_INTERVAL,
                    config.upBw(),
                    config.downBw(),
                    preset.UPLINK_LATENCY,
                    config.ratePerMips()
            );

            // Set the fog device's level in the hierarchy
            fogDevice.setLevel(config.level());

            // Return null if the FogDevice could not be created
            return fogDevice;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
