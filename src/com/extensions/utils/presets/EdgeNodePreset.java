package com.extensions.utils.presets;

/**
 * Defines presets for edge node configurations in the iFogSim simulation.
 * These presets provide common configurations for edge node resources and network parameters.
 */
public enum EdgeNodePreset {
    /**
     * The default edge node preset with typical resource and network parameters.
     */
    DEFAULT(
            2800,
            4096,
            50000,
            50000,
            2,
            0.01,
            50,
            20
    ),

    /**
     * An edge node preset that simulates a high-performance edge node with ample resources and low latency.
     */
    ASSUMED_PERFECT(
            8000,
            32768,
            500000,
            500000,
            1,
            0.005,
            100,
            30
    ),

    /**
     * An edge node preset that simulates a constrained edge node with limited resources and higher latency.
     */
    CONSTRAINED(
            1000,
            2048,
            10000,
            10000,
            10,
            0.05,
            20,
            10
    ),

    DCNSFog(
          2800,
          4000,
          10000,
          10000,
          2,
          0.0,
          107.339,
          83.43333
    );

    /**
     * The processing power of the edge node in MIPS.
     */
    public final int MIPS;

    /**
     * The RAM capacity of the edge node in MB.
     */
    public final int RAM;

    /**
     * The uplink bandwidth of the edge node in KBps.
     */
    public final double UPLINK_BW;

    /**
     * The down link bandwidth of the edge node in KBps.
     */
    public final double DOWN_LINK_BW;

    /**
     * The latency of the edge node in milliseconds.
     */
    public final double LATENCY;

    /**
     * The cost per MIPS of the edge node.
     */
    public final double RATE_PER_MIPS;

    /**
     * The power consumption of the edge node when busy in Watts.
     */
    public final double BUSY_POWER;

    /**
     * The power consumption of the edge node when idle in Watts.
     */
    public final double IDLE_POWER;

    /**
     * Constructs an EdgeNodePreset with the specified parameters.
     *
     * @param mips              The processing power of the edge node.
     * @param ram               The RAM capacity of the edge node.
     * @param uplinkBandwidth   The uplink bandwidth of the edge node.
     * @param downLinkBandwidth The down link bandwidth of the edge node.
     * @param latency           The latency of the edge node.
     * @param ratePerMips       The cost per MIPS of the edge node.
     * @param busyPower         The power consumption of the edge node when busy.
     * @param idlePower         The power consumption of the edge node when idle.
     */
    EdgeNodePreset(
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
