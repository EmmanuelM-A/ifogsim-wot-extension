package com.extensions.sysconstructor.nodered;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Groups sub-flows into trees
 */
public class TreeGrouping {
    private List<NodeRedNode> nodes;

    public TreeGrouping(List<NodeRedNode> nodes) {
        this.nodes = nodes;
    }

    public List<Tree> groupNodesIntoTrees() {
        List<Tree> trees = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for (NodeRedNode node : nodes) {
            if (!visited.contains(node.getId())) {
                List<NodeRedNode> treeNodes = new ArrayList<>();
                traverseTree(node, visited, treeNodes);
                trees.add(new Tree("Tree-" + trees.size(), treeNodes));
            }
        }

        return trees;
    }

    private void traverseTree(NodeRedNode node, Set<String> visited, List<NodeRedNode> treeNodes) {
        if (visited.contains(node.getId())) return;
        if(node.getConnections() == null) return;

        visited.add(node.getId());
        treeNodes.add(node);

        for (String wire : node.getConnections()) {
            NodeRedNode connectedNode = findNodeById(wire);
            if (connectedNode != null) {
                traverseTree(connectedNode, visited, treeNodes);
            }
        }
    }

    private NodeRedNode findNodeById(String id) {
        for (NodeRedNode node : nodes) {
            if(node.getId() == null) continue;
            if (node.getId().equals(id)) {
                return node;
            }
        }
        return null;
    }
}
