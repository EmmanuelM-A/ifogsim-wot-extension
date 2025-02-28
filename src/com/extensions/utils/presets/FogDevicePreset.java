package com.extensions.utils.presets;

/**
 * Defines presets for fog device configurations in the iFogSim simulation.
 * These presets provide common configurations for fog device resources and network parameters.
 */
public enum FogDevicePreset {
    /**
     * The default fog device preset with typical resource and network parameters.
     */
    DEFAULT(
            1000,
            4096,
            100000,
            10,
            2000,
            1000,
            3,
            0.05,
            70.0,
            20.0
    ),

    /**
     * A fog device preset that simulates a high-performing, realistic ideal fog device with ample resources and low latency.
     */
    ASSUMED_PERFECT(
            8000,
            32768,
            1000000,
            1,
            500000,
            500000,
            0,
            0.01,
            300.0,
            70.0
    ),

    /**
     * A fog device preset that simulates a constrained fog device with limited resources and higher latency.
     */
    CONSTRAINED(
            500,
            2048,
            40000,
            20,
            1000,
            500,
            10,
            0.1,
            30.0,
            15.0
    );

    /**
     * The processing power of the fog device in MIPS.
     */
    public final int MIPS;

    /**
     * The RAM capacity of the fog device in MB.
     */
    public final int RAM;

    /**
     * The storage capacity of the fog device in MB.
     */
    public final long STORAGE;

    /**
     * The scheduling interval of the fog device in milliseconds.
     */
    public final double SCHEDULING_INTERVAL;

    /**
     * The uplink bandwidth of the fog device in KBps.
     */
    public final double UPLINK_BW;

    /**
     * The down link bandwidth of the fog device in KBps.
     */
    public final double DOWN_LINK_BW;

    /**
     * The uplink latency of the fog device in milliseconds.
     */
    public final double UPLINK_LATENCY;

    /**
     * The cost per MIPS of the fog device.
     */
    public final double RATE_PER_MIPS;

    /**
     * The power consumption of the fog device when busy in Watts.
     */
    public final double BUSY_POWER;

    /**
     * The power consumption of the fog device when idle in Watts.
     */
    public final double IDLE_POWER;

    /**
     * Constructs a FogDevicePreset with the specified parameters.
     *
     * @param mips              The processing power of the fog device.
     * @param ram               The RAM capacity of the fog device.
     * @param storage           The storage capacity of the fog device.
     * @param schedulingInterval The scheduling interval of the fog device.
     * @param uplinkBandwidth   The uplink bandwidth of the fog device.
     * @param downLinkBandwidth The down link bandwidth of the fog device.
     * @param uplinkLatency     The uplink latency of the fog device.
     * @param ratePerMips       The cost per MIPS of the fog device.
     * @param busyPower         The power consumption of the fog device when busy.
     * @param idlePower         The power consumption of the fog device when idle.
     */
    FogDevicePreset(
            int mips, int ram, long storage, long schedulingInterval, double uplinkBandwidth, double downLinkBandwidth,
            double uplinkLatency, double ratePerMips,
            double busyPower, double idlePower
    ) {
        this.MIPS = mips;
        this.RAM = ram;
        this.STORAGE = storage;
        this.SCHEDULING_INTERVAL = schedulingInterval;
        this.UPLINK_BW = uplinkBandwidth;
        this.DOWN_LINK_BW = downLinkBandwidth;
        this.UPLINK_LATENCY = uplinkLatency;
        this.RATE_PER_MIPS = ratePerMips;
        this.BUSY_POWER = busyPower;
        this.IDLE_POWER = idlePower;
    }
}
