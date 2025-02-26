package com.extensions.sysconstructor.eventdriver;

import com.extensions.customfog.CustomFogDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {
    private static final EventManager instance = new EventManager();
    private final List<EventSensor> eventSensors = new ArrayList<>();

    private EventManager() {}

    public static synchronized EventManager getInstance() {
        return instance;
    }

    public void registerEventSensor(EventSensor sensor) {
        eventSensors.add(sensor);
    }

    public void triggerEvent(String eventType) {
        for (EventSensor sensor : eventSensors) {
            if (sensor.getTupleType().equals(eventType)) {
                sensor.triggerEvent();
                System.out.println("Event triggered: " + eventType);
            }
        }
    }

    public List<EventSensor> getEventSensors() {
        return eventSensors;
    }
}
