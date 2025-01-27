package com.extensions.sysconstructor.topology;

import com.extensions.customfog.FogDeviceFactory;
import com.extensions.sysconstructor.core.ApplicationPhysicalTopology;
import com.extensions.sysconstructor.nodered.NodeRedJSONParser;
import com.extensions.sysconstructor.nodered.NodeRedTranslator;
import com.extensions.utils.FilePaths;
import com.extensions.utils.presets.CloudNodePreset;
import com.extensions.utils.presets.EdgeNodePreset;
import com.extensions.vdcreation.core.VirtualDevice;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
    private final List<FogDevice> fogDevices;
    private final List<FogDevice> edgeNodes;

    private final List<TopologyNode> things;

    private final List<String> nodeTopics;

    private final List<TopologyNode> topologyNodes;

    private final List<TopologyNodeConnection> nodeConnections;

    private final List<TopologyDataFlow> dataFlows;

    private final List<TopologyNode> events;

    private final int UPLINK_LATENCY_EDGE_TO_CLOUD = 100;

    private final int UPLINK_LATENCY_VD_TO_EDGE = 10;

    public JsonToApplication(File nodeRedApplicationJsonFile, CloudNodePreset cloudNodePreset, EdgeNodePreset edgeNodePreset) throws IOException {
        this.cloudNodePreset = cloudNodePreset;
        this.edgeNodePreset = edgeNodePreset;
        this.fogDevices = new ArrayList<>();
        this.edgeNodes = new ArrayList<>();

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

        // Extract all the connections between nodes
        this.nodeConnections = applicationTopologyParser.parseTopologyConnections();

        // Extract all the data flows
        this.dataFlows = applicationTopologyParser.parseTopologyDataFlows();

        // Extract all events used
        this.events = applicationTopologyParser.parseTopologyNodes("events");

        System.out.println("Application Topology Parsed Successfully!");
    }

    public ApplicationPhysicalTopology createPhysicalTopology(List<VirtualDevice> virtualDevices) {
        try {
            // Search for selected VDs based on things list
            List<VirtualDevice> selectedVirtualDevices = getSelectedVirtualDevices(virtualDevices, things);

            // Get the name of all sensors and actuators used in the application
            List<String> sensorsAndActuatorsUsed = getAllSensorsAndActuatorsUsed(topologyNodes);

            // Get all sensors used from selected VDs
            List<Sensor> allSensorsFromVD = new ArrayList<>();
            for(VirtualDevice virtualDevice : selectedVirtualDevices) {
                allSensorsFromVD.addAll(getAllSensorsFrom(virtualDevice));
            }

            // Get all actuators used from selected VDs
            List<Actuator> allActuatorsFromVD = new ArrayList<>();
            for(VirtualDevice virtualDevice : selectedVirtualDevices) {
                allActuatorsFromVD.addAll(getAllActuatorsFrom(virtualDevice));
            }

            // Get all sensors and actuators used from the application
            List<Sensor> allSensorsUsedInApplication = getAllSensorsUsed(allSensorsFromVD, sensorsAndActuatorsUsed);
            List<Actuator> allActuatorsUsedInApplication = getAllActuatorsUsed(allActuatorsFromVD, sensorsAndActuatorsUsed);

            // Create cloud node/device at the top of the hierarchy
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

            if(!nodeTopics.isEmpty()) { // If the topics array is set
                // Assign VDs to edge nodes based on topics
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
            } else { // If the topics array is not set, distribute nodes normally
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

            System.out.println("Application's physical topology formed!");

            ApplicationPhysicalTopology applicationPhysicalTopology = new ApplicationPhysicalTopology();

            applicationPhysicalTopology.setFogDevices(fogDevices);
            applicationPhysicalTopology.setSensors(allSensorsUsedInApplication);
            applicationPhysicalTopology.setActuators(allActuatorsUsedInApplication);
            applicationPhysicalTopology.setEdgeNodes(edgeNodes);

            return applicationPhysicalTopology;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Application createApplication(String appId, int userId) {
        //
        // create application
        //
        // Create modules:
        // - Faux (or a better name) module represents an imaginary module that handles some undefined processing or data transmission to other modules.
        //
        // - client module that represents user input or user-based tuple transmission
        //
        // - Modules for any read-prop, write-prop or invoke-action node given the sub-flow does not start with a sub-event node
        //
        // Add app edges
        // - Modules connected based on node-red connections, so if a read-prop node is connected (directly or indirectly) to a write-prop, their
        // module counterparts will be connected as such.
        //      - The tuple type is the name of the property of the read-prop so the Property: lockState would be TUPLE_LOCK_STATE
        //      - Direction: determineDirection(src, dst)
        //      - edgeType: props as sensors and actions as actuators, everything else is a module
        //      - keep track of module connections and module inputs in a D.S (ModuleConnections)
        //
        // Define tuple mappings:
        // - use ModuleConnection class
        //
        //
        // Define app loops
        // - createAppLoops()
        //
        //
        // Define events
        // - Extend fogDevices to handle events
        // -
        //
        //
        // */


        return null;
    }

    private List<Sensor> getAllSensorsUsed(List<Sensor> sensors, List<String> allComponentsUsed) {
        List<Sensor> allSensorsUsed = new ArrayList<>();
        for (Sensor sensor : sensors) {
            if (allComponentsUsed.contains(sensor.getName())) allSensorsUsed.add(sensor);
        }
        return allSensorsUsed;
    }

    private List<Actuator> getAllActuatorsUsed(List<Actuator> actuators, List<String> allComponentsUsed) {
        List<Actuator> allActuatorsUsed = new ArrayList<>();
        for (Actuator actuator : actuators) {
            if (allComponentsUsed.contains(actuator.getName())) allActuatorsUsed.add(actuator);
        }
        return allActuatorsUsed;
    }

    private List<String> getAllSensorsAndActuatorsUsed(List<TopologyNode> nodes) {
        List<String> attributeNames = new ArrayList<>();
        List<String> includeTypes = new ArrayList<>(){{add("read-property"); add("invoke-action");}}; // Consider including write-props

        for(TopologyNode node : nodes) {
            if(includeTypes.contains(node.type())) attributeNames.add(node.uniqueAttribute());
        }

        return attributeNames;
    }

    private List<Sensor> getAllSensorsFrom(VirtualDevice virtualDevice) {
        List<Sensor> sensorsFromVD = new ArrayList<>();

        if(!virtualDevice.getSensorProperties().isEmpty()) {
            sensorsFromVD.addAll(virtualDevice.getSensorProperties());
        }

        return sensorsFromVD;
    }

    private List<Actuator> getAllActuatorsFrom(VirtualDevice virtualDevice) {
        List<Actuator> actuatorsFromVD = new ArrayList<>();

        if(!virtualDevice.getSensorProperties().isEmpty()) {
            actuatorsFromVD.addAll(virtualDevice.getActuatorActions());
        }

        return actuatorsFromVD;
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
