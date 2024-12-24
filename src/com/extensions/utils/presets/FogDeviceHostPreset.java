package com.extensions.utils.presets;

public enum FogDeviceHostPreset {
    DEFAULT(
            2048,
            1000000,
            10000,
            100,
            40
    );

    public final int HOST_RAM;
    public final long HOST_STORAGE;
    public final int HOST_BANDWIDTH;
    public final int HOST_BUSY_POWER;
    public final int HOST_IDLE_POWER;

    FogDeviceHostPreset(
            int hostRam,
            long hostStorage,
            int hostBandwidth,
            int hostBusyPower,
            int hostIdlePower
    ) {
        this.HOST_RAM = hostRam;
        this.HOST_STORAGE = hostStorage;
        this.HOST_BANDWIDTH = hostBandwidth;
        this.HOST_BUSY_POWER = hostBusyPower;
        this.HOST_IDLE_POWER = hostIdlePower;
    }


}
