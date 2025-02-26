package com.extensions.sysconstructor.eventdriver;

import com.extensions.customfog.CustomSensor;
import com.extensions.utils.presets.SensorPreset;
import com.extensions.vdcreation.models.Event;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.fog.application.AppEdge;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.Logger;
import org.fog.utils.TimeKeeper;

/**
 *
 */
public class EventSensor extends CustomSensor {
    private final SensorPreset preset;
    private Event event;
    private boolean eventTriggered = false;

    public EventSensor(String name, int userId, String appId, SensorPreset preset) {
        super(name, userId, appId, preset);
        this.preset = preset;
    }

    /**
     * Triggers the event, allowing data transmission.
     */
    public void triggerEvent() {
        this.eventTriggered = true;
    }

    @Override
    public void transmit() {
        if (eventTriggered) {
            transmitEventTuple();
            eventTriggered = false; // Reset event trigger after transmission
            System.out.println("Event " + getName() + " transmitted!");
        }
    }

    private void transmitEventTuple() {
        AppEdge _edge = null;
        for(AppEdge edge : getApp().getEdges()){
            if(edge.getSource().equals(getTupleType()))
                _edge = edge;
        }

        if(_edge != null) {
            long cpuLength = (long) _edge.getTupleCpuLength();
            long nwLength = (long) _edge.getTupleNwLength();

            EventTuple eventTuple = new EventTuple(getAppId(), FogUtils.generateTupleId(), Tuple.UP, cpuLength, 1, nwLength, getOutputSize(),
                    new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull(), getName());

            eventTuple.setUserId(getUserId());
            eventTuple.setTupleType(getTupleType());

            eventTuple.setDestModuleName(_edge.getDestination());
            eventTuple.setSrcModuleName(getSensorName());
            Logger.debug(getName(), "Sending tuple with tupleId = "+eventTuple.getCloudletId());

            eventTuple.setDestinationDeviceId(getGatewayDeviceId());

            int actualTupleId = updateTimings(getSensorName(), eventTuple.getDestModuleName());
            eventTuple.setActualTupleId(actualTupleId);

            send(getGatewayDeviceId(), getLatency(), FogEvents.TUPLE_ARRIVAL,eventTuple);
        }
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
