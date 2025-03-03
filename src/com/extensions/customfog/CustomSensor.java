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
}
