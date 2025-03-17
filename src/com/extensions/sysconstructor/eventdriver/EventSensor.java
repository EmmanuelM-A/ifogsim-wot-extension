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

/**
 * Represents Web of Thing event mapped as {@code Sensor} entities.
 */
public class EventSensor extends CustomSensor {
    /**
     * The preset used to configure the event sensor.
     */
    private final SensorPreset preset;

    /**
     * The event associated with the event sensor.
     */
    private Event event;

    /**
     * Determines if the event was triggered or not.
     */
    private boolean eventTriggered;

    /**
     * Constructs an {@code EventSensor} instance.
     * @param name The name of the sensor.
     * @param userId The user ID associated with the application.
     * @param appId The application ID which the sensor is linked to.
     * @param preset The preset configurations used to configure the sensor.
     */
    public EventSensor(String name, int userId, String appId, SensorPreset preset) {
        super(name, userId, appId, preset);
        this.preset = preset;
        this.eventTriggered = false;
    }

    /**
     * Triggers the event, allowing data transmission.
     */
    public void triggerEvent() {
        this.eventTriggered = true;
    }

    /**
     * Transmits the event only when the event sensors has not been triggered before.
     */
    @Override
    public void transmit() {
        if (eventTriggered) {
            transmitEventTuple();
            eventTriggered = false; // Reset event trigger after transmission
            System.out.println("Event " + getName() + " triggered!");
        }
    }

    /**
     * Transmit the event tuple, similar to how {@code Sensor}'s transmit method works.
     */
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

    /**
     * Retrieves the current event.
     *
     * @return the current {@link Event}
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Sets a new event.
     *
     * @param event the {@link Event} to set
     */
    public void setEvent(Event event) {
        this.event = event;
    }
}
