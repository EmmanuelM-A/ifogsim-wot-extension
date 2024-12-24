package com.extensions.vdcreation.core.validation;

import com.extensions.utils.ObjectValidator;
import com.extensions.vdcreation.core.VirtualDevice;

import java.util.List;

public abstract class Validator {
    protected List<VirtualDevice> virtualDevice;

    public Validator(List<VirtualDevice> virtualDevice) {
        this.virtualDevice = virtualDevice;
    }

    public abstract boolean validate();
}
