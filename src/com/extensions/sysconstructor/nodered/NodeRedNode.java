package com.extensions.sysconstructor.nodered;

import java.util.List;

/**
 * Represents a node
 */
public class NodeRedNode {
    private final String id;
    private final String type;
    private final String name;
    private final String thingID;
    private final List<String> connections;

    public NodeRedNode(String id, String type, String name, String thingId, List<String> connections) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.thingID = thingId;
        this.connections = connections;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getThingID() {
        return thingID;
    }

    public List<String> getConnections() {
        return connections;
    }
}
