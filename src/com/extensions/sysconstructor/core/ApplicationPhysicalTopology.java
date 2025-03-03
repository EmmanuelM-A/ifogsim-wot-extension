package com.extensions.sysconstructor.core;

import org.fog.entities.FogDevice;
import org.fog.entities.PhysicalTopology;

import java.util.List;

/**
 * Represents the physical topology of an application in the iFogSim simulation. This class extends the {@link PhysicalTopology} class
 * to include a list of edge nodes.
 */
public class ApplicationPhysicalTopology extends PhysicalTopology {
    /**
     * A list of edge nodes in the physical topology.
     */
    private List<FogDevice> edgeNodes;

    /**
     * Retrieves the list of edge nodes in the physical topology.
     *
     * @return The list of edge nodes.
     */
    public List<FogDevice> getEdgeNodes() {
        return edgeNodes;
    }

    /**
     * Sets the list of edge nodes in the physical topology.
     *
     * @param edgeNodes The list of edge nodes to set.
     */
    public void setEdgeNodes(List<FogDevice> edgeNodes) {
        this.edgeNodes = edgeNodes;
    }
}
