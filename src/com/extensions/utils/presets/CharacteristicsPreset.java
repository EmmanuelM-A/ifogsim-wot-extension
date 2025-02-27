package com.extensions.utils.presets;

/**
 * Defines presets for FogDeviceCharacteristics in the iFogSim simulation.
 * These presets provide common configurations for fog device characteristics like architecture, OS, and VM type.
 */
public class CharacteristicsPreset {
    /**
     * The hardware architecture of the system.
     */
    public static final String SYS_ARCH = "x86";

    /**
     * The Operating System running on the device.
     */
    public static final String OS = "Linux";

    /**
     * The virtualization layer.
     */
    public static final String VMM = "Xen";

    /**
     * The geographic time zone of the FogDevice
     */
    public static final double TIME_ZONE = 10.0;

    /**
     * The cost of using processing in the resource.
     */
    public static final double COST = 3.0;

    /**
     * The cost of using memory in this resource.
     */
    public static final double COST_PER_MEMORY = 0.05;

    /**
     * The cost of using storage in this resource.
     */
    public static final double COST_PER_STORAGE = 0.001;

    /**
     * The cost of using bandwidth in this resource.
     */
    public static final double COST_PER_BW = 0.0;


}
