package com.extensions.vdcreation.core;

import java.util.List;

/**
 * This class represents the configurations needed to create a vanilla fog device.
 * It includes various properties that define the computational resources and network
 * characteristics of the device.
 *
 * @param tags A list of tags that categorize the device, typically used for identifying
 *             the device's type or function.
 * @param mips The Million Instructions Per Second (MIPS) of the device's processing capability.
 * @param ram The amount of RAM (in MB) available for the device.
 * @param upBw The uplink bandwidth (in bits per second) of the device.
 * @param downBw The downLink bandwidth (in bits per second) of the device.
 * @param level The level or hierarchy of the device in a multi-layer fog architecture.
 * @param ratePerMips The rate of resource consumption (e.g., energy consumption) per MIPS.
 * @param busyPower The power consumption (in watts) when the device is under full load (busy).
 * @param idlePower The power consumption (in watts) when the device is idle.
 */
public record VirtualDeviceConfig(List<String> tags, long mips, int ram, long upBw, long downBw, int level,
                                  double ratePerMips, double busyPower, double idlePower) {
}

