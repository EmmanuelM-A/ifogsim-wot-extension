package com.extensions.vdcreation.validation;

import com.extensions.vdcreation.core.VirtualDevice;

import java.util.List;

/**
 * Responsible for checking if the virtual device (VD) contains the correctly mapped information of its corresponding
 * thing description (TD).
 */
public class FunctionalValidator extends Validator {
    public FunctionalValidator(List<VirtualDevice> virtualDevice) {
        super(virtualDevice);
    }

    @Override
    public boolean validate() {
        return false;
    }
}
