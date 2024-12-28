package com.extensions.vdcreation.validation;

import com.extensions.vdcreation.core.VirtualDevice;

import java.util.List;

public abstract class Validator {
    protected List<VirtualDevice> virtualDevices;

    public Validator(List<VirtualDevice> virtualDevices) {
        this.virtualDevices = virtualDevices;
    }

    public abstract boolean validate();
}
