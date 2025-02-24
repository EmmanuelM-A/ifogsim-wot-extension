package com.extensions.sysconstructor.eventdriver;

import com.extensions.customfog.CustomSensor;
import com.extensions.utils.presets.SensorPreset;
import com.extensions.vdcreation.models.Event;

/**
 *
 */
public class EventSensor extends CustomSensor {
    private final SensorPreset preset;
    private Event event;
    private boolean eventTriggered = false;

    public EventSensor(String name, int userId, String appId, SensorPreset preset) {
        super(name, userId, appId, preset);
        this.preset = preset;
    }

    /**
     * Triggers the event, allowing data transmission.
     */
    public void triggerEvent() {
        this.eventTriggered = true;
    }

    @Override
    public void transmit() {
        if (eventTriggered) {
            super.transmit();
            eventTriggered = false; // Reset event trigger after transmission
            //System.out.println("Event " + getName() + " triggered!");
        }
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
