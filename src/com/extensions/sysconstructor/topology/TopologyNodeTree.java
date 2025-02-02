package com.extensions.sysconstructor.topology;

import java.util.List;

public record TopologyNodeTree(TopologyNode rootNode, List<List<TopologyNode>> branches) {
}
