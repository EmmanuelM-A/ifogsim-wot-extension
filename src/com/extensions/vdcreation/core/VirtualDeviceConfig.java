package com.extensions.vdcreation.core;

import java.util.List;

/**
 * This class represents the configurations need to create a vanilla fog device.
 */
public class VirtualDeviceConfig {
    private final List<String> tags;
    private final long mips;
    private final int ram;
    private final long upBw;
    private final long downBw;
    private final int level;
    private final double ratePerMips;
    private final double busyPower;
    private final double idlePower;

    public VirtualDeviceConfig(List<String> tags, long mips, int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower) {
        this.tags = tags;
        this.mips = mips;
        this.ram = ram;
        this.upBw = upBw;
        this.downBw = downBw;
        this.level = level;
        this.ratePerMips = ratePerMips;
        this.busyPower = busyPower;
        this.idlePower = idlePower;
    }

    public List<String> getTags() {
        return tags;
    }

    public long getMips() {
        return mips;
    }

    public int getRam() {
        return ram;
    }

    public long getUpBw() {
        return upBw;
    }

    public long getDownBw() {
        return downBw;
    }

    public int getLevel() {
        return level;
    }

    public double getRatePerMips() {
        return ratePerMips;
    }

    public double getBusyPower() {
        return busyPower;
    }

    public double getIdlePower() {
        return idlePower;
    }
}
