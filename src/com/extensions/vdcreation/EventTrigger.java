package com.extensions.vdcreation;

public class EventTrigger extends Trigger {
    private
    public EventTrigger(String name) {
        super(name);
    }

    @Override
    public boolean handleTrigger() {
        return false;
    }
}
