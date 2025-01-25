package com.extensions.sysconstructor.core;

import org.fog.entities.FogDevice;
import org.fog.entities.PhysicalTopology;

import java.util.List;

public class ApplicationPhysicalTopology extends PhysicalTopology {
    private List<FogDevice> edgeNodes;

    public List<FogDevice> getEdgeNodes() {
        return edgeNodes;
    }

    public void setEdgeNodes(List<FogDevice> edgeNodes) {
        this.edgeNodes = edgeNodes;
    }
}
