package com.extensions.vdcreation.validation;

import com.extensions.vdcreation.core.VirtualDevice;

import java.util.ArrayList;
import java.util.List;

public class VirtualDeviceValidationManager {
    private final static List<Validator> validators = new ArrayList<>();

    //private final ValidationReport report;

    public VirtualDeviceValidationManager(VirtualDevice virtualDevice) {
        //this.validators = new ArrayList<>();
        //this.report = new ValidationReport();

        // Add you VD validators here as needed
        validators.add(new FunctionalValidator(virtualDevice));
        //validators.add(new EventActionValidator(virtualDevices));
        //validators.add(new DataValidator(virtualDevices));
    }

    public boolean validateVirtualDevice() {
        for (Validator validator : validators) {
            if(!validator.validate()) return false;
        }
        return true;
    }

    public static boolean validate(VirtualDevice virtualDevice) {
        // Add you VD validators here as needed
        validators.add(new FunctionalValidator(virtualDevice));
        //validators.add(new EventActionValidator(virtualDevices));
        //validators.add(new DataValidator(virtualDevices));

        for (Validator validator : validators) {
            if(!validator.validate()) return false;
        }

        return true;
    }
}
