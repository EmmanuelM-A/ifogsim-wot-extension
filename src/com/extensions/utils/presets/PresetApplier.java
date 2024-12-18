package com.extensions.utils.presets;

import org.fog.entities.FogDevice;

/**
 * This class provides a blueprint for defining configurations (presets) for FogDevices, such as CPU power, memory,
 * storage, bandwidth and latency.
 */
public interface PresetApplier {
    /**
     * Applies the preset configuration to a {@link FogDevice}.
     * @param fogDevice The {@link FogDevice} object to which this preset will be applied.
     */
    void apply(FogDevice fogDevice);
}
