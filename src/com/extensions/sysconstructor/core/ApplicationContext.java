package com.extensions.sysconstructor.core;

import com.extensions.sysconstructor.nodered.NodeRedJSONParser;
import com.extensions.sysconstructor.nodered.NodeRedTranslator;
import com.extensions.sysconstructor.topology.*;
import com.extensions.utils.FilePaths;
import com.extensions.utils.Utility;
import com.extensions.utils.presets.ApplicationPreset;
import com.extensions.utils.presets.CloudNodePreset;
import com.extensions.utils.presets.EdgeNodePreset;
import com.extensions.vdcreation.core.VirtualDevice;
import org.fog.application.AppLoop;
import org.fog.entities.FogDevice;
import org.fog.application.AppModule;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The ApplicationContext class represents the execution context of an IoT-Fog computing application.
 * It is responsible for parsing a Node-RED application design and constructing the necessary data structures
 * to facilitate simulation and analysis.
 */
public class ApplicationContext {
    /**
     * Presets for defining the characteristics of cloud nodes in the application
     */
    public final CloudNodePreset cloudNodePreset;

    /**
     * Presets for defining the characteristics of all edge nodes in the application
     */
    public final EdgeNodePreset edgeNodePreset;

    /**
     * Presets for defining the characteristics of all cloud nodes in the application
     */
    public final ApplicationPreset applicationPreset;

    /**
     * List of all fog computing devices involved in the application
     */
    public final List<FogDevice> fogDevices;

    /**
     * List of edge nodes in the application
     */
    public final List<FogDevice> edgeNodes;

    /**
     * The list of all WoT thing nodes parsed from the application topology
     */
    public final List<TopologyNode> things;

    /**
     * The list of all topics parsed from the application topology. Used to group devices with edge nodes.
     */
    public final List<String> nodeTopics;

    /**
     * A list of all nodes used in the application.
     */
    public final List<TopologyNode> topologyNodes;

    /**
     * A list of all required nodes (WoT nodes and inject nodes), all other node types like
     * functions or debugs have been filtered out.
     */
    public final List<TopologyNode> requiredNodes;

    // Hierarchical representation of topology nodes in a tree structure
    public final List<TopologyNodeTree> topologyNodeTrees;

    // Sub flow trees that represent data flows within an application
    public final List<TopologyNodeTree> dataFlows;

    /**
     * Sub flow trees that represent (user) inject stimulated data flows.
     */
    public final List<TopologyNodeTree> injectFlows;

    /**
     * Sub flow trees that represent event flows within an application.
     */
    public final List<TopologyNodeTree> eventFlows;

    // Map of created application modules, identified by a unique string key
    public final Map<String, AppModule> appModulesCreated;

    // Map storing application edges, representing relationships between nodes
    public final Map<String, String> appEdges;

    // List of loops present in the application topology (cyclic dependencies)
    public final List<AppLoop> appLoops;

    // Parser responsible for extracting and organizing application topology information
    public final ApplicationTopologyParser applicationTopologyParser;

    public final List<VirtualDevice> selectedVirtualDevices;

    /**
     * Constructor to initialize the application context by parsing a Node-RED application design.
     *
     * @param nodeRedApplicationJsonFile JSON file describing the Node-RED application design.
     * @param cloudNodePreset Preset configuration for cloud nodes.
     * @param edgeNodePreset Preset configuration for edge nodes.
     * @param applicationPreset Preset configuration for the application.
     * @throws IOException If an error occurs while reading or parsing the file.
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

        // Convert the Node-RED application description into a structured input format
        NodeRedTranslator.nodeRedToInputJson(nodeRedApplicationJsonFile);

        // Initialize the topology parser using the generated application topology file
        this.applicationTopologyParser = new ApplicationTopologyParser(new File(FilePaths.APPLICATION_TOPOLOGY));

        // Extract IoT device nodes (things) from the parsed topology
        this.things = applicationTopologyParser.parseTopologyNodes("things");

        // Extract the list of communication topics used in the application
        this.nodeTopics = applicationTopologyParser.parseTopologyNodeTopics();

        // Parse and construct topology node trees for hierarchical organization
        this.topologyNodeTrees = applicationTopologyParser.parseTopologyNodeTrees();

        // Get all the topology nodes from the sub flows list
        this.topologyNodes = Utility.getAllNodesFromTopology(topologyNodeTrees);

        // Get only the required topology nodes (only WoT nodes and inject nodes)
        this.requiredNodes = filterTopologyNode(topologyNodes);

        // Initialize the connection checker to validate topology node connections
        TopologyNodeConnectionChecker.initializeChecker(topologyNodeTrees);

        this.selectedVirtualDevices = new ArrayList<>();

        // Initialize storage for data flows, application modules, and application edges
        this.dataFlows = new ArrayList<>();
        this.eventFlows = new ArrayList<>();
        this.injectFlows = new ArrayList<>();
        this.appModulesCreated = new HashMap<>();
        this.appEdges = new HashMap<>();
        this.appLoops = new ArrayList<>();

        System.out.println("Application Topology Parsed Successfully!");
    }

    private List<TopologyNode> filterTopologyNode(List<TopologyNode> topologyNodes) {
        List<String> filter = List.of(
                NodeRedJSONParser.TYPE_INVOKE_ACTION,
                NodeRedJSONParser.TYPE_READ_PROPERTY,
                NodeRedJSONParser.TYPE_WRITE_PROPERTY,
                NodeRedJSONParser.TYPE_SUBSCRIBE_EVENT,
                NodeRedJSONParser.TYPE_INJECT);

        List<TopologyNode> filteredNodes = new ArrayList<>();

        for(TopologyNode topologyNode : topologyNodes) {
            if(filter.contains(topologyNode.type())) filteredNodes.add(topologyNode);
        }

        return filteredNodes;
    }
}
