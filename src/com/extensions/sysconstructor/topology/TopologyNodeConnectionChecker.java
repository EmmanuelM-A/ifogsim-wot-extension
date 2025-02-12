package com.extensions.sysconstructor.topology;

import java.util.*;

public class TopologyNodeConnectionChecker {
    private static Map<String, Set<String>> adjacencyList; // Stores node connections
    private static Map<String, TopologyNode> nodeLookup; // Maps node IDs to actual nodes

    public static void initializeChecker(List<TopologyNodeTree> topologyNodeTrees) {
        adjacencyList = new HashMap<>();
        nodeLookup = new HashMap<>();

        for (TopologyNodeTree tree : topologyNodeTrees) {
            processTopologyNodeTree(tree);
        }
    }

    // Processes each tree by extracting its root node and branches
    private static void processTopologyNodeTree(TopologyNodeTree tree) {
        TopologyNode rootNode = tree.rootNode();
        nodeLookup.put(rootNode.id(), rootNode);
        buildAdjacencyList(rootNode, tree.branches());
    }

    // Builds adjacency list from branches
    private static void buildAdjacencyList(TopologyNode rootNode, List<List<TopologyNode>> branches) {
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

    // Checks if two nodes are connected using their unique IDs
    public static TopologyNodeConnectionStatus areNodesConnected(String nodeIdA, String nodeIdB) {
        if (!nodeLookup.containsKey(nodeIdA) || !nodeLookup.containsKey(nodeIdB)) {
            return new TopologyNodeConnectionStatus(false, false);
        }

        boolean isConnected = false;
        boolean isDirectlyConnected = false;

        if (nodeIdA.equals(nodeIdB)) {
            return new TopologyNodeConnectionStatus(true, true);
        }
        if (adjacencyList.getOrDefault(nodeIdA, Collections.emptySet()).contains(nodeIdB)) {
            isDirectlyConnected = true;
        }
        if (dfs(nodeIdA, nodeIdB, new HashSet<>())) {
            isConnected = true;
        }

        return new TopologyNodeConnectionStatus(isConnected, isDirectlyConnected);
    }

    // Depth-first search (DFS) to check connectivity
    private static boolean dfs(String current, String target, Set<String> visited) {
        if (current.equals(target)) {
            return true;
        }
        if (visited.contains(current)) {
            return false;
        }

        visited.add(current);

        Set<String> neighbors = adjacencyList.getOrDefault(current, Collections.emptySet());
        for (String neighbor : neighbors) {
            if (dfs(neighbor, target, visited)) {
                return true;
            }
        }

        return false;
    }
}