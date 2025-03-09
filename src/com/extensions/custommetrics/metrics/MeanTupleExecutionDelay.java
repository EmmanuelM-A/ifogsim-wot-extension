package com.extensions.custommetrics.metrics;

import com.extensions.custommetrics.CustomPerformanceMetric;
import com.extensions.simulation.SimulationData;

import java.util.Map;

/**
 * Custom performance metric to calculate the mean execution time of all tuples.
 * This metric helps evaluate the average processing time for data tuples across
 * the fog computing environment, which is crucial for scalability analysis.
 */
public class MeanTupleExecutionDelay implements CustomPerformanceMetric<Double> {

    @Override
    public Double evaluate(SimulationData simulationData) {
        Map<String, Double> tupleExecutionDelays = simulationData.getTupleExecutionDelays();

        // If there are no tuples processed, return 0.0
        if (tupleExecutionDelays == null || tupleExecutionDelays.isEmpty()) {
            return 0.0;
        }

        // Calculate the sum of all tuple execution delays
        double totalDelay = 0.0;
        for (Double delay : tupleExecutionDelays.values()) {
            totalDelay += delay;
        }

        // Return the mean execution time
        return totalDelay / tupleExecutionDelays.size();
    }

    @Override
    public String getMetricName() {
        return "Mean Tuple Execution Delay (ms)";
    }
}
