package com.extensions.custommetrics.metrics;

import com.extensions.custommetrics.CustomPerformanceMetric;
import com.extensions.simulation.SimulationResults;

import java.util.Map;

/**
 * Finds the most time-consuming application loop. Helps detect which task chains
 * are slowing down the system.
 */
public class LongestApplicationLoopDelay implements CustomPerformanceMetric<String> {
    @Override
    public String evaluate(SimulationResults simulationResults) {
        Map<String, Double> loopDelays = simulationResults.getApplicationLoopDelays();
        if (loopDelays.isEmpty()) return "No loops detected";

        return loopDelays.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> "Loop: " + entry.getKey() + " (Delay: " + entry.getValue() + "ms)")
                .orElse("No data");
    }

    @Override
    public String getMetricName() {
        return "Longest Application Loop Delay";
    }
}

