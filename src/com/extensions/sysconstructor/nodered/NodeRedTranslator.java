package com.extensions.sysconstructor.nodered;

import com.extensions.utils.FilePaths;
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

            // Tree grouping for nodes
            TreeGrouping treeGrouper = new TreeGrouping(nodes);
            List<Tree> trees = treeGrouper.groupNodesIntoTrees();

            // Generate JSON with Things, Nodes, Wires, Data Flows, and Events
            NodeRedJSONGenerator generator = new NodeRedJSONGenerator(nodes, trees);
            ObjectNode outputJson = generator.generate();

            // Write to file using Jackson's ObjectMapper
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(FilePaths.NODE_RED_APPLICATION_JSON), outputJson);

            System.out.println("JSON file generated successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        nodeRedToInputJson(new File("src/com/extensions/input/application/smart-coffee-machine-application.json"));
    }
}
