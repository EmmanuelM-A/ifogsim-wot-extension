package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Version information for the Thing Description, indicating model updates.
 */
public class VersionInfo extends BaseEntity {
    /**
     * The current instance version, identifying changes in the Thing's interface or behaviour
     */
    @JsonProperty("instance")
    private String instance;

    public String getInstance() {
        return this.instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }
}
