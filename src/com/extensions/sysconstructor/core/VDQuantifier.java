package com.extensions.sysconstructor.core;

import com.extensions.utils.processors.FileProcessor;
import com.extensions.utils.Pair;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * VDQuantifier is responsible for processing a JSON file containing IoT device distributions
 * and mapping them to edge nodes.
 * <p>
 * This class implements {@code FileProcessor} to process a given JSON file and return
 * a structured representation of IoT devices per edge node.
 * <p>
 * JSON Format Example:
 * <pre>
 * {
 *   "edge-1": {
 *     "things": {
 *       "thing1": 3,
 *       "thing2": 4,
 *       "thing3": 4
 *     }
 *   },
 *   "edge-2": {
 *     "things": {
 *       "thing1": 3,
 *       "thing2": 4,
 *       "thing3": 4
 *     }
 *   }
 * }
 * </pre>
 *
 */
public class VDQuantifier implements FileProcessor<Map<String, List<Pair<String, Integer>>>> {

    /**
     * Processes a JSON file containing IoT device allocations to edge nodes.
     *
     * @param file The JSON file containing the IoT device information.
     * @return A map where each key is an edge node name, and the value is a list of
     *         pairs representing (Virtual Device Name, Quantity).
     * @throws IOException If an error occurs while reading the file.
     */
    @Override
    public Map<String, List<Pair<String, Integer>>> process(File file) throws IOException {
        // Initialize Jackson's ObjectMapper to parse JSON
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<Pair<String, Integer>>> edgeNodeVDMap = new HashMap<>();

        try {
            // Read and parse the JSON file
            JsonNode rootNode = objectMapper.readTree(file);

            // Iterate through edge nodes in the JSON structure
            for (Iterator<String> it = rootNode.fieldNames(); it.hasNext(); ) {
                String edgeNode = it.next();
                JsonNode edgeDataNode = rootNode.get(edgeNode);

                // Validate edgeDataNode structure
                if (edgeDataNode == null || !edgeDataNode.has("things")) {
                    System.err.println("Warning: Missing 'things' section in edge node: " + edgeNode);
                    continue; // Skip invalid entries
                }

                JsonNode thingsNode = edgeDataNode.get("things");
                List<Pair<String, Integer>> vdList = new ArrayList<>();

                // Iterate through the IoT devices in the "things" section
                for (Iterator<String> thingsIt = thingsNode.fieldNames(); thingsIt.hasNext(); ) {
                    String vdName = thingsIt.next();

                    // Extract and validate the quantity
                    JsonNode quantityNode = thingsNode.get(vdName);
                    if (quantityNode == null || !quantityNode.isInt()) {
                        System.err.println("Warning: Invalid quantity for device '" + vdName + "' in edge node: " + edgeNode);
                        continue; // Skip invalid entries
                    }

                    int quantity = quantityNode.asInt();
                    vdList.add(new Pair<>(vdName, quantity));
                }

                // Store the extracted information
                edgeNodeVDMap.put(edgeNode, vdList);
            }

        } catch (IOException e) {
            System.err.println("Error: Failed to process JSON file - " + e.getMessage());
            throw e; // Re-throw exception to allow higher-level handling
        }

        return edgeNodeVDMap;
    }
}