package com.extensions.sysconstructor.nodered;

import java.util.List;

/**
 * Represents a node from node red
 */
public class NodeRedNode {
    private final String id;
    private final String type;
    private final String name;
    private final String topic;
    private final String thingID;
    private final String uniqueAttribute;
    private final List<String> connections;

    public NodeRedNode(String id, String type, String name, String topic, String thingId, String uniqueAttribute, List<String> connections) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.topic = topic;
        this.thingID = thingId;
        this.uniqueAttribute = uniqueAttribute;
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

    public String getTopic() {
        return topic;
    }

    public String getThingID() {
        return thingID;
    }

    public List<String> getConnections() {
        return connections;
    }

    public String getUniqueAttribute() {
        return uniqueAttribute;
    }
}
