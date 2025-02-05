package com.extensions.customfog;

import com.extensions.utils.presets.SensorPreset;
import com.extensions.vdcreation.models.Property;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.fog.application.AppEdge;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.Logger;

public class SensorProperty extends Sensor {
    private Property property;
    private SensorPreset preset;
    private String name;
    private String tupleType;

    public SensorProperty(String name, int userId, String appId, Property property, SensorPreset preset) {
        super(name, name, userId, appId, preset.DISTRIBUTION);
        this.name = name;
        this.tupleType = name;

        // Define sensor configs
        setLatency(preset.LATENCY);

        this.property = property;
        this.preset = preset;
    }

    @Override
    public String toString() {
        return "{" + "Sensor Name: " + name + " | Tuple Type: " + tupleType + " | Distribution: " + preset.DISTRIBUTION.getDistributionType() + " | Latency: " + preset.LATENCY + "}";
    }

    @Override
    public void transmit(){
        AppEdge _edge = null;
        for(AppEdge edge : getApp().getEdges()){
            if(edge.getSource().equals(getTupleType()))
                _edge = edge;
        }
        if (_edge == null) {
            System.out.println("AppEdge is null in SensorProperty! Ensure it is initialized correctly. Sensor: ");
        } else {
            //System.out.println("AppEdge is initialized correctly! Sensor: " + _edge.getDestination());
        }
        assert _edge != null;
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
