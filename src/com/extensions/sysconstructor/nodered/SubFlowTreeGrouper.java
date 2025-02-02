package com.extensions.sysconstructor.nodered;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Groups nodes into trees/sub-flows
 */
public class SubFlowTreeGrouper {
    /**
     * A list of all NodeRedNodes extracted from the raw node red application json.
     */
    private static List<NodeRedNode> nodes;

    private static final List<String> nodeTypesToIgnore = new ArrayList<>(){{
        //add("debug");
        add("tab");
        add("comment");
        add("consumed-thing");
    }};

    /**
     * Groups the nodes into tree-like sub-flows (similar to how it's found in node red)
     * @param nodeRedNodes The node red nodes
     * @return A list of sub-flows
     */
    public static List<SubFlowTree> groupNodesIntoSubFlowTree(List<NodeRedNode> nodeRedNodes) {
        // Assign the global variable
        nodes = nodeRedNodes;
        List<SubFlowTree> subFlowTrees = new ArrayList<>();

        // 1. Identify potential root nodes (nodes with no incoming connections)
        Set<String> allConnections = new HashSet<>();
        for (NodeRedNode node : nodes) {
            if (node.getConnections() != null) {
                allConnections.addAll(node.getConnections());
            }
        }
        List<NodeRedNode> potentialRoots = new ArrayList<>();
        for (NodeRedNode node : nodes) {
            if (!allConnections.contains(node.getId()) && !nodeTypesToIgnore.contains(node.getType())) {
                potentialRoots.add(node);
            }
        }

        // 2. Build trees starting from the identified roots
        Set<String> visited = new HashSet<>();
        for (NodeRedNode root : potentialRoots) {
            if (!visited.contains(root.getId())) {
                List<TreeBranch> branches = new ArrayList<>();
                traverseSubFlow(root, visited, branches, new ArrayList<>());
                subFlowTrees.add(new SubFlowTree(root, branches));
            }
        }

        return subFlowTrees;
    }

    private static void traverseSubFlow(NodeRedNode node, Set<String> visited, List<TreeBranch> branches, List<NodeRedNode> currentBranch) {
        if (visited.contains(node.getId())) return;
        if (node.getConnections() == null || node.getConnections().isEmpty()) {
            currentBranch.add(node);
            branches.add(new TreeBranch(new ArrayList<>(currentBranch)));
            return;
        }

        visited.add(node.getId());
        currentBranch.add(node);

        for (String wire : node.getConnections()) {
            NodeRedNode connectedNode = findNodeById(wire);
            if (connectedNode != null) {
                traverseSubFlow(connectedNode, visited, branches, new ArrayList<>(currentBranch));
            }
        }
    }

    private static NodeRedNode findNodeById(String id) {
        for (NodeRedNode node : nodes) {
            if (node.getId() == null) continue;
            if (node.getId().equals(id)) {
                return node;
            }
        }
        return null;
    }
}
