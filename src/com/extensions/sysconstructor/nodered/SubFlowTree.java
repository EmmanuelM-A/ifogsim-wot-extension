package com.extensions.sysconstructor.nodered;

import java.util.List;

/**
 * Represents a sub-flow tree, consisting of a root node and a list of branches.
 * A sub-flow tree is used to represent the structure and flow of a specific Node-RED sub-flow.
 */
public record SubFlowTree(NodeRedNode rootNode, List<TreeBranch> branches) {
}
