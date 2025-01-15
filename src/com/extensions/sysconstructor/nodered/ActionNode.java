package com.extensions.sysconstructor.nodered;

import java.util.List;

public class ActionNode extends NodeRedNode {
    private final String action;
    public ActionNode(String id, String type, String name, String thingId, List<String> connections, String action) {
        super(id, type, name, thingId, connections);
        this.action = action;
    }

    @Override
    public String getAction() {
        return action;
    }
}
