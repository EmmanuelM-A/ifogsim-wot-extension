package com.extensions.vdcreation.core;

import com.extensions.customfog.ActuatorAction;
import com.extensions.customfog.SensorProperty;
import com.extensions.utils.presets.CharacteristicsPreset;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.vdcreation.EventTrigger;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    private Map<String, SensorProperty> properties;

    /**
     * Represents actions in the TD.
     */
    private Map<String, ActuatorAction> actions;

    /**
     * Represents events in the TD.
     */
    private Map<String, EventTrigger> events;

    public VirtualDevice(String name, FogDevicePreset preset) {
        this.fogDevice = createFogDevice(name, preset);
        this.properties = null;
        this.actions = null;
        this.events = null;
    }

    public FogDevice createFogDevice(String name, FogDevicePreset preset) {
        // Define the arguments for the FogDevice
        List<Pe> peList = new ArrayList<>();

        peList.add(new Pe(0, new PeProvisionerOverbooking(preset.MIPS)));

        int hostId = FogUtils.generateEntityId();
        long storage = 10000000;

        PowerHost host = new PowerHost(
            hostId,
            new RamProvisionerSimple(preset.RAM),
            new BwProvisionerOverbooking(preset.BANDWIDTH),
            storage,
            peList,
            new StreamOperatorScheduler(peList),
            new FogLinearPowerModel(preset.BUSY_POWER, preset.IDLE_POWER)
        );

        List<Host> hostList = new ArrayList<>();
        hostList.add(host);

        List<Storage> storageList = new LinkedList<>();

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

        // Instantiate an "empty" fog device with the passed in preset configurations
        FogDevice fogDevice = null;
        try {
            fogDevice = new FogDevice(
                    name,
                    characteristics,
                    new AppModuleAllocationPolicy(hostList), // CHANGE THIS LATER
                    storageList,
                    preset.SCHEDULING_INTERVAL,
                    preset.UPLINK_BW,
                    preset.DOWNLINK_BW,
                    preset.UPLINK_LATENCY,
                    preset.RATE_PER_MIPS
            );

            return fogDevice;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
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

    public void setProperties(Map<String, SensorProperty> properties) {
        this.properties = properties;
    }

    public Map<String, ActuatorAction> getActions() {
        return actions;
    }

    public void setActions(Map<String, ActuatorAction> actions) {
        this.actions = actions;
    }

    public Map<String, EventTrigger> getEvents() {
        return events;
    }

    public void setEvents(Map<String, EventTrigger> events) {
        this.events = events;
    }
}
