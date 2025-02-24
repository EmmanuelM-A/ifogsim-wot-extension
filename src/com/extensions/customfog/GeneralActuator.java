package com.extensions.customfog;

import com.extensions.utils.presets.ActuatorPreset;
import com.extensions.vdcreation.core.VirtualDeviceFactory;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.entities.Actuator;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;

import java.util.List;

public class GeneralActuator extends ActuatorAction {
    private final ActuatorPreset preset;
    private List<ActuatorAction> realActuators;
    private final int parentFogDeviceId;

    public GeneralActuator(String name, int userId, String appId, ActuatorPreset preset, int parentFogDeviceId) {
        super(name, userId, appId, null, preset);

        this.preset = preset;
        this.parentFogDeviceId = parentFogDeviceId;
    }

    @Override
    public void processEvent(SimEvent ev) {
        if (ev.getTag() == FogEvents.TUPLE_ARRIVAL) {
            Tuple tuple = (Tuple) ev.getData();
            System.out.println("GENERAL-ACTUATOR received: " + tuple.getTupleType());

            // Broadcast to all real actuators
            broadcastToOtherActuators(tuple);
        }
        super.processEvent(ev);
    }

    private void broadcastToOtherActuators(Tuple tuple) {
        for (ActuatorAction actuator : realActuators) {
            Tuple copiedTuple = makeCopy(tuple);

            if (!actuator.getName().equals(VirtualDeviceFactory.GENERAL_ACTUATOR)) {
                copiedTuple.setDestModuleName(actuator.getActuatorType());
                copiedTuple.setTupleType(actuator.getActuatorType());
                send(parentFogDeviceId, preset.LATENCY, FogEvents.TUPLE_ARRIVAL, copiedTuple);

                System.out.println("Broadcast tuple: " + copiedTuple.getTupleType() + " to " + actuator.getName());
            }
        }
    }

    private Tuple makeCopy(Tuple tuple) {
        return new Tuple(
                tuple.getAppId(),
                tuple.getCloudletId(),
                tuple.getDirection(),
                tuple.getCloudletLength(),
                tuple.getNumberOfPes(),
                tuple.getCloudletFileSize(),
                tuple.getCloudletOutputSize(),
                tuple.getUtilizationModelCpu(),
                tuple.getUtilizationModelRam(),
                tuple.getUtilizationModelBw()
        );
    }

    public List<ActuatorAction> getRealActuators() {
        return realActuators;
    }

    public void setRealActuators(List<ActuatorAction> realActuators) {
        this.realActuators = realActuators;
    }
}
