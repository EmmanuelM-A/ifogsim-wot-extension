package com.extensions.sysconstructor.nodered;

import java.util.List;

public class SubFlowTree {
    private final NodeRedNode rootNode;
    private final List<TreeBranch> branches;

    public SubFlowTree(NodeRedNode rootNode, List<TreeBranch> branches) {
        this.rootNode = rootNode;
        this.branches = branches;
    }

    public NodeRedNode getRootNode() {
        return rootNode;
    }

    public List<TreeBranch> getBranches() {
        return branches;
    }
}
