package com.extensions.custommetrics.metrics;

import com.extensions.custommetrics.CustomPerformanceMetric;
import com.extensions.simulation.SimulationResults;

import java.util.Map;

/**
 * Finds the device consuming the most energy. Useful for optimizing fog node placement in energy-aware IoT applications.
 */
public class PeakEnergyConsumptionDevice implements CustomPerformanceMetric<String> {
    @Override
    public String evaluate(SimulationResults simulationResults) {
        Map<String, Double> energyUsage = simulationResults.getEnergyConsumptionPerDevice();
        if (energyUsage.isEmpty()) return "No energy data available";

        return energyUsage.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + " (Energy: " + entry.getValue() + "J)")
                .orElse("No data");
    }

    @Override
    public String getMetricName() {
        return "Peak Energy Consumption Device";
    }
}

