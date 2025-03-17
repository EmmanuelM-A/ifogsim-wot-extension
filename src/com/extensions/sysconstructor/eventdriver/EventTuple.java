package com.extensions.sysconstructor.eventdriver;

import org.cloudbus.cloudsim.UtilizationModel;
import org.fog.entities.Tuple;

/**
 * The {@code EventTuple} class represents a specialized {@link Tuple} that carries an event type.
 * It extends the {@link Tuple} class and includes an additional field to specify the type of event.
 */
public class EventTuple extends Tuple {
    /**
     * The type of event associated with this tuple.
     */
    private final String eventType;

    /**
     * Constructs an {@code EventTuple} with the specified parameters.
     *
     * @param appId               the application ID
     * @param cloudletId          the cloudlet ID
     * @param direction           the direction of the tuple
     * @param cloudletLength      the length of the cloudlet
     * @param pesNumber           the number of processing elements (PEs) required
     * @param cloudletFileSize    the file size of the cloudlet
     * @param cloudletOutputSize  the output size of the cloudlet
     * @param utilizationModelCpu the CPU utilization model
     * @param utilizationModelRam the RAM utilization model
     * @param utilizationModelBw  the bandwidth utilization model
     * @param eventType           the type of event associated with this tuple
     */
    public EventTuple(
            String appId,
            int cloudletId,
            int direction,
            long cloudletLength,
            int pesNumber,
            long cloudletFileSize,
            long cloudletOutputSize,
            UtilizationModel utilizationModelCpu,
            UtilizationModel utilizationModelRam,
            UtilizationModel utilizationModelBw,
            String eventType
    ) {
        super(appId, cloudletId, direction, cloudletLength, pesNumber,
                cloudletFileSize, cloudletOutputSize,
                utilizationModelCpu, utilizationModelRam, utilizationModelBw);

        this.eventType = eventType;

        // Set Tuple Type as an Event Tuple
        this.setTupleType(eventType);
    }

    /**
     * Returns the type of event associated with this tuple.
     *
     * @return the event type
     */
    public String getEventType() {
        return eventType;
    }
}


