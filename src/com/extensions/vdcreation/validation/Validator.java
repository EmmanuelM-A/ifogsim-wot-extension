package com.extensions.vdcreation.validation;

import com.extensions.vdcreation.core.VirtualDevice;

import java.util.List;

public abstract class Validator {
    protected List<VirtualDevice> virtualDevice;

    public Validator(List<VirtualDevice> virtualDevice) {
        this.virtualDevice = virtualDevice;
    }

    public abstract boolean validate();
}
