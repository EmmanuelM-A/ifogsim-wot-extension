package com.extensions.customfog;

import com.extensions.utils.presets.ActuatorPreset;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;
import org.fog.utils.Logger;
import org.fog.utils.TimeKeeper;

/**
 * Represents a custom actuator extending the base Actuator class.
 */
public class CustomActuator extends Actuator {
    /**
     * The preset configurations used to configure this actuator.
     */
    private final ActuatorPreset preset;

    /**
     * The name of the actuator.
     */
    private String name;

    /**
     * The type of the actuator.
     */
    private final String actuatorType;

    /**
     * Constructs a {@code CustomActuator} instance.
     * @param name The name of the actuator.
     * @param userId The user ID associated with the application.
     * @param appId The application ID the actuator is linked to.
     * @param preset The preset used to configure ths actuator.
     */
    public CustomActuator(String name, int userId, String appId, ActuatorPreset preset) {
        super(name, userId, appId, name);
        this.name = name;
        this.actuatorType = name;
        this.preset = preset;
    }

    public CustomActuator(String name, int userId, String appId, ActuatorPreset preset, String actuatorType) {
        super(name, userId, appId, actuatorType);
        this.actuatorType = actuatorType;
        this.preset = preset;
    }

    /**
     * Retrieves the name of the actuator.
     *
     * @return The name of the actuator.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the actuator.
     * @param name The new name of the actuator.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a string representation of the actuator, displaying all the necessary
     * information about the actuator.
     * @return A formatted string containing the actuator's details.
     */
    @Override
    public String toString() {
        return "{" + "Sensor Name: " + name + " | Actuator Type: " + actuatorType + " | Latency: " + preset.LATENCY + "}";
    }

    @Override
    public void processEvent(SimEvent ev) {
        switch(ev.getTag()){
            case FogEvents.TUPLE_ARRIVAL:
                processTupleArrival(ev);
                break;
        }
    }

    private void processTupleArrival(SimEvent ev) {
        Tuple tuple = (Tuple)ev.getData();
        Logger.debug(getName(), "Received tuple "+tuple.getCloudletId()+"on "+tuple.getDestModuleName());
        String srcModule = tuple.getSrcModuleName();
        String destModule = tuple.getDestModuleName();
        Application app = getApp();

        for(AppLoop loop : app.getLoops()){
            if(loop.hasEdge(srcModule, destModule) && loop.isEndModule(destModule)){
                //System.out.println("Actuator module connection: " + srcModule + " ---> " + destModule);

                Double startTime = TimeKeeper.getInstance().getEmitTimes().get(tuple.getActualTupleId());
                if(startTime==null) {
                    break;
                }
                if(!TimeKeeper.getInstance().getLoopIdToCurrentAverage().containsKey(loop.getLoopId())){
                    TimeKeeper.getInstance().getLoopIdToCurrentAverage().put(loop.getLoopId(), 0.0);
                    TimeKeeper.getInstance().getLoopIdToCurrentNum().put(loop.getLoopId(), 0);
                }
                double currentAverage = TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loop.getLoopId());
                int currentCount = TimeKeeper.getInstance().getLoopIdToCurrentNum().get(loop.getLoopId());
                double delay = CloudSim.clock()- TimeKeeper.getInstance().getEmitTimes().get(tuple.getActualTupleId());
                TimeKeeper.getInstance().getEmitTimes().remove(tuple.getActualTupleId());
                double newAverage = (currentAverage*currentCount + delay)/(currentCount+1);
                TimeKeeper.getInstance().getLoopIdToCurrentAverage().put(loop.getLoopId(), newAverage);
                TimeKeeper.getInstance().getLoopIdToCurrentNum().put(loop.getLoopId(), currentCount+1);
                break;
            } else {
                //System.out.println("Actuator module connection does not exist: " + srcModule + " ---> " + destModule);
            }
        }
    }
}
