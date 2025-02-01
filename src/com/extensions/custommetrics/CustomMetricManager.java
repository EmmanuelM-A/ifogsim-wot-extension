package com.extensions.custommetrics;

import com.extensions.simulation.SimulationData;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages custom performance metrics for evaluation.
 */
public class CustomMetricManager {
    private final List<CustomPerformanceMetric<?>> customMetrics = new ArrayList<>();

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
     *
     * @param simulationData The data used for evaluation.
     */
    public void evaluateMetrics(SimulationData simulationData) {
        System.out.println("=========================================");
        System.out.println("CUSTOM PERFORMANCE METRICS");
        System.out.println("=========================================");
        for (CustomPerformanceMetric<?> metric : customMetrics) {
            Object result = metric.evaluate(simulationData);
            System.out.println(metric.getMetricName() + ": " + result);
        }
    }
}

