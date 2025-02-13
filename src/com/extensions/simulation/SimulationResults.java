package com.extensions.simulation;

import com.extensions.sysconstructor.eventdriver.EventDrivenApplication;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.entities.FogDevice;
import org.fog.utils.Config;
import org.fog.utils.NetworkUsageMonitor;
import org.fog.utils.TimeKeeper;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class stores and processes the results of an iFogSim simulation.
 */
public class SimulationResults {
    private static SimulationResults instance;
    private long executionTime;
    private Map<String, Double> applicationLoopDelays;
    private Map<String, Double> tupleExecutionDelays;
    private Map<String, Double> energyConsumptionPerDevice;
    private double totalNetworkUsage;
    private double cloudExecutionCost;
    private Map<String, Application> applications;

    /**
     * Constructor initializes and records all performance metrics from the simulation.
     * @param fogDevices The list of fog devices in the simulation.
     */
    public SimulationResults(List<FogDevice> fogDevices) {
        this.executionTime = Calendar.getInstance().getTimeInMillis() - TimeKeeper.getInstance().getSimulationStartTime();
        this.applicationLoopDelays = extractApplicationLoopDelays();
        this.tupleExecutionDelays = extractTupleExecutionDelays();
        this.energyConsumptionPerDevice = extractEnergyConsumption(fogDevices);
        this.totalNetworkUsage = NetworkUsageMonitor.getNetworkUsage() / Config.MAX_SIMULATION_TIME;
        this.cloudExecutionCost = extractCloudExecutionCost(fogDevices);
        this.applications = new HashMap<>();
    }

    public Map<String, Application> getApplications() {
        return applications;
    }

    public void setApplications(Map<String, Application> applications) {
        this.applications = applications;
    }

    /**
     * Extracts loop delays from TimeKeeper.
     */
    private Map<String, Double> extractApplicationLoopDelays() {
        Map<String, Double> loopDelays = new HashMap<>();
        for (Integer loopId : TimeKeeper.getInstance().getLoopIdToCurrentAverage().keySet()) {
            loopDelays.put("Loop-" + loopId, TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loopId));
        }
        return loopDelays;
    }

    /**
     * Extracts tuple execution delays from TimeKeeper.
     */
    private Map<String, Double> extractTupleExecutionDelays() {
        Map<String, Double> delays = new HashMap<>();
        for (String tupleType : TimeKeeper.getInstance().getTupleTypeToAverageCpuTime().keySet()) {
            delays.put(tupleType, TimeKeeper.getInstance().getTupleTypeToAverageCpuTime().get(tupleType));
        }
        return delays;
    }

    /**
     * Extracts energy consumption per fog device.
     */
    private Map<String, Double> extractEnergyConsumption(List<FogDevice> fogDevices) {
        Map<String, Double> energyConsumption = new HashMap<>();
        for (FogDevice fogDevice : fogDevices) {
            energyConsumption.put(fogDevice.getName(), fogDevice.getEnergyConsumption());
        }
        return energyConsumption;
    }

    /**
     * Extracts cloud execution cost from the cloud node.
     */
    private double extractCloudExecutionCost(List<FogDevice> fogDevices) {
        for (FogDevice device : fogDevices) {
            if (device.getName().equals("cloud")) {
                return device.getTotalCost();
            }
        }
        return 0;
    }

    private String getStringForLoopId(int loopId){
        for(String appId : getApplications().keySet()){
            Application app = getApplications().get(appId);
            for(AppLoop loop : app.getLoops()){
                if(loop.getLoopId() == loopId)
                    return loop.getModules().toString();
            }
        }
        return null;
    }

    /**
     * Prints out the simulation results.
     */
    public void printResults() {
        System.out.println("=========================================");
        System.out.println("           SIMULATION RESULTS            ");
        System.out.println("=========================================");
        System.out.println("Execution Time: " + executionTime + " ms");
        System.out.println("Total Network Usage: " + totalNetworkUsage);
        System.out.println("Cloud Execution Cost: " + cloudExecutionCost);

        // TODO REFORMAT THIS SO IT PRINTS OUT THE ITEMS IN THE LOOP AS WELL AS ITS DELAY
        System.out.println("\nApplication Loop Delays:");
        for(Integer loopId : TimeKeeper.getInstance().getLoopIdToTupleIds().keySet()){
            System.out.println(getStringForLoopId(loopId) + " ---> "+TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loopId));
        }

        System.out.println("\nTuple Execution Delays:");
        for (Map.Entry<String, Double> entry : tupleExecutionDelays.entrySet()) {
            System.out.println(entry.getKey() + " ---> " + entry.getValue() + " ms");
        }

        System.out.println("\nEnergy Consumption Per Device:");
        for (Map.Entry<String, Double> entry : energyConsumptionPerDevice.entrySet()) {
            System.out.println(entry.getKey() + " ---> " + entry.getValue() + " J");
        }
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public Map<String, Double> getApplicationLoopDelays() {
        return applicationLoopDelays;
    }

    public void setApplicationLoopDelays(Map<String, Double> applicationLoopDelays) {
        this.applicationLoopDelays = applicationLoopDelays;
    }

    public Map<String, Double> getTupleExecutionDelays() {
        return tupleExecutionDelays;
    }

    public void setTupleExecutionDelays(Map<String, Double> tupleExecutionDelays) {
        this.tupleExecutionDelays = tupleExecutionDelays;
    }

    public Map<String, Double> getEnergyConsumptionPerDevice() {
        return energyConsumptionPerDevice;
    }

    public void setEnergyConsumptionPerDevice(Map<String, Double> energyConsumptionPerDevice) {
        this.energyConsumptionPerDevice = energyConsumptionPerDevice;
    }

    public double getTotalNetworkUsage() {
        return totalNetworkUsage;
    }

    public void setTotalNetworkUsage(double totalNetworkUsage) {
        this.totalNetworkUsage = totalNetworkUsage;
    }

    public double getCloudExecutionCost() {
        return cloudExecutionCost;
    }

    public void setCloudExecutionCost(double cloudExecutionCost) {
        this.cloudExecutionCost = cloudExecutionCost;
    }
}

