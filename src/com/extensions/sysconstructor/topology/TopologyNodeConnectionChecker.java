package com.extensions.sysconstructor.topology;

import java.util.*;

/**
 * This class is responsible for checking for connectivity between a source and destination.
 */
public class TopologyNodeConnectionChecker {
    /**
     * Stores the node connections as an adjacency list.
     */
    private static Map<String, Set<String>> adjacencyList; // Stores node connections

    /**
     * Maps the node IDs to actual nodes.
     */
    private static Map<String, TopologyNode> nodeLookup;

    /**
     * Initializes the checker, by setting up adjacency list and node lookup table using the topology node trees.
     * @param topologyNodeTrees A list of all topology node tress.
     */
    public static void initializeChecker(List<TopologyNodeTree> topologyNodeTrees) {
        adjacencyList = new HashMap<>();
        nodeLookup = new HashMap<>();

        for (TopologyNodeTree tree : topologyNodeTrees) {
            processTopologyNodeTree(tree);
        }
    }

    /**
     * Processes each tree by extracting its root node and branches.
     * @param tree The topology node tree being processed.
     */
    private static void processTopologyNodeTree(TopologyNodeTree tree) {
        TopologyNode rootNode = tree.rootNode();
        nodeLookup.put(rootNode.id(), rootNode);
        buildAdjacencyList(tree.branches());
    }

    /**
     * Builds the adjacency list from the topology node branches.
     * @param branches The branches list from the topology node tree.
     */
    private static void buildAdjacencyList(List<List<TopologyNode>> branches) {
        for (List<TopologyNode> branch : branches) {
            for (int i = 0; i < branch.size() - 1; i++) {
                TopologyNode current = branch.get(i);
                TopologyNode next = branch.get(i + 1);
                adjacencyList.computeIfAbsent(current.id(), k -> new HashSet<>()).add(next.id());
                nodeLookup.putIfAbsent(current.id(), current);
                nodeLookup.putIfAbsent(next.id(), next);
            }
        }
    }

    /**
     * Checks if two nodes are connected using their unique IDs.
     * It checks both direct and indirect connections between the nodes.
     *
     * @param nodeIdA The ID of the first node.
     * @param nodeIdB The ID of the second node.
     * @return A TopologyNodeConnectionStatus indicating if the nodes are connected, and if they are directly connected.
     */
    public static TopologyNodeConnectionStatus areNodesConnected(String nodeIdA, String nodeIdB) {
        if (!nodeLookup.containsKey(nodeIdA) || !nodeLookup.containsKey(nodeIdB)) {
            return new TopologyNodeConnectionStatus(false, false);
        }

        boolean isConnected = false;
        boolean isDirectlyConnected = false;

        // If both IDs are the same, they are trivially connected
        if (nodeIdA.equals(nodeIdB)) {
            return new TopologyNodeConnectionStatus(true, true);
        }

        // Check for direct connection
        if (adjacencyList.getOrDefault(nodeIdA, Collections.emptySet()).contains(nodeIdB)) {
            isDirectlyConnected = true;
        }

        // Use DFS to check for any path between the nodes (indirect connection)
        if (dfs(nodeIdA, nodeIdB, new HashSet<>())) {
            isConnected = true;
        }

        return new TopologyNodeConnectionStatus(isConnected, isDirectlyConnected);
    }

    /**
     * Depth-first search (DFS) to check if there is a path from the current node to the target node.
     * This method recursively explores neighboring nodes to find a connection.
     *
     * @param current The current node being explored.
     * @param target The target node we are trying to find a path to.
     * @param visited A set of visited nodes to avoid cycles and infinite recursion.
     * @return true if there is a path from the current node to the target, false otherwise.
     */
    private static boolean dfs(String current, String target, Set<String> visited) {
        // If we have reached the target node, return true
        if (current.equals(target)) {
            return true;
        }

        // If the current node has already been visited, return false to avoid cycles
        if (visited.contains(current)) {
            return false;
        }

        // Mark the current node as visited
        visited.add(current);

        // Get the neighboring nodes of the current node
        Set<String> neighbors = adjacencyList.getOrDefault(current, Collections.emptySet());

        // Recursively check all neighbors
        for (String neighbor : neighbors) {
            if (dfs(neighbor, target, visited)) {
                return true;
            }
        }

        // If no path is found, return false
        return false;
    }
}