package com.extensions.vdcreation.models;

import java.util.Map;

/**
 * Represents an item with a set of internal properties.
 * This class provides a structure for defining properties that belong to an item,
 * allowing for hierarchical and structured data representation.
 */
public class Item extends BaseEntity {

    /**
     * A map of property names to their corresponding {@link InternalProperty} objects.
     * This defines the various attributes associated with the item.
     */
    private Map<String, InternalProperty> properties;

    /**
     * Retrieves the properties of the item.
     *
     * @return A map containing property names as keys and their corresponding {@link InternalProperty} values.
     */
    public Map<String, InternalProperty> getProperties() {
        return properties;
    }

    /**
     * Sets the properties of the item.
     *
     * @param properties A map containing property names as keys and their corresponding {@link InternalProperty} values.
     */
    public void setProperties(Map<String, InternalProperty> properties) {
        this.properties = properties;
    }
}