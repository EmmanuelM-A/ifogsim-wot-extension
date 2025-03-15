package com.extensions.customfog;

import com.extensions.utils.presets.SensorPreset;
import com.extensions.vdcreation.models.Property;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.Logger;
import org.fog.utils.TimeKeeper;

import java.util.ArrayList;

/**
 * Represents a custom sensor, extending the base {@code Sensor} class
 */
public class CustomSensor extends Sensor {
    /**
     * The sensor preset used to configure the sensor.
     */
    private final SensorPreset preset;

    /**
     * The property associated with the sensor (used for sensor properties).
     */
    private Property property;

    /**
     * Constructs a CustomSensor instance with the given parameters.
     * @param name The name of the sensor.
     * @param userId The user ID associated with the application.
     * @param appId The application ID which the sensor is linked to.
     * @param preset The preset configurations used to configure the sensor.
     */
    public CustomSensor(String name, int userId, String appId, SensorPreset preset) {
        super(name, name, userId, appId, preset.DISTRIBUTION);
        setLatency(preset.LATENCY);
        this.preset = preset;
    }

    public CustomSensor(String name, String tupleType, int userId, String appId, SensorPreset preset) {
        super(name, tupleType, userId, appId, preset.DISTRIBUTION);
        setLatency(preset.LATENCY);
        this.preset = preset;
    }

    /**
     * Retrieves the property associated to the sensor.
     *
     * @return The property read by the sensor.
     */
    public Property getProperty() {
        return property;
    }

    /**
     * Sets the property value.
     *
     * @param property The property value of the sensor.
     */
    public void setProperty(Property property) {
        this.property = property;
    }

    /**
     * Returns a string representation of the sensor, displaying all the necessary
     * information about the actuator.
     * @return A formatted string containing the sensor's details.
     */
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

        if(_edge != null) { // Ignores edges that have not been defined properly
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

    @Override
    protected int updateTimings(String src, String dest){
        Application application = getApp();
        for(AppLoop loop : application.getLoops()){
            if(loop.hasEdge(src, dest)){
                //System.out.println("From Sensor: " + src + " ---> " + dest);

                int tupleId = TimeKeeper.getInstance().getUniqueId();
                if(!TimeKeeper.getInstance().getLoopIdToTupleIds().containsKey(loop.getLoopId()))
                    TimeKeeper.getInstance().getLoopIdToTupleIds().put(loop.getLoopId(), new ArrayList<Integer>());
                TimeKeeper.getInstance().getLoopIdToTupleIds().get(loop.getLoopId()).add(tupleId);
                TimeKeeper.getInstance().getEmitTimes().put(tupleId, CloudSim.clock());
                return tupleId;
            } else {
                //System.out.println("Error Occurred - From Sensor: " + src + " ---> " + dest);
            }
        }
        return -1;
    }
}
