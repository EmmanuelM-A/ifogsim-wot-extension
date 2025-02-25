package com.extensions.vdcreation.core;

import com.extensions.customfog.CustomFogDevice;
import com.extensions.customfog.FogDeviceFactory;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.vdcreation.models.ThingDescription;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;

public class VD {
    private final CustomFogDevice fogDevice;
    private Sensor sensor;
    private Actuator actuator;
    /**
     * Represents the TD used to create this virtual device.
     */
    private ThingDescription thingDescription;

    public VD(String name, FogDevicePreset preset, VirtualDeviceConfig config) {
        this.fogDevice = FogDeviceFactory.createFogDevice(name, preset, config);
        this.sensor = null;
        this.actuator = null;
    }

    public VD(String name, FogDevicePreset preset) {
        this.fogDevice = FogDeviceFactory.createFogDevice(name, preset);
        this.sensor = null;
        this.actuator = null;
    }

    public CustomFogDevice getFogDevice() {
        return fogDevice;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public Actuator getActuator() {
        return actuator;
    }

    public void setActuator(Actuator actuator) {
        this.actuator = actuator;
    }

    public ThingDescription getThingDescription() {
        return thingDescription;
    }

    public void setThingDescription(ThingDescription thingDescription) {
        this.thingDescription = thingDescription;
    }
}
