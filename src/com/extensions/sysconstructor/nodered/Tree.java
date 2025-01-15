package com.extensions.sysconstructor.nodered;

import java.util.List;

public class Tree {
    private final String treeId;
    private final List<NodeRedNode> nodes;

    public Tree(String treeId, List<NodeRedNode> nodes) {
        this.treeId = treeId;
        this.nodes = nodes;
    }

    public String getTreeId() {
        return treeId;
    }

    public List<NodeRedNode> getNodes() {
        return nodes;
    }
}
