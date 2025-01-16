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
        //nodeRedToInputJson(new File("src/com/extensions/input/application/smart-coffee-machine-application.json"));

        try {
            ApplicationTopologyParser applicationTopologyParser = new ApplicationTopologyParser(new File(FilePaths.NODE_RED_APPLICATION_JSON));

            List<TopologyNode> topologyNodes = applicationTopologyParser.parseTopologyNodes("nodes");

            List<TopologyNodeConnection> topologyNodeConnections = applicationTopologyParser.parseTopologyConnections();

            System.out.println("Application Topology Parsed Successfully!");

            TopologyNodeConnectionChecker connectionChecker = new TopologyNodeConnectionChecker(topologyNodeConnections);

            String isMaintenceNeeded = Utility.getTopologyNodeIdByName(topologyNodes, "isMaintenceNeeded");
            String writeProp = Utility.getTopologyNodeIdByType(topologyNodes, NodeRedJSONParser.TYPE_WRITE_PROPERTY);

            if(connectionChecker.areNodesConnected(isMaintenceNeeded, writeProp)) {
                System.out.println(isMaintenceNeeded + " is connected to " + writeProp);
            } else {
                System.out.println(isMaintenceNeeded + " is not connected to " + writeProp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
