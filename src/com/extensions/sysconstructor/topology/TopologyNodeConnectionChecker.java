package com.extensions.sysconstructor.topology;

import java.util.*;

public class TopologyNodeConnectionChecker {
    private Map<String, Set<String>> adjacencyList;

    public TopologyNodeConnectionChecker(List<TopologyNodeConnection> connections) {
        this.adjacencyList = new HashMap<>();

        for (TopologyNodeConnection connection : connections) {
            adjacencyList
                    .computeIfAbsent(connection.source(), k -> new HashSet<>())
                    .add(connection.destination());
            // If it's undirected, uncomment the line below
            // adjacencyList.computeIfAbsent(connection.getDestination(), k -> new HashSet<>()).add(connection.getSource());
        }
    }

    public boolean areNodesConnected(String nodeA, String nodeB) {
        if (!adjacencyList.containsKey(nodeA)) {
            return false; // No connections from nodeA
        }

        // Perform DFS to check connectivity
        Set<String> visited = new HashSet<>();
        return dfs(nodeA, nodeB, visited);
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
