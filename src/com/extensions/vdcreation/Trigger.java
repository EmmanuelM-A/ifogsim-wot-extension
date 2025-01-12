package com.extensions.vdcreation;

public abstract class Trigger {
    private final String name;

    public Trigger(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract boolean handleTrigger();
}
