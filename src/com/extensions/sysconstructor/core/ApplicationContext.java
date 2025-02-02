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

public class ApplicationContext {
    public final CloudNodePreset cloudNodePreset;
    public final EdgeNodePreset edgeNodePreset;
    public final ApplicationPreset applicationPreset;
    public final List<FogDevice> fogDevices;
    public final List<FogDevice> edgeNodes;
    public final List<TopologyNode> things;
    public final List<String> nodeTopics;
    /**
     * A list of all topology nodes of the required types: read-property, write-property, subscribe-event, inject and invoke-action
     */
    public final List<TopologyNode> topologyNodes;
    public final List<TopologyNodeTree> topologyNodeTrees;
    public final List<TopologyNodeConnection> nodeConnections;
    public final List<TopologyNodeTree> dataFlows;
    public final List<TopologyNode> events;
    public final Map<String, AppModule> appModulesCreated;

    public final Map<AppModule, AppModule> appEdges;

    public final Map<String, NodeModule> nodeModules;
    public final TopologyNodeConnectionChecker nodeConnectionChecker;
    public final int UPLINK_LATENCY_EDGE_TO_CLOUD = 100;
    public final int UPLINK_LATENCY_VD_TO_EDGE = 10;

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
        this.nodeModules = new HashMap<>();

        // Generate the application topology from the node red application design
        NodeRedTranslator.nodeRedToInputJson(nodeRedApplicationJsonFile);

        // Set up the parser for the application
        ApplicationTopologyParser applicationTopologyParser = new ApplicationTopologyParser(new File(FilePaths.APPLICATION_TOPOLOGY));

        // Extract all the thing nodes
        this.things = applicationTopologyParser.parseTopologyNodes("things");

        // Extract all the topics
        this.nodeTopics = applicationTopologyParser.parseTopologyNodeTopics();

        // Extract all the topology nodes
        this.topologyNodes = applicationTopologyParser.parseTopologyNodes("nodes");

        this.topologyNodeTrees = applicationTopologyParser.parseTopologyNodeTrees();

        // Extract all the connections between nodes
        this.nodeConnections = applicationTopologyParser.parseTopologyConnections();

        // Initialise node connection checker
        this.nodeConnectionChecker = new TopologyNodeConnectionChecker(this.nodeConnections, this.topologyNodes);

        // Store all the data flows
        this.dataFlows = new ArrayList<>();

        this.appModulesCreated = new HashMap<>();

        this.appEdges = new HashMap<>();

        // Extract all events used
        this.events = applicationTopologyParser.parseTopologyNodes("events");

        System.out.println("Application Topology Parsed Successfully!");
    }
}