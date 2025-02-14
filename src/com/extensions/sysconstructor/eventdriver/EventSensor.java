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
    public EventSensor(String name, int userId, String appId, SensorPreset preset) {
        super(name, userId, appId, preset);
        this.preset = preset;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
