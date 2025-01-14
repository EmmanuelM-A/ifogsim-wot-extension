package com.extensions.sysconstructor.nodered;

import java.util.List;

public class EventNode extends NodeRedNode {
    private final String event;
    public EventNode(String id, String type, String name, String thingId, List<String> connections, String event) {
        super(id, type, name, thingId, connections);
        this.event = event;
    }

    public String getEvent() {
        return event;
    }
}
