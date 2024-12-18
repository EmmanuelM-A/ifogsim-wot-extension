package com.extensions.utils.presets;

import org.fog.entities.FogDeviceCharacteristics;

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
            new FogDeviceCharacteristics(
                    CharacteristicsPreset.SYS_ARCH,
                    CharacteristicsPreset.OS,
                    CharacteristicsPreset.VMM,
                    CharacteristicsPreset.HOST,
                    CharacteristicsPreset.TIME_ZONE,
                    CharacteristicsPreset.COST,
                    CharacteristicsPreset.COST_PER_MEMORY,
                    CharacteristicsPreset.COST_PER_STORAGE,
                    CharacteristicsPreset.COST_PER_BW
            )
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
    //public final VmAllocationPolicy VM_ALC_POLICY;
    public final FogDeviceCharacteristics FOG_CHARACTERISTICS;

    FogDevicePreset(
            int mips, int ram, long storage, long schedulingInterval, double uplinkBandwidth, double downlinkBandwidth,
            double uplinkLatency, double ratePerMips, /*VmAllocationPolicy vmAllocationPolicy,*/ FogDeviceCharacteristics characteristics
    ) {
        this.MIPS = mips;
        this.RAM = ram;
        this.STORAGE = storage;
        this.SCHEDULING_INTERVAL = schedulingInterval;
        this.UPLINK_BW = uplinkBandwidth;
        this.DOWNLINK_BW = downlinkBandwidth;
        this.UPLINK_LATENCY = uplinkLatency;
        this.RATE_PER_MIPS = ratePerMips;
        //this.VM_ALC_POLICY = vmAllocationPolicy;
        this.FOG_CHARACTERISTICS = characteristics;
    }
}
