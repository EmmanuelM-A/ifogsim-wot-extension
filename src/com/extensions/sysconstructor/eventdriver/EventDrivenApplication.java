package com.extensions.sysconstructor.eventdriver;

import org.fog.application.Application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDrivenApplication extends Application {
    private final Map<String, NodeEvent> eventMap = new HashMap<>();
    public EventDrivenApplication(String appId, int userId) {
        super(appId, userId);
    }

    public void setEvents(List<NodeEvent> nodeEventList) {
        for (NodeEvent event : nodeEventList) {
            eventMap.put(event.eventType(), event);
        }
    }

    public NodeEvent getEvent(String eventType) {
        return eventMap.get(eventType);
    }



}
