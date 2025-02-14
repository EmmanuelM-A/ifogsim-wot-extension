package com.extensions.customfog;

import com.extensions.utils.presets.SensorPreset;
import com.extensions.vdcreation.models.Event;
import com.extensions.vdcreation.models.Property;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.fog.application.AppEdge;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.Logger;

public class CustomSensor extends Sensor {
    private final SensorPreset preset;
    private Property property;

    public CustomSensor(String name, int userId, String appId, SensorPreset preset) {
        super(name, name, userId, appId, preset.DISTRIBUTION);
        setLatency(preset.LATENCY);
        this.preset = preset;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    @Override
    public String toString() {
        return "{" + "Sensor Name: " + getName() + " | Tuple Type: " + getTupleType() + " | Distribution: " + preset.DISTRIBUTION.getDistributionType() + " | Latency: " + preset.LATENCY + "}";
    }

    @Override
    public void transmit(){
        AppEdge _edge = null;
        for(AppEdge edge : getApp().getEdges()){
            if(edge.getSource().equals(getTupleType()))
                _edge = edge;
        }

        if(_edge != null) {
            long cpuLength = (long) _edge.getTupleCpuLength();
            long nwLength = (long) _edge.getTupleNwLength();

            Tuple tuple = new Tuple(getAppId(), FogUtils.generateTupleId(), Tuple.UP, cpuLength, 1, nwLength, getOutputSize(),
                    new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
            tuple.setUserId(getUserId());
            tuple.setTupleType(getTupleType());

            tuple.setDestModuleName(_edge.getDestination());
            tuple.setSrcModuleName(getSensorName());
            Logger.debug(getName(), "Sending tuple with tupleId = "+tuple.getCloudletId());

            tuple.setDestinationDeviceId(getGatewayDeviceId());

            int actualTupleId = updateTimings(getSensorName(), tuple.getDestModuleName());
            tuple.setActualTupleId(actualTupleId);

            send(getGatewayDeviceId(), getLatency(), FogEvents.TUPLE_ARRIVAL,tuple);
        }
    }
}
