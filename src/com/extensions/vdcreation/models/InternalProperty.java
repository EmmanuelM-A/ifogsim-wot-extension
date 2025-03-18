package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Represents an internal property within a Thing Description.
 * This class defines a hierarchical structure for properties, allowing nested internal properties,
 * security keys, and enumerated values.
 */
public class InternalProperty extends BaseEntity {

    /**
     * The security key associated with this property.
     */
    @JsonProperty("key")
    private SecurityKey key;

    /**
     * A map of nested internal properties, enabling hierarchical property definitions.
     * The keys in this map represent property names, and the values are corresponding {@link InternalProperty} objects.
     */
    @JsonProperty("properties")
    private Map<String, InternalProperty> internalProperties;

    /**
     * A list of allowed values for this property, representing an enumeration constraint.
     */
    @JsonProperty("enum")
    private List<String> propertyEnum;

    /**
     * Gets the list of allowed values for this property.
     *
     * @return A list of strings representing the enumerated values.
     */
    public List<String> getPropertyEnum() {
        return propertyEnum;
    }

    /**
     * Sets the list of allowed values for this property.
     *
     * @param propertyEnum A list of strings representing the enumerated values.
     */
    public void setPropertyEnum(List<String> propertyEnum) {
        this.propertyEnum = propertyEnum;
    }

    /**
     * Gets the nested internal properties.
     *
     * @return A map of property names to {@link InternalProperty} objects.
     */
    public Map<String, InternalProperty> getInternalProperties() {
        return internalProperties;
    }

    /**
     * Sets the nested internal properties.
     *
     * @param internalProperties A map of property names to {@link InternalProperty} objects.
     */
    public void setInternalProperties(Map<String, InternalProperty> internalProperties) {
        this.internalProperties = internalProperties;
    }

    /**
     * Gets the security key associated with this property.
     *
     * @return The {@link SecurityKey} object.
     */
    public SecurityKey getKey() {
        return key;
    }

    /**
     * Sets the security key associated with this property.
     *
     * @param key The {@link SecurityKey} object.
     */
    public void setKey(SecurityKey key) {
        this.key = key;
    }
}