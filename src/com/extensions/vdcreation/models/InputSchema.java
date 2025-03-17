package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Represents the input schema for an action, defining parameters.
 * This class describes the structure of the input data for an action, specifying the properties that are part of the input schema.
 */
public class InputSchema extends BaseEntity {

    /**
     * A list of properties included in the input schema.
     * This map defines the individual properties (as InputProperty) that are part of the input schema for the action.
     */
    @JsonProperty("properties")
    private Map<String, InputProperty> inputProperties;

    /**
     * Gets the properties included in the input schema.
     *
     * @return A map of property names to InputProperty objects representing the input properties.
     */
    public Map<String, InputProperty> getInputProperties() {
        return inputProperties;
    }

    /**
     * Sets the properties included in the input schema.
     *
     * @param inputProperties A map of property names to InputProperty objects to set as the input properties.
     */
    public void setInputProperties(Map<String, InputProperty> inputProperties) {
        this.inputProperties = inputProperties;
    }
}