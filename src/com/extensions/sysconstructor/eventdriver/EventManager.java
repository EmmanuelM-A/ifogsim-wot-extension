package com.extensions.sysconstructor.eventdriver;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for managing event sensors and triggering events based on event types. It maintains a list
 * of registered {@link EventSensor} objects and notifies them when a relevant event occurs.
 */
public class EventManager {
    /**
     * The singleton instance of {@code EventManager}.
     */
    private static final EventManager instance = new EventManager();

    /**
     * The list of registered event sensors.
     */
    private final List<EventSensor> eventSensors = new ArrayList<>();

    /**
     * A flag to enable or disable event handling.
     */
    public static boolean EVENT_HANDLING_ENABLED = true;

    /**
     * A private constructor to enforce the singleton pattern.
     */
    private EventManager() {}

    /**
     * Returns the singleton instance of {@code EventManager}.
     *
     * @return the singleton {@code EventManager} instance
     */
    public static synchronized EventManager getInstance() {
        return instance;
    }

    /**
     * Registers an {@link EventSensor} to receive event notifications.
     *
     * @param sensor the event sensor to register
     */
    public void registerEventSensor(EventSensor sensor) {
        eventSensors.add(sensor);
    }

    /**
     * Triggers an event by notifying all registered event sensors that match the given event type.
     *
     * @param eventType the type of event to trigger
     */
    public void triggerEvent(String eventType) {
        for (EventSensor sensor : eventSensors) {
            if (sensor.getTupleType().equals(eventType)) {
                sensor.triggerEvent();
            }
        }
    }

    /**
     * Returns the list of registered event sensors.
     *
     * @return a list of registered {@link EventSensor} objects
     */
    public List<EventSensor> getEventSensors() {
        return eventSensors;
    }
}