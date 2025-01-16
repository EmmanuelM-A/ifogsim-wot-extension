package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class InternalProperty extends BaseEntity {
    @JsonProperty("key")
    private SecurityKey key;

    @JsonProperty("properties")
    private Map<String, InternalProperty> internalProperties;

    @JsonProperty("enum")
    private List<String> propertyEnum;

    public List<String> getPropertyEnum() {
        return propertyEnum;
    }

    public void setPropertyEnum(List<String> propertyEnum) {
        this.propertyEnum = propertyEnum;
    }

    public Map<String, InternalProperty> getInternalProperties() {
        return internalProperties;
    }

    public void setInternalProperties(Map<String, InternalProperty> internalProperties) {
        this.internalProperties = internalProperties;
    }

    public SecurityKey getKey() {
        return key;
    }

    public void setKey(SecurityKey key) {
        this.key = key;
    }
}
