package com.extensions.utils;

import com.extensions.sysconstructor.core.TupleMapping;
import com.extensions.sysconstructor.eventdriver.EventDrivenApplication;
import com.extensions.sysconstructor.topology.TopologyNode;
import com.extensions.sysconstructor.topology.TopologyNodeTree;
import com.extensions.vdcreation.core.VirtualDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utility {
    public static VirtualDevice getVirtualDevice(List<VirtualDevice> virtualDevices, String name) {
        for(VirtualDevice virtualDevice : virtualDevices) {
            if(virtualDevice.getFogDevice().getName().equals(name)) return virtualDevice;
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
                System.out.println("    Branch:");
                for (TopologyNode node : branch) {
                    printTopologyNode(node);
                }
            }
            System.out.println("------------------------------------------------------");
        }
    }

    public static List<TopologyNode> getAllNodesFromTopology(List<TopologyNodeTree> topologyNodeTrees) {
        List<TopologyNode> allNodes = new ArrayList<>();

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

        return allNodes;
    }

    public static void printAppLoops(List<List<String>> appLoops) {
        if (appLoops == null || appLoops.isEmpty()) {
            System.out.println("App loops are empty or null.");
            return;
        }

        for (int i = 0; i < appLoops.size(); i++) {
            List<String> route = appLoops.get(i);
            System.out.print("Route " + (i + 1) + ": ");  // Or just "Route:" if you prefer

            if (route.isEmpty()) {
                System.out.println("[]"); // Print empty route representation
            } else {
                System.out.print("[");
                for (int j = 0; j < route.size(); j++) {
                    System.out.print(route.get(j));
                    if (j < route.size() - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.println("]");
            }
        }
    }
}
