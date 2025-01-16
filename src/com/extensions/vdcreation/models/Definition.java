package com.extensions.vdcreation.models;

/**
 * Represents a data schema definition for reusable types in the Thing
 */
public class Definition extends BaseEntity {
    private String scale;

    public String getScale() {
        return this.scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }
}
