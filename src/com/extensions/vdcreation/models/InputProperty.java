package com.extensions.vdcreation.models;

import java.util.List;

/**
 * Represents a field/property for an input schema or an event schema.
 */
public class InputProperty {
    private String type;

    private String description;

    private List<String> required;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
