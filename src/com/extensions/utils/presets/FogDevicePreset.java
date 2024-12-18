package com.extensions.utils.presets;

public enum FogDevicePreset {
    DEFAULT(
            0, 0, 0, 0, 0, 0, 0, 0
    ),

    ASU_PERFECT(
            0, 0, 0, 0, 0, 0, 0, 0
    ),

    CONSTRAINED(
            0, 0,0,0, 0, 0, 0, 0
    );

    public final int CPU;
    public final int MEMORY;
    public final long STORAGE;
    public final double SCHEDULING_INTERVAL;
    public final double UPLINK_BW;
    public final double DOWNLINK_BW;
    public final double UPLINK_LATENCY;
    public final double RATE_PER_MIPS;

    FogDevicePreset(int cpu, int memory, long storage, long schedulingInterval, double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency, double ratePerMips) {
        this.CPU = cpu;
        this.MEMORY = memory;
        this.STORAGE = storage;
        this.SCHEDULING_INTERVAL = schedulingInterval;
        this.UPLINK_BW = uplinkBandwidth;
        this.DOWNLINK_BW = downlinkBandwidth;
        this.UPLINK_LATENCY = uplinkLatency;
        this.RATE_PER_MIPS = ratePerMips;
    }
}
