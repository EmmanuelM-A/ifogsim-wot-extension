package com.extensions.utils;

import com.extensions.sysconstructor.topology.TopologyNode;
import com.extensions.sysconstructor.topology.TopologyNodeTree;
import com.extensions.vdcreation.core.VirtualDevice;

import java.util.ArrayList;
import java.util.List;

public class Utility {
    public static VirtualDevice getVirtualDevice(List<VirtualDevice> virtualDevices, String name) {
        for(VirtualDevice virtualDevice : virtualDevices) {
            if(virtualDevice.getFogDevice().getName().equals(name)) return virtualDevice;
        }
        return null;
    }

    public static String getTopologyNodeIdByName(List<TopologyNode> topologyNodes, String name) {
        for(TopologyNode topologyNode : topologyNodes) {
            if(topologyNode.name().equals(name)) return topologyNode.id();
        }
        return null;
    }

    public static String getTopologyNodeIdByType(List<TopologyNode> topologyNodes, String type) {
        for(TopologyNode topologyNode : topologyNodes) {
            if(topologyNode.type().equals(type)) return topologyNode.id();
        }
        return null;
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

}
