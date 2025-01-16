package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Represents the input schema for an action, defining parameters.
 */
public class InputSchema extends BaseEntity {
    /**
     * A list of properties included in the input schema.
     */
    @JsonProperty("properties")
    private Map<String, InputProperty> inputProperties;

    public Map<String, InputProperty> getInputProperties() {
        return inputProperties;
    }

    public void setInputProperties(Map<String, InputProperty> inputProperties) {
        this.inputProperties = inputProperties;
    }
}
