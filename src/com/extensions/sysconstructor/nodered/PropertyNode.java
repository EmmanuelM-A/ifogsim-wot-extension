package com.extensions.sysconstructor.nodered;

import java.util.List;

public class PropertyNode extends NodeRedNode {
    private final String property;
    public PropertyNode(String id, String type, String name, String thingId, List<String> connections, String property) {
        super(id, type, name, thingId, connections);
        this.property = property;
    }

    public String getProperty() {
        return property;
    }
}
