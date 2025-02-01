package com.extensions.custommetrics.metrics;

import com.extensions.custommetrics.CustomPerformanceMetric;
import com.extensions.simulation.SimulationData;

/**
 * Custom metric to measure the average tuple processing time.
 */
public class AverageTupleProcessingTimeMetric implements CustomPerformanceMetric<Double> {

    @Override
    public Double evaluate(SimulationData simulationData) {
        /*double totalProcessingTime = simulationData.getTotalTupleProcessingTime();
        int tupleCount = simulationData.getTotalTuplesProcessed();

        return tupleCount == 0 ? 0 : totalProcessingTime / tupleCount;*/
        return 0.0;
    }

    @Override
    public String getMetricName() {
        return "Average Tuple Processing Time";
    }
}

