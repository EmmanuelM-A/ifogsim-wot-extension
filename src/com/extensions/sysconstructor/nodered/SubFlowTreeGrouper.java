package com.extensions.sysconstructor.nodered;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Groups Node-RED nodes into sub-flow trees based on their connections. The class identifies potential root nodes,
 * traverses through their connected nodes, and groups them into tree-like structures representing sub-flows.
 */
public class SubFlowTreeGrouper {
    /**
     * A list of all NodeRedNodes extracted from the raw Node-RED application JSON.
     */
    private static List<NodeRedNode> nodes;

    /**
     * A list of node types that should be ignored when grouping nodes into sub-flows.
     * These types are not considered as part of sub-flow trees.
     */
    private static final List<String> nodeTypesToIgnore = new ArrayList<>() {{
        //add("debug"); // Uncomment if "debug" nodes should be ignored.
        add("tab");
        add("comment");
        add("consumed-thing");
    }};

    /**
     * Groups the nodes into tree-like sub-flows (similar to how they are structured in Node-RED).
     *
     * @param nodeRedNodes The list of Node-RED nodes to be grouped.
     * @return A list of sub-flow trees, each containing a root node and its respective branches.
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

    /**
     * Recursively traverses a sub-flow starting from a given root node.
     * For each node, it explores its connections and groups the connected nodes into branches.
     *
     * @param node The current node to be traversed.
     * @param visited A set of node IDs that have already been visited.
     * @param branches The list of branches in the current sub-flow tree.
     * @param currentBranch The current branch being traversed.
     */
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

    /**
     * Finds a node by its ID from the list of NodeRedNodes.
     *
     * @param id The ID of the node to be found.
     * @return The NodeRedNode corresponding to the given ID, or null if no node is found.
     */
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
