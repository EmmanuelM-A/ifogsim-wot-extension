package com.extensions.utils.presets;

import org.cloudbus.cloudsim.Vm;
import org.fog.entities.FogDevice;

import java.util.ArrayList;

/**
 * This class represents the "ASU_PERFECT" configuration for FogDevices, assuming near-perfect performance with maximum
 * available resources.
 */
public class AsuPerfectPreset implements PresetApplier {
    /**
     * Maximum CPU power
     */
    public static final int CPU = Integer.MAX_VALUE;

    public static final int MEMORY = Integer.MAX_VALUE;

    public static final long STORAGE = Long.MAX_VALUE;

    public static final double UPLINK_BW = 0.0;

    public static final double DOWNLINK_BW = 0.0;

    public static final double UPLINK_LATENCY = 0.0;

    public static final double RATE_PER_MIPS = 0.0;


    @Override
    public void apply(FogDevice fogDevice) {
        fogDevice.setUplinkBandwidth(UPLINK_BW);
        fogDevice.setDownlinkBandwidth(DOWNLINK_BW);
        fogDevice.setUplinkLatency(UPLINK_LATENCY);
        fogDevice.setRatePerMips(RATE_PER_MIPS);
    }
}
