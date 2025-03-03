package com.extensions.sysconstructor.topology;

import java.util.List;

/**
 * Represents a sub-flow as tree based data structure with a root node and branches.
 * @param rootNode The root node of the tree.
 * @param branches A list of branches (which are a list of topology nodes)
 */
public record TopologyNodeTree(TopologyNode rootNode, List<List<TopologyNode>> branches) {}
