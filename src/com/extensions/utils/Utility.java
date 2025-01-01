package com.extensions.utils;

import com.extensions.vdcreation.core.VirtualDevice;

import java.util.List;

public class Utility {
    public static VirtualDevice getVirtualDevice(List<VirtualDevice> virtualDevices, String name) {
        for(VirtualDevice virtualDevice : virtualDevices) {
            if(virtualDevice.getFogDevice().getName().equals(name)) return virtualDevice;
        }
        return null;
    }
}
