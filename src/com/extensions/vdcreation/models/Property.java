package com.extensions.vdcreation.models;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a property of the Thing which can be read or written.
 */
public class Property extends BaseEntity {
    @JsonProperty("enum")
    private List<String> propertyEnum;

    private Map<String, InternalProperty> properties;

    private Map<String, UriVariable> uriVariables;

    private Item item;

    /////////////////////////// Getters and Setters ///////////////////////////

    public Item getItem() {
        return item;
    }

    public void setItems(Item item) {
        this.item = item;
    }

    public List<String> getPropertyEnum() {
        return propertyEnum;
    }

    public void setPropertyEnum(List<String> propertyEnum) {
        this.propertyEnum = propertyEnum;
    }

    public Map<String, InternalProperty> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, InternalProperty> properties) {
        this.properties = properties;
    }

    public Map<String, UriVariable> getUriVariables() {
        return uriVariables;
    }

    public void setUriVariables(Map<String, UriVariable> uriVariables) {
        this.uriVariables = uriVariables;
    }
}
