package com.extensions.utils;

import com.extensions.sysconstructor.topology.TopologyNode;
import com.extensions.sysconstructor.topology.TopologyNodeTree;
import com.extensions.vdcreation.core.VirtualDevice;
import org.fog.application.AppLoop;

import java.util.*;

public class Utility {
    public static void printVirtualDevices(List<VirtualDevice> virtualDevices, String identifier) {
        System.out.println("----------------------------------------------");
        System.out.println("Identifier: " + identifier + " | Virtual Devices: ");
        for(VirtualDevice virtualDevice : virtualDevices) {
            if(virtualDevice != null) {
                System.out.println("[Virtual Device] " + virtualDevice.getFogDevice().getName());
            } else {
                System.out.println("[Virtual Device] null!");
            }
        }
        System.out.println("----------------------------------------------");
    }

    public static VirtualDevice getVirtualDevice(List<VirtualDevice> virtualDevices, String name) {
        for(VirtualDevice virtualDevice : virtualDevices) {
            if(virtualDevice.getFogDevice().getName().equals(name)) return virtualDevice;
        }
        return null;
    }

    public static VirtualDevice getVirtualDeviceThatMatches(String criteria, List<VirtualDevice> virtualDevices) {
        for(VirtualDevice virtualDevice : virtualDevices) {
            if(virtualDevice.getFogDevice().getName().contains(criteria)) return virtualDevice;
        }
        return null;
    }
    /*
    *   {
            "access-control": {
                "things": {
                    "Smart Door Lock": 2,
                    "Smart Display": 1
                }
            },
            "security-monitoring": {
                "things": {
                    "Smart Camera": 3,
                    "Alarm": 1,
                    "Smart Display" : 1
                }
            }
        }
    *
    *
    * */

    public static VirtualDevice getVirtualDevice(List<TopologyNode> things, List<VirtualDevice> virtualDevices, TopologyNode node) {
        String thingNode = null;

        if(node.thing() == null || node.thing().isEmpty()) return null;

        for(TopologyNode thing : things) {
            if(thing.id().equals(node.thing())) thingNode = thing.name();
        }

        for(VirtualDevice virtualDevice : virtualDevices) {
            if(virtualDevice.getFogDevice().getName().equals(thingNode)) {
                return virtualDevice;
            }
        }

        return null;
    }

    public static TopologyNode findTopologyNodeBy(String criteria, String target, List<TopologyNode> allNodes) {
        if (allNodes == null || allNodes.isEmpty() || criteria == null || target == null) {
            return null; // Handle null or empty input
        }

        TopologyNode targetNode = null;
        switch (criteria) {
            case "id":
                for (TopologyNode topologyNode : allNodes) {
                    if (target.equals(topologyNode.id())) { // Use .equals() for String comparison
                        targetNode = topologyNode;
                        break; // Exit loop once found
                    }
                }
                break;
            case "name":
                for (TopologyNode topologyNode : allNodes) {
                    if (target.equals(topologyNode.name())) {
                        targetNode = topologyNode;
                        break;
                    }
                }
                break;
            case "type":
                for (TopologyNode topologyNode : allNodes) {
                    if (target.equals(topologyNode.type())) {
                        targetNode = topologyNode;
                        break;
                    }
                }
                break;
            case "thing":
                for (TopologyNode topologyNode : allNodes) {
                    if (target.equals(topologyNode.thing())) {
                        targetNode = topologyNode;
                        break;
                    }
                }
                break;
            case "uniqueAttribute":
                for (TopologyNode topologyNode : allNodes) {
                    if (target.equals(topologyNode.uniqueAttribute())) {
                        targetNode = topologyNode;
                        break;
                    }
                }
                break;
            case "subFlowId":
                for (TopologyNode topologyNode : allNodes) {
                    if (target.equals(topologyNode.subFlowId())) {
                        targetNode = topologyNode;
                        break;
                    }
                }
                break;
            // Add more cases as needed (topic, etc.)
            default:
                System.out.println("Unknown criteria: " + criteria); // Or throw an exception
        }
        return targetNode;
    }

    public static List<TopologyNode> getTopologyNodesByType(List<TopologyNode> topologyNodes, String type) {
        List<TopologyNode> nodes = new ArrayList<>();
        for(TopologyNode topologyNode : topologyNodes) {
            if(topologyNode.type().equals(type)) nodes.add(topologyNode);
        }
        return nodes;
    }

    public static TopologyNode getTopologyNode(List<TopologyNode> topologyNodes, String nodeName) {
        for(TopologyNode node : topologyNodes) {
            if(node.name().equals(nodeName)) return node;
        }
        return null;
    }

    public static void printNodes(List<TopologyNode> nodes) {
        System.out.println("=== Topology Nodes ===");

        if (nodes == null || nodes.isEmpty()) {
            System.out.println("No nodes available");
            return;
        }

        for (TopologyNode node : nodes) {
            printTopologyNode(node);
        }
    }

    public static void printTopologyNode(TopologyNode node) {
        System.out.println("------------------------------");
        System.out.println("ID: " + node.id());
        System.out.println("Name: " + node.name());
        System.out.println("Type: " + node.type());
        System.out.println("Topic: " + node.topic());
        System.out.println("Thing: " + node.thing());
        System.out.println("Unique Attribute: " + node.uniqueAttribute());
        System.out.println("------------------------------");
    }

    public static void printTopologyNodeTrees(List<TopologyNodeTree> nodeTrees) {
        for (TopologyNodeTree nodeTree : nodeTrees) {
            System.out.println("Root Node:");
            printTopologyNode(nodeTree.rootNode());

            System.out.println("Branches:");
            for (List<TopologyNode> branch : nodeTree.branches()) {
                System.out.println("--->Branch:");
                for (TopologyNode node : branch) {
                    printTopologyNode(node);
                }
            }
            System.out.println("------------------------------------------------------");
        }
    }

    public static List<TopologyNode> getAllNodesFromTopology(List<TopologyNodeTree> topologyNodeTrees) {
        Set<TopologyNode> allNodes = new HashSet<>();

        for (TopologyNodeTree topologyNodeTree : topologyNodeTrees) {
            // Add the root node
            if (topologyNodeTree.rootNode() != null) {
                allNodes.add(topologyNodeTree.rootNode());
            }

            // Add all nodes from branches
            for (List<TopologyNode> branch : topologyNodeTree.branches()) {
                allNodes.addAll(branch);
            }
        }

        return new ArrayList<>(allNodes);
    }

    public static String formAppLoop(AppLoop appLoop) {
        StringBuilder loop = new StringBuilder("[");

        for (String appModule : appLoop.getModules()) {
            loop.append(" ").append(appModule);
        }

        loop.append(" ]");
        return loop.toString();
    }

    public static void printAppLoops(List<AppLoop> appLoops) {
        if(appLoops == null) {
            System.out.println("App loops are  null!");
            return;
        }

        if(appLoops.isEmpty()) {
            System.out.println("App loops are empty!");
            return;
        }

        for (int i = 0; i < appLoops.size(); i++) {
            AppLoop appLoop = appLoops.get(i);

            System.out.print("Loop " + (i + 1) + ": ");
            System.out.println(formAppLoop(appLoop));
        }
    }

}