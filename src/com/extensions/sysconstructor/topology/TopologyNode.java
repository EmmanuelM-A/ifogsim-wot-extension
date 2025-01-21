package com.extensions.sysconstructor.topology;

public record TopologyNode(String id, String name, String topic, String type, String thing, String uniqueAttribute) {
    @Override
    public String toString() {
        return "TopologyNode{" +
                "id: '" + id + '\'' +
                ", name: '" + name + '\'' +
                ", type: '" + type + '\'' +
                ", thing: '" + thing + '\'' +
                ", attribute: " + uniqueAttribute +
                '}';
    }
}
