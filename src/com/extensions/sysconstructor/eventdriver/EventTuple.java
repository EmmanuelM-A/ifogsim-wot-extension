package com.extensions.sysconstructor.eventdriver;

import com.extensions.utils.FogEntityPrefixes;
import org.cloudbus.cloudsim.UtilizationModel;
import org.fog.entities.Tuple;
import java.util.List;

public class EventTuple extends Tuple {
    private final String eventType;

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
        this.setTupleType(FogEntityPrefixes.EVENT_TUPLE_PREFIX + eventType);
    }

    public String getEventType() {
        return eventType;
    }
}

