package com.extensions.sysconstructor.core;

import com.extensions.customfog.FogDeviceFactory;
import com.extensions.sysconstructor.eventdriver.EventDrivenApplication;
import com.extensions.sysconstructor.nodered.NodeRedJSONParser;
import com.extensions.sysconstructor.topology.TopologyNode;
import com.extensions.vdcreation.core.VirtualDevice;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;

import java.util.ArrayList;
import java.util.List;

public class JsonToPhysicalTopology {
    private static ApplicationContext applicationContext;
    public static ApplicationPhysicalTopology createApplicationPhysicalTopology(List<VirtualDevice> virtualDevices, ApplicationContext context) {
        // Set the global variable
        applicationContext = context;

        try {
            // Search for selected VDs based on things list
            List<VirtualDevice> selectedVirtualDevices = getSelectedVirtualDevices(virtualDevices, applicationContext.things);

            // Get the name of all sensors and actuators used in the application
            List<String> sensorsAndActuatorsUsed = getAllSensorsAndActuatorsUsed(applicationContext.topologyNodes);

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
                    applicationContext.cloudNodePreset.MIPS,
                    applicationContext.cloudNodePreset.RAM,
                    applicationContext.cloudNodePreset.UPLINK_BW,
                    applicationContext.cloudNodePreset.DOWN_LINK_BW,
                    applicationContext.cloudNodePreset.LATENCY,
                    applicationContext.cloudNodePreset.RATE_PER_MIPS,
                    applicationContext.cloudNodePreset.BUSY_POWER,
                    applicationContext.cloudNodePreset.IDLE_POWER
            );

            // Cloud has no parent, it is the root of the hierarchy
            cloud.setParentId(-1);

            applicationContext.fogDevices.add(cloud);

            if(!applicationContext.nodeTopics.isEmpty()) { // If the topics array is set
                // Assign VDs to edge nodes based on topics
                for(String topic : applicationContext.nodeTopics) {
                    // Create the edge node for that topic
                    FogDevice edgeNode = createEdgeNode(topic);

                    // Link the edge node to the cloud
                    edgeNode.setParentId(cloud.getId());
                    edgeNode.setUplinkLatency(applicationContext.UPLINK_LATENCY_EDGE_TO_CLOUD);
                    applicationContext.fogDevices.add(edgeNode);
                    applicationContext.edgeNodes.add(edgeNode);

                    // Connect all VDs to this edge node based on the topic
                    for(VirtualDevice virtualDevice : selectedVirtualDevices) {
                        VirtualDevice vd = getVirtualDeviceWithMatchingTopic(applicationContext.topologyNodes, applicationContext.things, virtualDevice, topic);

                        FogDevice vdFogDevice = null;

                        if(vd != null) vdFogDevice = vd.getFogDevice();

                        if(vdFogDevice != null) {
                            vdFogDevice.setParentId(edgeNode.getId());
                            vdFogDevice.setUplinkLatency(applicationContext.UPLINK_LATENCY_VD_TO_EDGE);
                            applicationContext.fogDevices.add(vdFogDevice);
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
                    edgeNode.setUplinkLatency(applicationContext.UPLINK_LATENCY_EDGE_TO_CLOUD);
                    applicationContext.fogDevices.add(edgeNode);
                    applicationContext.edgeNodes.add(edgeNode);

                    // Connect the VDs to the edge node
                    for(VirtualDevice virtualDevice : edgeNodeList.get(index)) {
                        FogDevice vdFogDevice = virtualDevice.getFogDevice();

                        vdFogDevice.setParentId(edgeNode.getId());

                        vdFogDevice.setUplinkLatency(applicationContext.UPLINK_LATENCY_VD_TO_EDGE);

                        applicationContext.fogDevices.add(vdFogDevice);
                    }
                }
            }

            System.out.println("Application's physical topology formed!");

            ApplicationPhysicalTopology applicationPhysicalTopology = new ApplicationPhysicalTopology();

            applicationPhysicalTopology.setFogDevices(applicationContext.fogDevices);
            applicationPhysicalTopology.setSensors(allSensorsUsedInApplication);
            applicationPhysicalTopology.setActuators(allActuatorsUsedInApplication);
            applicationPhysicalTopology.setEdgeNodes(applicationContext.edgeNodes);

            return applicationPhysicalTopology;
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private static List<Sensor> getAllSensorsUsed(List<Sensor> sensors, List<String> allComponentsUsed) {
        List<Sensor> allSensorsUsed = new ArrayList<>();
        for (Sensor sensor : sensors) {
            if (allComponentsUsed.contains(sensor.getName())) allSensorsUsed.add(sensor);
        }
        return allSensorsUsed;
    }

    private static List<Actuator> getAllActuatorsUsed(List<Actuator> actuators, List<String> allComponentsUsed) {
        List<Actuator> allActuatorsUsed = new ArrayList<>();
        for (Actuator actuator : actuators) {
            if (allComponentsUsed.contains(actuator.getName())) allActuatorsUsed.add(actuator);
        }
        return allActuatorsUsed;
    }

    private static List<String> getAllSensorsAndActuatorsUsed(List<TopologyNode> nodes) {
        List<String> attributeNames = new ArrayList<>();
        List<String> includeTypes = new ArrayList<>(){{add("read-property"); add("invoke-action");}}; // Consider including write-props

        for(TopologyNode node : nodes) {
            if(includeTypes.contains(node.type())) attributeNames.add(node.uniqueAttribute());
        }

        return attributeNames;
    }

    private static List<Sensor> getAllSensorsFrom(VirtualDevice virtualDevice) {
        List<Sensor> sensorsFromVD = new ArrayList<>();

        if(!virtualDevice.getSensorProperties().isEmpty()) {
            sensorsFromVD.addAll(virtualDevice.getSensorProperties());
        }

        return sensorsFromVD;
    }

    private static List<Actuator> getAllActuatorsFrom(VirtualDevice virtualDevice) {
        List<Actuator> actuatorsFromVD = new ArrayList<>();

        if(!virtualDevice.getSensorProperties().isEmpty()) {
            actuatorsFromVD.addAll(virtualDevice.getActuatorActions());
        }

        return actuatorsFromVD;
    }

    private static int calculateNoOfEdgeNodes(int numberOfVDs, int maxNoVDsForOneEdgeNode) {
        // CHANGE FORMULA AS YOU SEE FIT
        return (int)(numberOfVDs - maxNoVDsForOneEdgeNode) / 2;
    }

    private static List<VirtualDevice> getSelectedVirtualDevices(List<VirtualDevice> virtualDevices, List<TopologyNode> things) {
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

    private static FogDevice createEdgeNode(String identifier) {
        return FogDeviceFactory.createFogDevice(
                "edgeNode-" + identifier,
                applicationContext.edgeNodePreset.MIPS,
                applicationContext.edgeNodePreset.RAM,
                applicationContext.edgeNodePreset.UPLINK_BW,
                applicationContext.edgeNodePreset.DOWN_LINK_BW,
                applicationContext.edgeNodePreset.LATENCY,
                applicationContext.edgeNodePreset.RATE_PER_MIPS,
                applicationContext.edgeNodePreset.BUSY_POWER,
                applicationContext.edgeNodePreset.IDLE_POWER
        );
    }

    private static VirtualDevice getVirtualDeviceWithMatchingTopic(List<TopologyNode> nodes, List<TopologyNode> things, VirtualDevice virtualDevice, String targetTopic) {
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
