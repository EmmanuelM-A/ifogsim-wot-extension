package com.extensions.sysconstructor.topology;

import java.util.*;

public class TopologyNodeConnectionChecker {
    private final Map<String, Set<String>> adjacencyList;
    private final Map<String, TopologyNode> nodeLookup;

    public TopologyNodeConnectionChecker(List<TopologyNodeConnection> connections, List<TopologyNode> nodes) {
        this.adjacencyList = new HashMap<>();
        this.nodeLookup = new HashMap<>();

        for (TopologyNode node : nodes) {
            nodeLookup.put(node.id(), node);
        }

        for (TopologyNodeConnection connection : connections) {
            adjacencyList
                    .computeIfAbsent(connection.source(), k -> new HashSet<>())
                    .add(connection.destination());
            // If it's undirected, uncomment the line below
            // adjacencyList.computeIfAbsent(connection.destination(), k -> new HashSet<>()).add(connection.source());
        }
    }

    public TopologyNodeConnectionStatus areNodesConnected(String identifierA, String identifierB, String criteria) {
        Set<String> matchingNodesA = findNodesMatching(identifierA, criteria);
        Set<String> matchingNodesB = findNodesMatching(identifierB, criteria);

        if (matchingNodesA.isEmpty() || matchingNodesB.isEmpty()) {
            return new TopologyNodeConnectionStatus(false, false); // No matching nodes for the given criteria
        }

        boolean isConnected = false;
        boolean isDirectlyConnected = false;

        // Check if any node in set A is connected to any node in set B
        for (String nodeA : matchingNodesA) {
            Set<String> visited = new HashSet<>();
            for (String nodeB : matchingNodesB) {
                if (nodeA.equals(nodeB)) {
                    return new TopologyNodeConnectionStatus(true, true); // Same node case
                }
                if (adjacencyList.getOrDefault(nodeA, Collections.emptySet()).contains(nodeB)) {
                    isDirectlyConnected = true; // Immediate direct connection found
                }
                if (dfs(nodeA, nodeB, visited)) {
                    isConnected = true; // Indirect connection found
                }
                if (isConnected && isDirectlyConnected) {
                    return new TopologyNodeConnectionStatus(true, true); // Both conditions met
                }
            }
        }

        return new TopologyNodeConnectionStatus(isConnected, isDirectlyConnected);
    }

    private Set<String> findNodesMatching(String identifier, String criteria) {
        Set<String> matchingNodes = new HashSet<>();
        for (TopologyNode node : nodeLookup.values()) {
            switch (criteria.toLowerCase()) {
                case "id" -> {
                    if (node.id().equals(identifier)) matchingNodes.add(node.id());
                }
                case "name" -> {
                    if (node.name().equalsIgnoreCase(identifier)) matchingNodes.add(node.id());
                }
                case "type" -> {
                    if (node.type().equalsIgnoreCase(identifier)) matchingNodes.add(node.id());
                }
                case "topic" -> {
                    if (node.topic().equalsIgnoreCase(identifier)) matchingNodes.add(node.id());
                }
            }
        }
        return matchingNodes;
    }

    // Depth-First Search for connectivity
    private boolean dfs(String current, String target, Set<String> visited) {
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

