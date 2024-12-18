package com.extensions.utils.presets;

import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.fog.policy.AppModuleAllocationPolicy;

public enum FogDevicePreset {
    DEFAULT(
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            null,
            10000,
            124.34,
            85.49
    );

    /*ASU_PERFECT(
            0, 0, 0, 0, 0, 0, 0, 0
    ),

    CONSTRAINED(
            0, 0,0,0, 0, 0, 0, 0
    );*/

    public final int MIPS;
    public final int RAM;
    public final long STORAGE;
    public final double SCHEDULING_INTERVAL;
    public final double UPLINK_BW;
    public final double DOWNLINK_BW;
    public final double UPLINK_LATENCY;
    public final double RATE_PER_MIPS;
    public final VmAllocationPolicy VM_ALC_POLICY;
    public final long BANDWIDTH;
    public final double BUSY_POWER;
    public final double IDLE_POWER;

    FogDevicePreset(
            int mips, int ram, long storage, long schedulingInterval, double uplinkBandwidth, double downlinkBandwidth,
            double uplinkLatency, double ratePerMips, VmAllocationPolicy vmAllocationPolicy, long bandwidth,
            double busyPower, double idlePower
    ) {
        this.MIPS = mips;
        this.RAM = ram;
        this.STORAGE = storage;
        this.SCHEDULING_INTERVAL = schedulingInterval;
        this.UPLINK_BW = uplinkBandwidth;
        this.DOWNLINK_BW = downlinkBandwidth;
        this.UPLINK_LATENCY = uplinkLatency;
        this.RATE_PER_MIPS = ratePerMips;
        this.VM_ALC_POLICY = vmAllocationPolicy;
        this.BANDWIDTH = bandwidth;
        this.BUSY_POWER = busyPower;
        this.IDLE_POWER = idlePower;
    }
}
