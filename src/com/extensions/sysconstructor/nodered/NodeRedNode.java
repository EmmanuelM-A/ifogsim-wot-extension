package com.extensions.sysconstructor.nodered;

import java.util.List;

/**
 * Represents a Node-RED node, encapsulating its ID, type, name, topic, associated Thing ID,
 * unique attribute, and connections.
 */
public class NodeRedNode {
    /**
     * The node red ID assigned to the node.
     */
    private final String id;

    /**
     * The type of node.
     */
    private final String type;

    /**
     * The name of the node.
     */
    private final String name;

    /**
     * The topic associated with the node.
     */
    private final String topic;

    /**
     * The thing ID referenced by the node.
     */
    private final String thingID;

    /**
     * The unique attribute the node posses.
     */
    private final String uniqueAttribute;

    /**
     * The list connections to other nodes from that node.
     */
    private final List<String> connections;

    /**
     * Constructs a Node-RED node with the given properties.
     *
     * @param id              The unique identifier of the node.
     * @param type            The type of the node (e.g., function, event, property).
     * @param name            The name of the node.
     * @param topic           The topic associated with the node, if applicable.
     * @param thingId         The Thing ID this node is associated with.
     * @param uniqueAttribute A unique attribute relevant to the node type.
     * @param connections     A list of IDs representing the connections to other nodes.
     */
    public NodeRedNode(String id, String type, String name, String topic, String thingId, String uniqueAttribute, List<String> connections) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.topic = topic;
        this.thingID = thingId;
        this.uniqueAttribute = uniqueAttribute;
        this.connections = connections;
    }

    /**
     * Gets the unique identifier of the node.
     *
     * @return The node's ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the type of the node.
     *
     * @return The node's type.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the name of the node.
     *
     * @return The node's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the topic associated with the node.
     *
     * @return The node's topic, or null if not applicable.
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Gets the Thing ID that this node is associated with.
     *
     * @return The Thing ID.
     */
    public String getThingID() {
        return thingID;
    }

    /**
     * Gets the list of connections to other nodes.
     *
     * @return A list of node IDs that this node is connected to.
     */
    public List<String> getConnections() {
        return connections;
    }

    /**
     * Gets the unique attribute of the node.
     *
     * @return The node's unique attribute.
     */
    public String getUniqueAttribute() {
        return uniqueAttribute;
    }
}