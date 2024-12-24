package com.extensions.vdcreation.core;

import com.extensions.vdcreation.core.validation.FunctionalValidator;
import com.extensions.vdcreation.core.validation.Validator;

import java.util.ArrayList;
import java.util.List;

public class VirtualDeviceValidationManager {
    private final List<Validator> validators = new ArrayList<>();

    public VirtualDeviceValidationManager(VirtualDevice virtualDevice) {
        validators.add(new FunctionalValidator(virtualDevice));
    }

    public boolean valiadteVirtualDevice() {
        for (Validator validator : validators) {
            if(!validator.valiadte()) return false;
        }

        return true;
    }
}
