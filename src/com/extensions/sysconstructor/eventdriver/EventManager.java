package com.extensions.sysconstructor.eventdriver;

import com.extensions.customfog.CustomFogDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {
    private static EventManager instance;
    private final Map<String, List<CustomFogDevice>> eventRoutingTable = new HashMap<>();

    private EventManager() {}

    public static synchronized EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public void registerEvent(String eventType, CustomFogDevice device) {
        eventRoutingTable.computeIfAbsent(eventType, k -> new ArrayList<>()).add(device);
    }

    public void routeEvent(EventTuple tuple) {
        List<CustomFogDevice> destinations = eventRoutingTable.get(tuple.getEventType());
        if (destinations != null) {
            for (CustomFogDevice device : destinations) {
                device.processEventTuple(tuple);
            }
        }
    }
}
