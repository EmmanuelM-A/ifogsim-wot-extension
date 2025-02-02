package com.extensions.sysconstructor.topology;

public record TopologyNode(String id, String name, String topic, String type, String thing, String uniqueAttribute, String subFlowId) {
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
