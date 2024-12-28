package com.extensions.vdcreation.validation;

import com.extensions.vdcreation.core.VirtualDevice;

import java.util.ArrayList;
import java.util.List;

public class VirtualDeviceValidationManager {
    private final List<Validator> validators = new ArrayList<>();

    public VirtualDeviceValidationManager(List<VirtualDevice> virtualDevices) {
        // Add you VD validators here as needed
        validators.add(new FunctionalValidator(virtualDevices));
    }

    public boolean validateVirtualDevices() {
        for (Validator validator : validators) {
            if(!validator.validate()) return false;
        }
        return true;
    }
}
