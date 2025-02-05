package com.extensions.custommetrics;

import com.extensions.simulation.SimulationResults;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages custom performance metrics for evaluation.
 */
public class CustomMetricManager {
    private final List<CustomPerformanceMetric<?>> customMetrics = new ArrayList<>();
    private final SimulationResults simulationResults;

    public CustomMetricManager(SimulationResults simulationResults) {
        this.simulationResults = simulationResults;
    }

    /**
     * Allows users to register custom metrics.
     *
     * @param metric The metric to be registered.
     */
    public void registerMetric(CustomPerformanceMetric<?> metric) {
        customMetrics.add(metric);
    }

    /**
     * Runs all registered custom metrics and prints results.
     */
    public void evaluateMetrics() {
        System.out.println("=========================================");
        System.out.println("CUSTOM PERFORMANCE METRICS");
        System.out.println("=========================================");
        for (CustomPerformanceMetric<?> metric : customMetrics) {
            Object result = metric.evaluate(simulationResults);
            System.out.println(metric.getMetricName() + ": " + result);
        }
    }
}

