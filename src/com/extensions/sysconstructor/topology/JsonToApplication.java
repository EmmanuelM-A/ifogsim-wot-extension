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
    private final List<FogDevice> fogDevices;
    private final List<Sensor> sensors;
    private final List<Actuator> actuators;
    private final List<FogDevice> edgeNodes;

    private final int UPLINK_LATENCY_EDGE_TO_CLOUD = 100;

    private final int UPLINK_LATENCY_VD_TO_EDGE = 10;

    public JsonToApplication(CloudNodePreset cloudNodePreset, EdgeNodePreset edgeNodePreset) throws IOException {
        this.cloudNodePreset = cloudNodePreset;
        this.edgeNodePreset = edgeNodePreset;
        this.applicationTopologyParser = new ApplicationTopologyParser(new File(FilePaths.APPLICATION_TOPOLOGY));
        this.fogDevices = new ArrayList<>();
        this.sensors = new ArrayList<>();
        this.actuators = new ArrayList<>();
        this.edgeNodes = new ArrayList<>();
    }

    public ApplicationPhysicalTopology createPhysicalTopology(File nodeRedApplicationJsonFile, List<VirtualDevice> virtualDevices) {
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
                    edgeNode.setUplinkLatency(UPLINK_LATENCY_EDGE_TO_CLOUD);
                    fogDevices.add(edgeNode);
                    edgeNodes.add(edgeNode);

                    // Connect all VDs to this edge node based on the topic
                    for(VirtualDevice virtualDevice : selectedVirtualDevices) {
                        VirtualDevice vd = getVirtualDeviceWithMatchingTopic(topologyNodes, things, virtualDevice, topic);

                        FogDevice vdFogDevice = null;

                        if(vd != null) vdFogDevice = vd.getFogDevice();

                        if(vdFogDevice != null) {
                            vdFogDevice.setParentId(edgeNode.getId());
                            vdFogDevice.setUplinkLatency(UPLINK_LATENCY_VD_TO_EDGE);
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

                int maxNoVDsForOneEdgeNode = 6;

                // Calculate the number of edge nodes needed
                int numberOfEdgeNodes = Math.max(1, calculateNoOfEdgeNodes(virtualDevices.size(), maxNoVDsForOneEdgeNode));

                // Create a list of edge nodes (each edge node is represented as a list of VDs)
                List<List<VirtualDevice>> edgeNodeList = new ArrayList<>();

                // Distribute virtual devices among edge nodes
                for (int i = 0; i < numberOfEdgeNodes; i++) {
                    edgeNodeList.add(new ArrayList<>());
                }

                for (int i = 0; i < virtualDevices.size(); i++) {
                    // Assign each virtual device to an edge node in a round-robin manner
                    edgeNodeList.get(i % numberOfEdgeNodes).add(virtualDevices.get(i));
                }

                // Connect the VDs to the edge nodes
                for(int index = 0; index < edgeNodeList.size(); index++) {
                    // Create the edge node
                    FogDevice edgeNode = createEdgeNode(String.valueOf(index));

                    // Link the edge node to the cloud
                    edgeNode.setParentId(cloud.getId());
                    edgeNode.setUplinkLatency(UPLINK_LATENCY_EDGE_TO_CLOUD);
                    fogDevices.add(edgeNode);
                    edgeNodes.add(edgeNode);

                    // Connect the VDs to the edge node
                    for(VirtualDevice virtualDevice : edgeNodeList.get(index)) {
                        FogDevice vdFogDevice = virtualDevice.getFogDevice();

                        vdFogDevice.setParentId(edgeNode.getId());

                        vdFogDevice.setUplinkLatency(UPLINK_LATENCY_VD_TO_EDGE);

                        fogDevices.add(vdFogDevice);
                    }
                }
            }

            System.out.println("Application physical topology formed!");
            // MIGHT HAVE TO ADD ONLY THE SENSORS AND ACTUATORS USED IN APPLICATION

            // Add all fog nodes, sensors and actuators to ApplicationPhysTopology
            addAllSensorsAndActuators(selectedVirtualDevices);

            ApplicationPhysicalTopology applicationPhysicalTopology = new ApplicationPhysicalTopology();

            applicationPhysicalTopology.setFogDevices(fogDevices);
            applicationPhysicalTopology.setSensors(sensors);
            applicationPhysicalTopology.setActuators(actuators);
            applicationPhysicalTopology.setEdgeNodes(edgeNodes);

            return applicationPhysicalTopology;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Application createApplicationDataMappings() {
        return null;
    }

    /*private List<String> getAllSensorsUsedIn(List<TopologyNode> nodes, String typeMatch) {
        List<String> nodeAttributes = new ArrayList<>();

        for(TopologyNode node : nodes) {
            if(node.type().equals(typeMatch)) {
                nodeAttributes.add(node.uniqueAttribute());
            }
        }

        for(VirtualDevice virtualDevice : virtualDevices) {
            if(!virtualDevice.getSensorProperties().isEmpty()) {
                sensors.addAll(virtualDevice.getSensorProperties());
            }
        }
    }*/

    private void addAllSensorsAndActuators(List<VirtualDevice> virtualDevices) {
        for(VirtualDevice virtualDevice : virtualDevices) {
            if(!virtualDevice.getSensorProperties().isEmpty()) {
                sensors.addAll(virtualDevice.getSensorProperties());
            }

            if(!virtualDevice.getActuatorActions().isEmpty()) {
                actuators.addAll(virtualDevice.getActuatorActions());
            }
        }
    }

    private int calculateNoOfEdgeNodes(int numberOfVDs, int maxNoVDsForOneEdgeNode) {
        // CHANGE FORMULA AS YOU SEE FIT
        return (int)(numberOfVDs - maxNoVDsForOneEdgeNode) / 2;
    }

    private List<VirtualDevice> getSelectedVirtualDevices(List<VirtualDevice> virtualDevices, List<TopologyNode> things) {
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

    private FogDevice createEdgeNode(String identifier) {
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

    private VirtualDevice getVirtualDeviceWithMatchingTopic(List<TopologyNode> nodes, List<TopologyNode> things, VirtualDevice virtualDevice, String targetTopic) {
        if(targetTopic.isEmpty()) {
            System.out.println("Topic must not be empty!");
            return null;
        }

        // TODO IMPROVE TIME COMPLEXITY - CURRENTLY N^4 TOO HIGH!!!!

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
}
