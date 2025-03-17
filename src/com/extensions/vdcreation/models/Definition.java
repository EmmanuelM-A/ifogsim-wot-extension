package com.extensions.vdcreation.models;

/**
 * Represents a data schema definition for reusable types in the Thing.
 * This class is used to define a reusable schema that can be applied across different instances of data within a Thing.
 */
public class Definition extends BaseEntity {

    /**
     * The scale associated with the schema definition.
     * This field represents a scaling factor or unit that may be applied to the data type.
     */
    private String scale;

    /**
     * Gets the scale associated with the schema definition.
     *
     * @return The scale of the schema, typically representing a unit or scaling factor.
     */
    public String getScale() {
        return this.scale;
    }

    /**
     * Sets the scale for the schema definition.
     *
     * @param scale The scale to be set, typically representing a unit or scaling factor.
     */
    public void setScale(String scale) {
        this.scale = scale;
    }
}