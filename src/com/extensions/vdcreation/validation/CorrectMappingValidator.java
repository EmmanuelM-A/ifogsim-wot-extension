package com.extensions.vdcreation.validation;

import com.extensions.vdcreation.core.VirtualDevice;
import com.extensions.vdcreation.models.Property;
import org.fog.entities.Sensor;

import java.util.List;
import java.util.Map;

public class CorrectMappingValidator extends Validator {
    public CorrectMappingValidator(List<VirtualDevice> virtualDevices) {super(virtualDevices);}

    @Override
    public boolean validate() {
        for(VirtualDevice virtualDevice : virtualDevices) {
            Map<String, Property> thingDescriptionProperties = virtualDevice.getThingDescription().getProperties();
            List<Sensor> virtualDeviceSensorProperties = virtualDevice.getSensorProperties();

            for (String property : thingDescriptionProperties.keySet()) {
                if (!virtualDeviceSensorProperties.contains(property)) {
                    System.err.println("Error: TD property '" + property + "' is not mapped to VD sensor properties.");
                }

                if(virtualDevice.getSensorProperty(property) == null || !virtualDevice.getSensorProperty(property).getName().equals(property)) {

                }
            }

            // Validate actions
            /*Map<String, Object> tdActions = virtualDevice.getThingDescription().getActions();
            List<String> vdActuatorActions = virtualDevice.getActuatorActions();

            for (String action : tdActions.keySet()) {
                if (!vdActuatorActions.contains(action)) {
                    System.err.println("Error: TD action '" + action + "' is not mapped to VD actuator actions.");
                }
            }*/
        }
        return false;
    }
}

/*
 * VD Validation Report:
 * Map<Validator, Boolean> {
 *      CorrectMapping - Success,
 *      Functional - Success,
 *      Data - Fail,
 *      ... - ...,
 * }
 * sucessRate: 5 / No. validators
 *
 */
