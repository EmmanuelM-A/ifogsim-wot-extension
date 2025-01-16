package com.extensions.sysconstructor.topology;

public class TopologyNode {
    private String id;
    private String name;
    private String type;
    private String thing;
    private String uniqueAttribute;

    // Constructor
    public TopologyNode(String id, String name, String type, String thing, String uniqueAttribute) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.thing = thing;
        this.uniqueAttribute = uniqueAttribute;
    }

    // Getters and toString for debugging purposes
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getThing() {
        return thing;
    }

    public String getUniqueAttribute() {
        return uniqueAttribute;
    }

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
