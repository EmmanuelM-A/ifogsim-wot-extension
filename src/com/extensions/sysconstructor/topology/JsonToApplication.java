package com.extensions.sysconstructor.topology;

import com.extensions.customfog.FogDeviceFactory;
import com.extensions.customfog.SensorProperty;
import com.extensions.sysconstructor.core.ApplicationPhysicalTopology;
import com.extensions.sysconstructor.nodered.NodeRedJSONParser;
import com.extensions.sysconstructor.nodered.NodeRedTranslator;
import com.extensions.utils.FilePaths;
import com.extensions.utils.Utility;
import com.extensions.utils.presets.CloudNodePreset;
import com.extensions.utils.presets.EdgeNodePreset;
import com.extensions.vdcreation.core.VirtualDevice;
import jdk.jshell.execution.Util;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * Parse the application topology json
 * Get the VDs
 * Connect devices according to the topology JSON
 * INJECT, SUB-EVENTS = REPRESENTS DATA FLOW ONLY (IGNORE)
 * READ-PROP = SENSORS
 * INVOKE-ACTION, WRITE-PROP = ACTUATORS
 * READ-PROP search for
 * */

public class JsonToApplication {
    private final CloudNodePreset cloudNodePreset;
    private final EdgeNodePreset edgeNodePreset;
    private final ApplicationTopologyParser applicationTopologyParser;

    public JsonToApplication(CloudNodePreset cloudNodePreset, EdgeNodePreset edgeNodePreset) throws IOException {
        this.cloudNodePreset = cloudNodePreset;
        this.edgeNodePreset = edgeNodePreset;
        this.applicationTopologyParser = new ApplicationTopologyParser(new File(FilePaths.APPLICATION_TOPOLOGY));
    }

