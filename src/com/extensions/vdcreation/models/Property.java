package com.extensions.vdcreation.models;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a property of the Thing that can be read or written.
 * A property defines a state of the Thing and can have metadata, constraints,
 * and optional variables for parameterization.
 */
public class Property extends BaseEntity {

    /**
     * A list of possible enumerated values for this property.
     */
    @JsonProperty("enum")
    private List<String> propertyEnum;

    /**
     * A map of additional internal properties associated with this property.
     * Each key represents the property name, and the value contains metadata.
     */
    private Map<String, InternalProperty> properties;

    /**
     * A map defining URI variables used for parameterized interactions with this property.
     * Each key represents the variable name, and the value defines its structure.
     */
    private Map<String, UriVariable> uriVariables;

    /**
     * An item that describes the structure of array-type properties.
     */
    private Item item;

    /////////////////////////// Getters and Setters ///////////////////////////

    /**
     * Retrieves the item associated with this property.
     *
     * @return The {@link Item} representing the structure of array-type properties.
     */
    public Item getItem() {
        return item;
    }

    /**
     * Sets the item associated with this property.
     *
     * @param item The {@link Item} representing the structure of array-type properties.
     */
    public void setItems(Item item) {
        this.item = item;
    }

    /**
     * Retrieves the list of possible enumerated values for this property.
     *
     * @return A list of enumerated values.
     */
    public List<String> getPropertyEnum() {
        return propertyEnum;
    }

    /**
     * Sets the list of possible enumerated values for this property.
     *
     * @param propertyEnum A list of enumerated values.
     */
    public void setPropertyEnum(List<String> propertyEnum) {
        this.propertyEnum = propertyEnum;
    }

    /**
     * Retrieves the map of internal properties associated with this property.
     *
     * @return A map of internal properties.
     */
    public Map<String, InternalProperty> getProperties() {
        return properties;
    }

    /**
     * Sets the map of internal properties associated with this property.
     *
     * @param properties A map of internal properties.
     */
    public void setProperties(Map<String, InternalProperty> properties) {
        this.properties = properties;
    }

    /**
     * Retrieves the map of URI variables for parameterized interactions.
     *
     * @return A map of URI variables.
     */
    public Map<String, UriVariable> getUriVariables() {
        return uriVariables;
    }

    /**
     * Sets the map of URI variables for parameterized interactions.
     *
     * @param uriVariables A map of URI variables.
     */
    public void setUriVariables(Map<String, UriVariable> uriVariables) {
        this.uriVariables = uriVariables;
    }
}