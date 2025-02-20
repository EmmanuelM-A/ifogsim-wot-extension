package com.extensions.vdcreation.core;

import java.util.List;

/**
 * This class represents the configurations need to create a vanilla fog device.
 */
public record VirtualDeviceConfig(List<String> tags, long mips, int ram, long upBw, long downBw, int level,
                                  double ratePerMips, double busyPower, double idlePower) {
}
