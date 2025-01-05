package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the input schema for an action, defining parameters.
 */
public class InputSchema {
    /**
     * Data type of the input schema, usually object.
     */
    private String type;

    /**
     * A list of properties included in the input schema.
     */
    @JsonProperty("properties")
    private List<InputProperty> inputProperties;

    /**
     * Determines which properties are required for this input.
     */
    private List<String> required;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<InputProperty> getInputProperties() {
        return inputProperties;
    }

    public void setInputProperties(List<InputProperty> inputProperties) {
        this.inputProperties = inputProperties;
    }

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }
}
