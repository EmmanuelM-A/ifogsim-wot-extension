package com.extensions.sysconstructor.topology;

/**
 * Represents a node extracted from the application topology JSON file.
 * @param id The id of the node.
 * @param name The name of the node.
 * @param topic The topic (edge node name) that the node belongs to.
 * @param type The node type.
 * @param thing The thing node it references.
 * @param uniqueAttribute The unique attribute associated with the node.
 * @param subFlowId The sub flow id the node belongs to.
 */
public record TopologyNode(String id, String name, String topic, String type, String thing, String uniqueAttribute, String subFlowId) {
    /**
     * Formats topology node into a readable string.
     *
     * @return A formatted string containing the node's details.
     */
    @Override
    public String toString() {
        return "{" +
                "id: '" + id + '\'' +
                ", name: '" + name + '\'' +
                ", topic: '" + topic + '\'' +
                ", type: '" + type + '\'' +
                ", thing: '" + thing + '\'' +
                ", uniqueAttribute: '" + uniqueAttribute + '\'' +
                "}";
    }
}
