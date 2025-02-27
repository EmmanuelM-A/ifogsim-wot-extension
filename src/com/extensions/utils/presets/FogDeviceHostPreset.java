package com.extensions.utils.presets;

/**
 * Defines presets for fog device host configurations in the iFogSim simulation.
 * These presets provide common configurations for host resources.
 */
public enum FogDeviceHostPreset {
    /**
     * The default fog device host preset with typical resource parameters.
     */
    DEFAULT(1000000, 10000),

    /**
     * A fog device host preset that simulates a high-performing host with ample resources.
     */
    ASSUMED_PERFECT(10000000, 500000),

    /**
     * A fog device host preset that simulates a constrained host with limited resources.
     */
    CONSTRAINED(100000, 5000);

    /**
     * The storage capacity of the host in MB.
     */
    public final long HOST_STORAGE;

    /**
     * The network bandwidth of the host in KBps.
     */
    public final int HOST_BANDWIDTH;

    /**
     * Constructs a FogDeviceHostPreset with the specified parameters.
     *
     * @param hostStorage   The storage capacity of the host in MB.
     * @param hostBandwidth The network bandwidth of the host in KBps.
     */
    FogDeviceHostPreset(
            long hostStorage,
            int hostBandwidth

    ) {
        this.HOST_STORAGE = hostStorage;
        this.HOST_BANDWIDTH = hostBandwidth;
    }
}
