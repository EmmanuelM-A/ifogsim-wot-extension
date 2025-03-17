package com.extensions.sysconstructor.nodered;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This class is responsible for converting a Node-RED application JSON file into an input JSON format for system topology.
 */
public class NodeRedTranslator {
    /**
     * The output file path where the application topology JSON will be saved.
     */
    public static String APPLICATION_TOPOLOGY = "src/com/extensions/output/application/application_topology.json";

    /**
     * Converts the given Node-RED application JSON file to an input JSON format and saves it to the specified location.
     *
     * @param nodeRedApplicationJsonFile The Node-RED application JSON file to be processed.
     */
    public static void nodeRedToInputJson(File nodeRedApplicationJsonFile) {
        try {
            // Parse Node-RED JSON into Nodes
            NodeRedJSONParser parser = new NodeRedJSONParser();
            List<NodeRedNode> rawNodes = parser.process(nodeRedApplicationJsonFile);

            // Generate the output JSON with the correct format
            NodeRedJSONGenerator generator = new NodeRedJSONGenerator(rawNodes);
            ObjectNode outputJson = generator.generate();

            // Write to file using Jackson's ObjectMapper
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(APPLICATION_TOPOLOGY), outputJson);

            System.out.println("Node Red Application Parsed Successfully!");
        } catch (IOException e) {
            System.out.println("Node Red Application Parsed Unsuccessfully! Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
