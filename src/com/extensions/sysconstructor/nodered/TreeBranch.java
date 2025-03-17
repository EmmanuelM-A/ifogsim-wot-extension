package com.extensions.sysconstructor.nodered;

import java.util.List;

/**
 * Represents a branch in a sub-flow tree.
 * A branch contains a list of NodeRedNodes that are connected in a sequence.
 * @param nodes The list of node red nodes that make up the branch.
 */
public record TreeBranch(List<NodeRedNode> nodes) {
}
