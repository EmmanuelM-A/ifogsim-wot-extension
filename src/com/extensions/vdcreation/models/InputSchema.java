package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

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
    private Map<String, InputProperty> inputProperties;

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

    public Map<String, InputProperty> getInputProperties() {
        return inputProperties;
    }

    public void setInputProperties(Map<String, InputProperty> inputProperties) {
        this.inputProperties = inputProperties;
    }

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }
}
