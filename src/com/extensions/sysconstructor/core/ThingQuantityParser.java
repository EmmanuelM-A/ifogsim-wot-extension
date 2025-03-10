package com.extensions.sysconstructor.core;

import com.extensions.utils.processors.FileProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Responsible for parsing the Thing (VD) quantities file and extracting and transforming the data into
 * a more useful form.
 */
public class ThingQuantityParser implements FileProcessor<Map<String, List<String>>> {
    /**
     * A frequency table that stores the number of times a thing appears.
     */
    private final Map<String, Integer> thingFrequencies;

    /**
     * A map of stores the all the VDs connected to an edge node. Where the key is the edge node name and the value
     * is a list of VDs that connect ot that edge node.
     */
    private final Map<String, List<String>> vdsConnectedToEdgeNodes;

    /**
     * Constructs a {@code VDQuantityParser} instance which on initialization parses the JSON file.
     * @param file The file containing the VD quantities (and their connections to edge devices).
     * @throws IOException IOException If an error occurs while reading or parsing the file
     */
    public ThingQuantityParser(File file) throws IOException {
        this.thingFrequencies = new HashMap<>();
        this.vdsConnectedToEdgeNodes = process(file);
    }

    /**
     * Processes the JSON file to extract the mapping of edge nodes to their connected VDs
     * and the frequencies of different things.
     *
     * @param file The JSON file to be processed.
     * @return A map where the key is the edge node name and the value is a list of VD names connected to it.
     * @throws IOException If an error occurs while reading or parsing the file.
     */
    @Override
    public Map<String, List<String>> process(File file) throws IOException {
        // Initialize Jackson's ObjectMapper to parse JSON
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<String>> edgeNodeVDMap = new HashMap<>();

        if(file == null || file.getName().isEmpty()) return null;

        // Read and parse the JSON file
        JsonNode rootNode = objectMapper.readTree(file);

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

        return edgeNodeVDMap.isEmpty() ? null : edgeNodeVDMap;
    }

    /**
     * Returns the frequencies of different things parsed from the JSON file.
     *
     * @return A map where the key is the thing name and the value is its frequency.
     */
    public Map<String, Integer> getThingFrequencies() {
        return thingFrequencies;
    }

    /**
     * Returns the mapping of edge nodes to their connected VDs.
     *
     * @return A map where the key is the edge node name and the value is a list of VD names connected to it.
     */
    public Map<String, List<String>> getVdsConnectedToEdgeNodes() {
        return vdsConnectedToEdgeNodes;
    }
}