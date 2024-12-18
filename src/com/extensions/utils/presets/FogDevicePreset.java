package com.extensions.utils.presets;

import org.fog.entities.FogDevice;

/**
 * This class provides a blueprint for defining configurations (presets) for FogDevices, such as CPU power, memory,
 * storage, bandwidth and latency.
 */
public abstract class FogDevicePreset {
    /**
     * Processing power in Millions Instructions Per Second (MIPS).
     */
    protected int cpu;

    /**
     * Random Access Memory (RAM) in Megabytes (MB).
     */
    protected int memory;

    /**
     * Storage space in Megabytes (MB).
     */
    protected long storage;

    /**
     * Network bandwidth in Megabytes per second (MBps)
     */
    protected double bandwidth;

    /**
     * Network latency in milliseconds (ms)
     */
    protected double latency;

    /**
     * Applies the preset configuration to a {@link FogDevice}.
     * @param fogDevice The {@link FogDevice} object to which this preset will be applied.
     */
    public abstract void apply(FogDevice fogDevice);
}
