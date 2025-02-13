package com.extensions.custommetrics;

import com.extensions.simulation.SimulationData;
import com.extensions.simulation.SimulationResults;

/**
 * Generic interface for defining custom performance metrics.
 *
 * @param <T> The return type of the evaluation (e.g., Integer, Double, String).
 */
public interface CustomPerformanceMetric<T> {

    /**
     * Evaluates the custom metric based on the simulation data.
     *
     * @param simulationData Data collected during the simulation.
     * @return The computed metric value of type T.
     */
    T evaluate(SimulationData simulationData);

    /**
     * Returns the name of the custom metric.
     *
     * @return The metric name as a string.
     */
    String getMetricName();
}

