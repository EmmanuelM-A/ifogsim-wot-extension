package com.extensions.simulation;

import org.fog.entities.FogDevice;
import org.fog.utils.Config;
import org.fog.utils.NetworkUsageMonitor;
import org.fog.utils.TimeKeeper;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores all simulation results for analysis after execution.
 */
public class SimulationData {
    private long executionTime;
    private final Map<String, Double> applicationLoopDelays;
    private final Map<String, Double> tupleExecutionDelays;
    private final Map<String, Double> energyConsumptionPerDevice;
    private double totalNetworkUsage;
    private double cloudExecutionCost;

    /**
     * Constructor initializes empty result maps.
     */
    public SimulationData() {
        this.applicationLoopDelays = new HashMap<>();
        this.tupleExecutionDelays = new HashMap<>();
        this.energyConsumptionPerDevice = new HashMap<>();
        this.totalNetworkUsage = 0.0;
        this.cloudExecutionCost = 0.0;
    }

    /**
     * Records execution time of the simulation.
     */
    public void recordExecutionTime() {
        this.executionTime = Calendar.getInstance().getTimeInMillis() - TimeKeeper.getInstance().getSimulationStartTime();
    }

    public void recordApplicationLoopDelays(String appLoop, Double appLoopDelay) {
        applicationLoopDelays.put(appLoop, appLoopDelay);
    }

    /**
     * Records tuple execution delays.
     */
    public void recordTupleExecutionDelays(String tupleType, Double tupleDelay) {
       tupleExecutionDelays.put(tupleType, tupleDelay);
    }

    /**
     * Records energy consumption per Fog device.
     */
    public void recordEnergyConsumption(List<FogDevice> fogDevices) {
        for (FogDevice fogDevice : fogDevices) {
            energyConsumptionPerDevice.put(fogDevice.getName(), fogDevice.getEnergyConsumption());
        }
    }

    /**
     * Records total network usage.
     */
    public void recordNetworkUsage() {
        this.totalNetworkUsage = NetworkUsageMonitor.getNetworkUsage();
    }

    /**
     * Records cloud execution cost.
     */
    public void recordCloudExecutionCost(FogDevice cloudDevice) {
        if (cloudDevice != null) {
            this.cloudExecutionCost = cloudDevice.getTotalCost();
        } else {
            System.out.println("CLOUD DEVICE IS NULL!");
        }
    }

    /**
     * Prints formatted simulation results similar to iFogSim output.
     */
    public void printResults() {
        System.out.println("==========================================");
        System.out.println("=========== SIMULATION RESULTS ===========");
        System.out.println("==========================================");
        System.out.println("EXECUTION TIME: " + executionTime + " ms");
        System.out.println("==========================================");

        // Application Loop Delays
        System.out.println("APPLICATION LOOP DELAYS:");
        for (Map.Entry<String, Double> entry : applicationLoopDelays.entrySet()) {
            System.out.printf("AppLoop %s ---> %.3f ms%n", entry.getKey(), entry.getValue());
        }
        System.out.println("==========================================");

        // Tuple Execution Delays
        System.out.println("TUPLE EXECUTION DELAYS:");
        for (Map.Entry<String, Double> entry : tupleExecutionDelays.entrySet()) {
            System.out.printf("Tuple [%s] ---> %.3f ms%n", entry.getKey(), entry.getValue());
        }
        System.out.println("==========================================");

        // Energy Consumption Per Device
        System.out.println("ENERGY CONSUMPTION PER DEVICE:");
        for (Map.Entry<String, Double> entry : energyConsumptionPerDevice.entrySet()) {
            System.out.printf("Device [%s] ---> %.3f J%n", entry.getKey(), entry.getValue());
        }
        System.out.println("==========================================");

        // Total Network Usage
        System.out.printf("TOTAL NETWORK USAGE ---> %.3f KB%n", totalNetworkUsage / Config.MAX_SIMULATION_TIME);
        System.out.println("==========================================");

        // Cloud Execution Cost
        System.out.printf("CLOUD EXECUTION COST ---> $%.3f%n", cloudExecutionCost);
        System.out.println("==========================================");
    }

    // Getters for access to stored results
    public long getExecutionTime() {
        return executionTime;
    }

    public Map<String, Double> getApplicationLoopDelays() {
        return applicationLoopDelays;
    }

    public Map<String, Double> getTupleExecutionDelays() {
        return tupleExecutionDelays;
    }

    public Map<String, Double> getEnergyConsumptionPerDevice() {
        return energyConsumptionPerDevice;
    }

    public double getTotalNetworkUsage() {
        return totalNetworkUsage;
    }

    public double getCloudExecutionCost() {
        return cloudExecutionCost;
    }
}

