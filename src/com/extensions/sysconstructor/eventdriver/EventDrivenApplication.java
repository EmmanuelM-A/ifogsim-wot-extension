package com.extensions.sysconstructor.eventdriver;

import com.extensions.utils.presets.ApplicationPreset;
import org.fog.application.AppModule;
import org.fog.application.Application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDrivenApplication extends Application {
    private final Map<String, NodeEvent> eventMap = new HashMap<>();
    private final ApplicationPreset appliationPreset;
    public EventDrivenApplication(String appId, int userId, ApplicationPreset applicationPreset) {
        super(appId, userId);
        this.appliationPreset = applicationPreset;
    }

    public void setEvents(List<NodeEvent> nodeEventList) {
        for (NodeEvent event : nodeEventList) {
            eventMap.put(event.eventType(), event);
        }
    }

    public NodeEvent getEvent(String eventType) {
        return eventMap.get(eventType);
    }

    public AppModule addAppModule(String moduleName) {
        super.addAppModule(moduleName, appliationPreset.APP_MODULE_RAM);
        return super.getModuleByName(moduleName);
    }
}
