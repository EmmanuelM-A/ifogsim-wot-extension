package com.extensions.vdcreation.models;

import java.util.Map;

/**
 * Represents data associated with a Thing.
 * This class contains a map of properties where each property is associated with a unique key.
 */
public class Data extends BaseEntity {
    /**
     * A map containing the internal properties of the data.
     * The key is a string identifier for the property, and the value is an instance of the InternalProperty class.
     */
    private Map<String, InternalProperty> properties;

    /**
     * Gets the map of properties associated with the data.
     *
     * @return A map where the key is the property identifier and the value is the InternalProperty.
     */
    public Map<String, InternalProperty> getProperties() {
        return properties;
    }

    /**
     * Sets the map of properties for the data.
     *
     * @param properties A map of properties where the key is the property identifier and the value is an InternalProperty.
     */
    public void setProperties(Map<String, InternalProperty> properties) {
        this.properties = properties;
    }
}