package com.extensions.sysconstructor.nodered;

import com.extensions.sysconstructor.topology.ApplicationTopologyParser;
import com.extensions.sysconstructor.topology.TopologyNode;
import com.extensions.sysconstructor.topology.TopologyNodeConnection;
import com.extensions.sysconstructor.topology.TopologyNodeConnectionChecker;
import com.extensions.utils.FilePaths;
import com.extensions.utils.Utility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class NodeRedTranslator {
    public static void nodeRedToInputJson(File nodeRedApplicationJsonFile) {
        try {
            // Parse Node-RED JSON into Nodes
            NodeRedJSONParser parser = new NodeRedJSONParser();
            List<NodeRedNode> nodes = parser.process(nodeRedApplicationJsonFile);

            // Generate the output JSON with the correct format
            NodeRedJSONGenerator generator = new NodeRedJSONGenerator(nodes);
            ObjectNode outputJson = generator.generate();

            // Write to file using Jackson's ObjectMapper
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(FilePaths.APPLICATION_TOPOLOGY), outputJson);

            System.out.println("Node Red Application Parsed Successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
