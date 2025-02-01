package com.extensions.custommetrics.metrics;

import com.extensions.custommetrics.CustomPerformanceMetric;
import com.extensions.simulation.SimulationData;

/**
 * Custom metric to classify system efficiency.
 */
public class SystemEfficiencyMetric implements CustomPerformanceMetric<String> {

    @Override
    public String evaluate(SimulationData simulationData) {
        /*double utilization = simulationData.getAverageCpuUtilization();
        if (utilization > 80) return "High Load";
        if (utilization > 50) return "Moderate Load";
        return "Low Load";*/
        return "";
    }

    @Override
    public String getMetricName() {
        return "System Efficiency Classification";
    }
}

