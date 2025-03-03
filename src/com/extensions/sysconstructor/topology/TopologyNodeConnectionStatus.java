package com.extensions.sysconstructor.topology;

/**
 * Determines the type of connection between two nodes.
 * @param isThereAConnection Determines if there exists some connection between two nodes weather its direct or indirect.
 * @param isDirectionConnection Determines if there exists a direct connection between two nodes.
 */
public record TopologyNodeConnectionStatus(boolean isThereAConnection, boolean isDirectionConnection) {}
