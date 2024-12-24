package com.extensions.vdcreation.core.validation;

import com.extensions.utils.ObjectValidator;
import com.extensions.vdcreation.core.VirtualDevice;

public abstract class Validator {
    protected VirtualDevice virtualDevice;

    public Validator(VirtualDevice virtualDevice) {
        this.virtualDevice = virtualDevice;
    }

    public abstract boolean valiadte();
}
