package com.extension.vdcreation.models;

import java.util.List;

/**
 * Represents the input schema for an action, defining parameters.
 */
public class InputSchema {
    /** Data type of the input schema, usually "object" */
    private String type;

    /** List of fields included in the input schema */
    private List<Field> fields;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
}
