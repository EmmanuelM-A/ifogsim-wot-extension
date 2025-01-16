package com.extensions.vdcreation.models;

import java.util.Map;

public class OutputSchema extends BaseEntity {
    private Map<String, InternalProperty> properties;

    public Map<String, InternalProperty> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, InternalProperty> properties) {
        this.properties = properties;
    }
}