    public ApplicationPhysicalTopology createPhysicalTopology(int userId, String appId, File nodeRedApplicationJsonFile, List<VirtualDevice> virtualDevices) {
        List<FogDevice> fogDevices = new ArrayList<>();
        List<Sensor> sensors = new ArrayList<>();
        List<Actuator> actuators = new ArrayList<>();
        List<FogDevice> edgeNodes = new ArrayList<>();

        try {
            // Generate the application topology from the node red application design
            NodeRedTranslator.nodeRedToInputJson(nodeRedApplicationJsonFile);

            // Parse the application topology json
            ApplicationTopologyParser applicationTopologyParser = new ApplicationTopologyParser(new File(FilePaths.APPLICATION_TOPOLOGY));

            // Extract all the thing nodes
            List<TopologyNode> things = applicationTopologyParser.parseTopologyNodes("things");

            // Extract all the topics - USED FOR EDGE NODE AND DEVICE GROUPING
            List<String> nodeTopics = applicationTopologyParser.parseTopologyNodeTopics();

            // Extract all the topology nodes
            List<TopologyNode> topologyNodes = applicationTopologyParser.parseTopologyNodes("nodes");

            // Extract all the connections between nodes
            List<TopologyNodeConnection> topologyNodeConnections = applicationTopologyParser.parseTopologyConnections();

            System.out.println("Application Topology Parsed Successfully!");

            //System.out.println(topologyNodes.getFirst().toString()); // TODO COME BACK TO THIS

            // Search for selected VDs based on things list
            List<VirtualDevice> selectedVirtualDevices = getSelectedVirtualDevices(virtualDevices, things);

            // Create cloud node
            FogDevice cloud = FogDeviceFactory.createFogDevice(
                    "cloud",
                    cloudNodePreset.MIPS,
                    cloudNodePreset.RAM,
                    cloudNodePreset.UPLINK_BW,
                    cloudNodePreset.DOWN_LINK_BW,
                    cloudNodePreset.LATENCY,
                    cloudNodePreset.RATE_PER_MIPS,
                    cloudNodePreset.BUSY_POWER,
                    cloudNodePreset.IDLE_POWER
            );

            // Cloud has no parent, it is the root of the hierarchy
            cloud.setParentId(-1);

            // If there are topics - assign VD to edge nodes based on topics else
                // Create the edge nodes using formula E = Int((D - 6) / 2) where D > 6

                // connectNodesToEdgeNodes()

            if(!nodeTopics.isEmpty()) {
                for(String topic : nodeTopics) {
                    // Create the edge node for that topic
                    FogDevice edgeNode = createEdgeNode(topic);

                    // Link the edge node to the cloud
                    edgeNode.setParentId(cloud.getId());
                    edgeNode.setUplinkLatency(100); // CHANGE TO A GENERIC VALUE
                    fogDevices.add(edgeNode);
                    edgeNodes.add(edgeNode);

                    // Connect all VDs to this edge node based on the topic
                    for(VirtualDevice virtualDevice : selectedVirtualDevices) {
                        FogDevice vdFogDevice = getVirtualDeviceWithMatchingTopic(topologyNodes, things, virtualDevice, topic).getFogDevice();

                        if(vdFogDevice != null) {
                            vdFogDevice.setParentId(edgeNode.getId());
                            vdFogDevice.setUplinkLatency(10); // CHANGE TO GENERIC VALUE
                            fogDevices.add(vdFogDevice);
                        }
                    }
                }
            } else {
                /*
                * Generate the number of edge nodes based on the number VDs (IoT devices) for example the
                * No. Edge Nodes = Int((No. VDs - 6) / 2) where No. VDs > 6, so the MAX_VD_FOR_ONE_EDGE_NODE = 6
                *
                * Distribute the VDs eqaully amongst the edge nodes so each edge node has up to 6
                *
                * distributeVDsAmongstEdgeNodes(edgeNodes, virtualDevices)
                *
                * */
            }

            // Add all fog nodes, sensors and actuators to ApplicationPhysTopology



        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }



    public List<VirtualDevice> getSelectedVirtualDevices(List<VirtualDevice> virtualDevices, List<TopologyNode> things) {
        List<VirtualDevice> selectedVirtualDevices = new ArrayList<>();

        for(VirtualDevice virtualDevice : virtualDevices) {
            String name = virtualDevice.getFogDevice().getName();

            for(TopologyNode thing : things) {
                String thingName = thing.name();

                if(name.equals(thingName)) selectedVirtualDevices.add(virtualDevice);
            }
        }

        return selectedVirtualDevices;
    }

    public FogDevice createEdgeNode(String identifier) {
        return FogDeviceFactory.createFogDevice(
                "edgeNode-" + identifier,
                edgeNodePreset.MIPS,
                edgeNodePreset.RAM,
                edgeNodePreset.UPLINK_BW,
                edgeNodePreset.DOWN_LINK_BW,
                edgeNodePreset.LATENCY,
                edgeNodePreset.RATE_PER_MIPS,
                edgeNodePreset.BUSY_POWER,
                edgeNodePreset.IDLE_POWER
        );
    }

    public VirtualDevice getVirtualDeviceWithMatchingTopic(List<TopologyNode> nodes, List<TopologyNode> things, VirtualDevice virtualDevice, String targetTopic) {
        if(targetTopic.isEmpty()) {
            System.out.println("Topic must not be empty!");
            return null;
        }

        // TODO IMPROVE TIME COMPLEXITY - CURRENTLY N^4 TOO LONG!!!!

        // If a topology node's thing and topic are set (not null or empty) and the topology node is an
        // action or read-property node, then using the thing attribute get the thing referenced, and from that
        // use the thing's name to get the corresponding VD.
        for(TopologyNode node : nodes) {
            if(
               node.thing() != null && !node.thing().isEmpty() && node.topic() != null && node.topic().equals(targetTopic) &&
               (node.uniqueAttribute().equals(NodeRedJSONParser.TYPE_READ_PROPERTY) ||
               node.uniqueAttribute().equals(NodeRedJSONParser.TYPE_INVOKE_ACTION))) {
                   for(TopologyNode thingNode : things) {
                       if(node.thing().equals(thingNode.id())) {
                           if(virtualDevice.getFogDevice().getName().equals(thingNode.name())) return virtualDevice;
                       }
                   }
            }
        }

        return null;
    }

    public Application createApplicationDataMappings() {
        return null;
    }

    public void connectNodesToEdgeNodes(List<FogDevice> edgdeNodes, List<VirtualDevice> selectedVirtualDevices) {

    }
}
