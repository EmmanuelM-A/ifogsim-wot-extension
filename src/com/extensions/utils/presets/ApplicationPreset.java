package com.extensions.utils.presets;

public enum ApplicationPreset {
    DEFAULT(
           10,
           0,
           0,
           10000,
           100
    );

    public final int APP_MODULE_RAM;
    public final int APP_MODULE_MIPS;
    public final int APP_MODULE_SIZE;
    public final int APP_EDGE_TUPLE_CPU_LENGTH;
    public final int APP_EDGE_TUPLE_NW_LENGTH;

    ApplicationPreset(
            int appModuleRam,
            int appModuleMips,
            int appModuleSize,
            int appEdgeTupleCpuLength,
            int appEdgeTupleNwLength
    ) {
        this.APP_MODULE_RAM = appModuleRam;
        this.APP_MODULE_MIPS = appModuleMips;
        this.APP_MODULE_SIZE = appModuleSize;
        this.APP_EDGE_TUPLE_CPU_LENGTH = appEdgeTupleCpuLength;
        this.APP_EDGE_TUPLE_NW_LENGTH = appEdgeTupleNwLength;
    }
}
