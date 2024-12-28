package com.extensions.vdcreation.validation;

import com.extensions.vdcreation.core.VirtualDevice;

import java.util.ArrayList;
import java.util.List;

public class VirtualDeviceValidationManager {
    private final List<Validator> validators;

    private final List<VirtualDevice> virtualDevices;

    public VirtualDeviceValidationManager(List<VirtualDevice> virtualDevices) {
        this.validators = new ArrayList<>();
        this.virtualDevices = virtualDevices;

        // Add you VD validators here as needed
        validators.add(new FunctionalValidator(virtualDevices));
        //validators.add(new EventActionValidator(virtualDevices));
        //validators.add(new DataValidator(virtualDevices));
    }

    public boolean validateVirtualDevices() {
        for (Validator validator : validators) {
            if(!validator.validate()) return false;
        }
        return true;
    }
}
