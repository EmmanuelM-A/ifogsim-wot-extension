package com.extensions.utils.presets;

public enum ApplicationPreset {
    DEFAULT(
           10,
           0,
           0
    );

    public final int APP_MODULE_RAM;
    public final int APP_MODULE_MIPS;
    public final int APP_MODULE_SIZE;

    ApplicationPreset(
            int appModuleRam,
            int appModuleMips,
            int appModuleSize
    ) {
        this.APP_MODULE_RAM = appModuleRam;
        this.APP_MODULE_MIPS = appModuleMips;
        this.APP_MODULE_SIZE = appModuleSize;
    }
}
