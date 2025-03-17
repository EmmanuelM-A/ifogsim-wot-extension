package com.extensions.tests.testing;

import com.extensions.simulation.SimulationData;
import com.extensions.sysconstructor.core.ThingQuantityParser;
import com.extensions.tests.Testing;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.cloudbus.cloudsim.core.CloudSim;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class ScalabilityTestHarness {
    //private static final int[] QUANTITIES = {10, 30, 50, 100, 150, 250, 350, 450};
    private static final int[] QUANTITIES = {20, 30, 40, 60, 100, 150, 220, 340, 512};
    private static final int ITERATIONS = 20;

    // Maps to store results for each metric
    private final Map<Integer, List<Double>> meanTupleExecutionTimes = new HashMap<>();
    private final Map<Integer, List<Double>> networkUsages = new HashMap<>();
    private final Map<Integer, List<Double>> executionTimes = new HashMap<>();

    private final String THING_QUANTITIES_FILE = "src/com/extensions/tests/testing/SmartCityTrafficManagement/configs/thing-quantities.json";

    public void runTests() throws IOException {
        // Initialize result storage
        for (int quantity : QUANTITIES) {
            meanTupleExecutionTimes.put(quantity, new ArrayList<>());
            networkUsages.put(quantity, new ArrayList<>());
            executionTimes.put(quantity, new ArrayList<>());
        }

        // Run tests for each quantity
        for (int quantity : QUANTITIES) {
            for (int i = 0; i < ITERATIONS; i++) {
                System.out.println("Running test: Quantity " + quantity + ", Iteration " + (i+1));

                // Run the simulation
                SimulationData simulationData = runSimulation();

                // Collect metrics
                double tupleExecTime = simulationData.getExecutionTime();
                double networkUsage = simulationData.getTotalNetworkUsage();
                double execTime = (double) simulationData.getCustomPerformanceMetrics().get("Mean Tuple Execution Delay (ms)");

                // Store results
                meanTupleExecutionTimes.get(quantity).add(tupleExecTime);
                networkUsages.get(quantity).add(networkUsage);
                executionTimes.get(quantity).add(execTime);

                // Modify your thing-quantities.json programmatically
                updateThingQuantities();
            }
        }

        // Export results to CSV or directly to Excel
        exportResults();
    }

    private void updateThingQuantities() throws IOException {
        // Read the JSON file
        ThingQuantityParser thingQuantityParser = new ThingQuantityParser(new File(THING_QUANTITIES_FILE));

        Map<String, Integer> thingFrequencies = thingQuantityParser.getThingFrequencies();

        Map<String, List<String>> vdsConnectedToEdgeNode = thingQuantityParser.getVdsConnectedToEdgeNodes();

        // Update the quantities proportionally
        for(String thing : thingFrequencies.keySet()) {
            int thingQuantity = thingFrequencies.get(thing);

            //double scaleFactor = (double) quantity / thingQuantity;

            int newThingQuantity = (int)(thingQuantity * 1.5);

            thingFrequencies.put(thing, newThingQuantity);
        }

        // Write back to file
        addEdgeThingFrequency(vdsConnectedToEdgeNode, thingFrequencies);

        System.out.println("Updated thing quantities written to file.");
    }

    private void addEdgeThingFrequency(Map<String, List<String>> vdsConnectedToEdgeNode, Map<String, Integer> thingFrequencies) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            ObjectNode thingQuantities = mapper.createObjectNode();

            for(String edge : vdsConnectedToEdgeNode.keySet()) {
                ObjectNode edgeNode = mapper.createObjectNode();

                ObjectNode thingsNode = mapper.createObjectNode();

                switch (edge) {
                    case "traffic-surveillance":
                        thingsNode.put("Traffic Camera", thingFrequencies.get("Traffic Camera"));
                        break;
                    case "traffic-light-control":
                        thingsNode.put("Smart Traffic Light", thingFrequencies.get("Smart Traffic Light"));
                        break;
                    case "enviornmental-monitoring":
                        thingsNode.put("Roadside Air Quality Sensor", thingFrequencies.get("Roadside Air Quality Sensor"));
                        break;
                    case "pedestrian-monitoring":
                        thingsNode.put("Pedestrian Sensor", thingFrequencies.get("Pedestrian Sensor"));
                        break;
                    case "ai-traffic-control":
                        thingsNode.put("AI-Based Traffic Controller", thingFrequencies.get("AI-Based Traffic Controller"));
                        thingsNode.put("Cloud Dashboard", thingFrequencies.get("Cloud Dashboard"));
                        break;
                    default:
                        System.out.println("The edge " + edge + " does not exist!");
                        break;
                }

                edgeNode.set("things", thingsNode);

                thingQuantities.set(edge, edgeNode);
            }

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(THING_QUANTITIES_FILE), thingQuantities);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SimulationData runSimulation() throws IOException {
        Testing.main(new String[]{});

        // Reset CloudSim state if needed
        CloudSim.init(1, Calendar.getInstance(), false);

        return null;
    }

    private void exportResults() {
        // Export results to CSV files
        exportMetricToCSV("MeanTupleExecutionTime", meanTupleExecutionTimes);
        exportMetricToCSV("NetworkUsage", networkUsages);
        exportMetricToCSV("ExecutionTime", executionTimes);
    }

    private void exportMetricToCSV(String metricName, Map<Integer, List<Double>> data) {
        String filename = "src/com/extensions/tests/testing/results/" + metricName + ".csv";
        try (PrintWriter writer = new PrintWriter(filename)) {
            // Write header
            writer.print("Quantity");
            for (int i = 1; i <= ITERATIONS; i++) {
                writer.print(",Iteration " + i);
            }
            writer.print(",Mean,StdDev");
            writer.println();

            // Write data for each quantity
            for (int quantity : QUANTITIES) {
                writer.print(quantity);

                List<Double> values = data.get(quantity);
                for (double value : values) {
                    writer.print("," + value);
                }

                // Calculate average
                double avg = values.stream().mapToDouble(v -> v).average().orElse(0);

                // Calculate standard deviation
                double variance = values.stream()
                        .mapToDouble(v -> Math.pow(v - avg, 2))
                        .average().orElse(0);
                double stdDev = Math.sqrt(variance);

                writer.print("," + avg + "," + stdDev);
                writer.println();
            }
            System.out.println("COMPLETE!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            new ScalabilityTestHarness().runTests();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
