package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the version information for a Thing Description (TD), indicating
 * updates or changes to the model.
 */
public class VersionInfo extends BaseEntity {

    /**
     * The current instance version of the Thing, used to identify changes
     * in the Thing's interface or behavior.
     */
    @JsonProperty("instance")
    private String instance;

    /**
     * Gets the current instance version of the Thing.
     *
     * @return the instance version
     */
    public String getInstance() {
        return this.instance;
    }

    /**
     * Sets the instance version of the Thing.
     *
     * @param instance the instance version to set
     */
    public void setInstance(String instance) {
        this.instance = instance;
    }
}