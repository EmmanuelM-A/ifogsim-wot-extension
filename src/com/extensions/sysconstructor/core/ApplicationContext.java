package com.extensions.sysconstructor.core;

import com.extensions.sysconstructor.nodered.NodeRedTranslator;
import com.extensions.sysconstructor.topology.*;
import com.extensions.utils.FilePaths;
import com.extensions.utils.Utility;
import com.extensions.utils.presets.ApplicationPreset;
import com.extensions.utils.presets.CloudNodePreset;
import com.extensions.utils.presets.EdgeNodePreset;
import org.fog.entities.FogDevice;
import org.fog.application.AppModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The ApplicationContext class represents the execution context of an IoT-Fog computing application.
 * It is responsible for parsing a Node-RED application design and constructing the necessary data structures
 * to facilitate simulation and analysis.
 */
public class ApplicationContext {
    // Presets defining the characteristics of cloud and edge nodes, as well as the overall application
    public final CloudNodePreset cloudNodePreset;
    public final EdgeNodePreset edgeNodePreset;
    public final ApplicationPreset applicationPreset;

    // List of fog computing devices involved in the application
    public final List<FogDevice> fogDevices;

    // List of edge nodes in the application
    public final List<FogDevice> edgeNodes;

    // The list of all WoT things parsed from the application topology
    public final List<TopologyNode> things;

    // List of communication topics used by nodes
    public final List<String> nodeTopics;

    /**
     * A list of all topology nodes of specific types:
     * - read-property
     * - write-property
     * - subscribe-event
     * - inject
     * - invoke-action
     */
    //public final List<TopologyNode> topologyNodes;

    // Hierarchical representation of topology nodes in a tree structure
    public final List<TopologyNodeTree> topologyNodeTrees;

    // List of all direct connections between topology nodes
    //public final List<TopologyNodeConnection> nodeConnections;

    // Data flow paths within the application, stored as topology node trees
    public final List<TopologyNodeTree> dataFlows;

    // Map of created application modules, identified by a unique string key
    public final Map<String, AppModule> appModulesCreated;

    // Map storing application edges, representing relationships between nodes
    public final Map<String, String> appEdges;

    // List of loops present in the application topology (cyclic dependencies)
    public final List<List<String>> appLoops;

    // Map of node modules, storing different processing components for each node
    //public final Map<String, NodeModule> nodeModules;

    // Checker to validate the connections between topology nodes
    //public final TopologyNodeConnectionChecker nodeConnectionChecker;

    // Parser responsible for extracting and organizing application topology information
    public final ApplicationTopologyParser applicationTopologyParser;

    /**
     * Constructor to initialize the application context by parsing a Node-RED application design.
     *
     * @param nodeRedApplicationJsonFile JSON file describing the Node-RED application design.
     * @param cloudNodePreset Preset configuration for cloud nodes.
     * @param edgeNodePreset Preset configuration for edge nodes.
     * @param applicationPreset Preset configuration for the application.
     * @throws IOException if an error occurs while reading or parsing the file.
     */
    public ApplicationContext(
            File nodeRedApplicationJsonFile,
            CloudNodePreset cloudNodePreset,
            EdgeNodePreset edgeNodePreset,
            ApplicationPreset applicationPreset
    ) throws IOException {
        this.cloudNodePreset = cloudNodePreset;
        this.edgeNodePreset = edgeNodePreset;
        this.applicationPreset = applicationPreset;
        this.fogDevices = new ArrayList<>();
        this.edgeNodes = new ArrayList<>();
        //this.nodeModules = new HashMap<>();

        // Convert the Node-RED application description into a structured input format
        NodeRedTranslator.nodeRedToInputJson(nodeRedApplicationJsonFile);

        // Initialize the topology parser using the generated application topology file
        this.applicationTopologyParser = new ApplicationTopologyParser(new File(FilePaths.APPLICATION_TOPOLOGY));

        // Extract IoT device nodes (things) from the parsed topology
        this.things = applicationTopologyParser.parseTopologyNodes("things");

        // Extract the list of communication topics used in the application
        this.nodeTopics = applicationTopologyParser.parseTopologyNodeTopics();

        // Extract all defined topology nodes from the parsed topology
        //this.topologyNodes = applicationTopologyParser.parseTopologyNodes("nodes");

        // Parse and construct topology node trees for hierarchical organization
        this.topologyNodeTrees = applicationTopologyParser.parseTopologyNodeTrees();

        // Extract the list of node-to-node connections within the topology
        //this.nodeConnections = applicationTopologyParser.parseTopologyConnections();

        // Initialize the connection checker to validate topology node connections
        //this.nodeConnectionChecker = new TopologyNodeConnectionChecker(this.nodeConnections, this.topologyNodes);

        // Initialize storage for data flows, application modules, and application edges
        this.dataFlows = new ArrayList<>();
        this.appModulesCreated = new HashMap<>();
        this.appEdges = new HashMap<>();
        this.appLoops = new ArrayList<>();

        System.out.println("Application Topology Parsed Successfully!");
    }
}
