package com.extensions.vdcreation.core.validation;

import com.extensions.vdcreation.core.VirtualDevice;
import com.extensions.vdcreation.models.ThingDescription;

/**
 * Responsible for checking if the virtual device (VD) contains the correctly mapped information of its corresponding
 * thing description (TD).
 */
public class FunctionalValidator extends Validator {
    private ThingDescription thingDescription;
    public FunctionalValidator(VirtualDevice virtualDevice) {
        super(virtualDevice);
    }

    @Override
    public boolean valiadte() {
        return false;
    }

    /*
     *  1) Verify the VD correctly maps the TD to a simulated physical entity
     *  - Compare VD sensor property names with the TD property name
     *
     *  2) Check that
     */
}
