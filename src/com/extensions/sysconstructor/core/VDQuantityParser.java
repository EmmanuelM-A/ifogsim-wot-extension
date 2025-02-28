package com.extensions.sysconstructor.core;

import com.extensions.utils.processors.FileProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class VDQuantityParser implements FileProcessor<Map<String, List<String>>> {
    private final Map<String, Integer> thingFrequencies;
    private final Map<String, List<String>> vdsConnectedToEdgeNodes;

    public VDQuantityParser(File file) throws IOException {
        this.thingFrequencies = new HashMap<>();
        this.vdsConnectedToEdgeNodes = process(file);
    }
    @Override
    public Map<String, List<String>> process(File file) throws IOException {
        // Initialize Jackson's ObjectMapper to parse JSON
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<String>> edgeNodeVDMap = new HashMap<>();

        try {
            // Read and parse the JSON file
            JsonNode rootNode = objectMapper.readTree(file);

            if (rootNode.isEmpty()) {
                throw new IllegalArgumentException("VD Quantities file is empty or missing!");
            }

            // Iterate through edge nodes in the JSON structure
            for (Iterator<String> it = rootNode.fieldNames(); it.hasNext(); ) {
                String edgeNode = it.next();
                JsonNode edgeDataNode = rootNode.get(edgeNode);

                // Validate edgeDataNode structure
                if (edgeDataNode == null || !edgeDataNode.has("things")) {
                    throw new IllegalArgumentException("Missing 'things' section in edge node: " + edgeNode);
                }

                JsonNode thingsNode = edgeDataNode.get("things");
                List<String> vdList = new ArrayList<>();

                // Iterate through the IoT devices in the "things" section
                for (Iterator<String> thingsIt = thingsNode.fieldNames(); thingsIt.hasNext(); ) {
                    String thingName = thingsIt.next();

                    // Extract and validate the quantity
                    JsonNode quantityNode = thingsNode.get(thingName);
                    if (quantityNode == null || !quantityNode.isInt() || quantityNode.asInt() < 0) {
                        throw new IllegalArgumentException("Invalid quantity for device '" + thingName + "' in edge node: " + edgeNode);
                    }

                    // Thing name --> VD name conversion (e.g., "Temperature Sensor" --> "TemperatureSensor")
                    String vdName = thingName.replace(" ", "");

                    int start = thingFrequencies.getOrDefault(thingName, 0);
                    int end = start + quantityNode.asInt();

                    // Add the formatted, quantified VDName to the list
                    for(int index = start; index < end; index++) {
                        vdList.add(vdName + "-" + index);
                    }

                    // Track thing occurrences
                    if(!thingFrequencies.containsKey(thingName)) {
                        thingFrequencies.put(thingName, quantityNode.asInt());
                    } else {
                        thingFrequencies.compute(thingName, (k, prevFreq) -> prevFreq + quantityNode.asInt());
                    }
                }

                // Store the extracted information
                edgeNodeVDMap.put(edgeNode, vdList);
            }

        } catch (IOException e) {
            System.err.println("Error: Failed to process JSON file - " + e.getMessage());
            throw e; // Re-throw exception for higher-level handling
        }

        return edgeNodeVDMap;
    }


    public Map<String, Integer> getThingFrequencies() {
        return thingFrequencies;
    }

    public Map<String, List<String>> getVdsConnectedToEdgeNodes() {
        return vdsConnectedToEdgeNodes;
    }
}