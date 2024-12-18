package com.extensions.utils.presets;

import com.extensions.utils.Preset;
import org.fog.entities.FogDevice;

/**
 * This class represents the "ASU_PERFECT" configuration for FogDevices, assuming near-perfect performance with maximum
 * available resources.
 */
public class AsuPerfectPreset {
    public void applyPresetTo(Preset preset, FogDevice fogDevice) {
        fogDevice.setUplinkBandwidth(preset.UPLINK_BW);
        fogDevice.setDownlinkBandwidth(preset.DOWNLINK_BW);
        fogDevice.setUplinkLatency(preset.UPLINK_LATENCY);
        fogDevice.setRatePerMips(preset.RATE_PER_MIPS);
    }
}
