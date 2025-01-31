package com.extensions.utils;

import com.extensions.sysconstructor.topology.TopologyNode;
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
}
