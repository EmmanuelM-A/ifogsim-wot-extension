package com.extensions.vdcreation.validation;

import com.extensions.vdcreation.core.VirtualDevice;

import java.util.List;

public abstract class Validator {
    protected VirtualDevice virtualDevice;

    public Validator(VirtualDevice virtualDevice) {
        this.virtualDevice = virtualDevice;
    }

    public abstract boolean validate();
}
