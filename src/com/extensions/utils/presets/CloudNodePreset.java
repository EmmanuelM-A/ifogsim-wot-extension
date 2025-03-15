package com.extensions.utils.presets;

/**
 * Defines presets for cloud node configurations in the iFogSim simulation.
 * These presets provide common configurations for cloud node resources and network parameters.
 */
public enum CloudNodePreset {
    /**
     * The default cloud node preset with typical resource and network parameters.
     */
    DEFAULT(
            44800,
            40000,
            100,
            1000,
            2,
            0.1,
            1600,
            1320
    );

    /**
     * The processing power of the cloud node in MIPS.
     */
    public final int MIPS;

    /**
     * The RAM capacity of the cloud node in MB.
     */
    public final int RAM;

    /**
     * The uplink bandwidth of the cloud node in KBps.
     */
    public final double UPLINK_BW;

    /**
     * The down link bandwidth of the cloud node in KBps.
     */
    public final double DOWN_LINK_BW;

    /**
     * The latency of the cloud node in milliseconds.
     */
    public final double LATENCY;

    /**
     * The cost per MIPS of the cloud node.
     */
    public final double RATE_PER_MIPS;

    /**
     * The power consumption of the cloud node when busy in Watts.
     */
    public final double BUSY_POWER;

    /**
     * The power consumption of the cloud node when idle in Watts.
     */
    public final double IDLE_POWER;

    /**
     * Constructs a CloudNodePreset with the specified parameters.
     *
     * @param mips              The processing power of the cloud node.
     * @param ram               The RAM capacity of the cloud node.
     * @param uplinkBandwidth   The uplink bandwidth of the cloud node.
     * @param downLinkBandwidth The down link bandwidth of the cloud node.
     * @param latency           The latency of the cloud node.
     * @param ratePerMips       The cost per MIPS of the cloud node.
     * @param busyPower         The power consumption of the cloud node when busy.
     * @param idlePower         The power consumption of the cloud node when idle.
     */
    CloudNodePreset(
            int mips, int ram, double uplinkBandwidth, double downLinkBandwidth,
            double latency, double ratePerMips, double busyPower, double idlePower
    ) {
        this.MIPS = mips;
        this.RAM = ram;
        this.UPLINK_BW = uplinkBandwidth;
        this.DOWN_LINK_BW = downLinkBandwidth;
        this.LATENCY = latency;
        this.RATE_PER_MIPS = ratePerMips;
        this.BUSY_POWER = busyPower;
        this.IDLE_POWER = idlePower;
    }
}
