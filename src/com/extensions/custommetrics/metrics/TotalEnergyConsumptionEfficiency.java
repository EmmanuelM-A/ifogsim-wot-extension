package com.extensions.custommetrics.metrics;

import com.extensions.custommetrics.CustomPerformanceMetric;
import com.extensions.simulation.SimulationResults;

/**
 * Calculates the energy used per tuple processed. It helps measure how power-efficient the IoT
 * simulation is.
 */
public class TotalEnergyConsumptionEfficiency implements CustomPerformanceMetric<Double> {
    @Override
    public Double evaluate(SimulationResults simulationResults) {
        double totalEnergy = simulationResults.getEnergyConsumptionPerDevice().values()
                .stream().mapToDouble(Double::doubleValue).sum();

        int totalTuplesProcessed = simulationResults.getTupleExecutionDelays().size();
        if (totalTuplesProcessed == 0) return Double.POSITIVE_INFINITY;

        return totalEnergy / totalTuplesProcessed;
    }

    @Override
    public String getMetricName() {
        return "Total Energy Consumption Efficiency (Joules per Tuple)";
    }
}

