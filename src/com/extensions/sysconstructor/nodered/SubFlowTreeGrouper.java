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
        add("debug");
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

        // Create a list to represent all sub-flows in the application
        List<SubFlowTree> subFlowTrees = new ArrayList<>();

        // Used to keep track of which nodes have been visited
        Set<String> visited = new HashSet<>();

        for (NodeRedNode node : nodes) {
            if(nodeTypesToIgnore.contains(node.getType())) continue; // Skip

            if (!visited.contains(node.getId())) {
                List<TreeBranch> branches = new ArrayList<>();
                traverseSubFlow(node, visited, branches, new ArrayList<>());
                subFlowTrees.add(new SubFlowTree(node, branches));
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
