package com.extensions.vdcreation.validation;

import com.extensions.vdcreation.core.VirtualDevice;
import com.extensions.vdcreation.models.Action;
import com.extensions.vdcreation.models.Property;
import org.fog.entities.Sensor;

import java.util.List;
import java.util.Map;

public class CorrectMappingValidator extends Validator {
    public CorrectMappingValidator(VirtualDevice virtualDevice) {super(virtualDevice);}

    @Override
    public boolean validate() {
        /*for(VirtualDevice virtualDevice : virtualDevices) {
            // Validate property mappings
            Map<String, Property> thingDescriptionProperties = virtualDevice.getThingDescription().getProperties();

            for (String property : thingDescriptionProperties.keySet()) {
                if(virtualDevice.getSensorProperty(property) == null || !virtualDevice.getSensorProperty(property).getName().equals(property)) {
                    //System.err.println("Error: TD property '" + property + "' is not mapped to VD sensor properties.");
                    return false;
                }
            }

            // Validate action mappings
            Map<String, Action> thingDescriptionActions = virtualDevice.getThingDescription().getActions();

            for (String action : thingDescriptionActions.keySet()) {
                if (virtualDevice.getActuatorAction(action) == null || !virtualDevice.getActuatorAction(action).getName().equals(action)) {
                    //System.err.println("Error: TD action '" + action + "' is not mapped to VD actuator actions.");
                    return false;
                }
            }
        }*/
        return true;
    }
}
