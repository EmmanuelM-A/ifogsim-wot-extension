package com.extensions.vdcreation.models;

import java.util.Map;

/**
 * Represents the output schema for an action, defining the structure of the response data.
 */
public class OutputSchema extends BaseEntity {
    /**
     * A map representing the properties of the output schema.
     * Each property is defined by a key-value pair, where the key is the property name
     * and the value is an {@link InternalProperty} describing its characteristics.
     */
    private Map<String, InternalProperty> properties;

    /**
     * Retrieves the properties of the output schema.
     *
     * @return A map containing the output properties.
     */
    public Map<String, InternalProperty> getProperties() {
        return properties;
    }

    /**
     * Sets the properties of the output schema.
     *
     * @param properties A map containing the output properties.
     */
    public void setProperties(Map<String, InternalProperty> properties) {
        this.properties = properties;
    }
}