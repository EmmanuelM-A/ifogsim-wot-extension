package com.extensions.utils.presets;

public enum EdgeNodePreset {
    DEFAULT(
            2800,
            4000,
            1000,
            1000,
            2,
            0,
            1000,
            500
    );

    /*CUSTOM_ONE(

    );*/

    public final int MIPS;
    public final int RAM;
    public final double UPLINK_BW;
    public final double DOWN_LINK_BW;
    public final double LATENCY;
    public final double RATE_PER_MIPS;
    public final double BUSY_POWER;
    public final double IDLE_POWER;

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
