package com.extensions.sysconstructor.eventdriver;

import com.extensions.utils.FogEntityPrefixes;
import org.cloudbus.cloudsim.UtilizationModel;
import org.fog.entities.Tuple;
import java.util.List;

public class EventTuple extends Tuple {
    private final String eventType;
    private final List<String> destinations;
    private final double eventTimestamp;

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
            String eventType,
            List<String> destinations,
            double eventTimestamp
    ) {
        super(appId, cloudletId, direction, cloudletLength, pesNumber,
                cloudletFileSize, cloudletOutputSize,
                utilizationModelCpu, utilizationModelRam, utilizationModelBw);

        this.eventType = eventType;
        this.destinations = destinations;
        this.eventTimestamp = eventTimestamp;

        // Set Tuple Type as an Event Tuple
        this.setTupleType(FogEntityPrefixes.EVENT_TUPLE_PREFIX + eventType);
    }

    public String getEventType() {
        return eventType;
    }

    public List<String> getDestinations() {
        return destinations;
    }

    public double getEventTimestamp() {
        return eventTimestamp;
    }
}

