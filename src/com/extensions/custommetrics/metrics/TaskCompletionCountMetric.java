package com.extensions.custommetrics.metrics;

import com.extensions.custommetrics.CustomPerformanceMetric;
import com.extensions.simulation.SimulationData;

/**
 * Custom metric to count the number of completed tasks.
 */
public class TaskCompletionCountMetric implements CustomPerformanceMetric<Integer> {

    @Override
    public Integer evaluate(SimulationData simulationData) {
        //return simulationData.getCompletedTasks();
        return 0;
    }

    @Override
    public String getMetricName() {
        return "Completed Task Count";
    }
}

