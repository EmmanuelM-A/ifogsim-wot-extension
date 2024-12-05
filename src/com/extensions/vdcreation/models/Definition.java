package com.extension.vdcreation.models;

/**
 * Represents a data schema definition for reusable types in the Thing
 */
public class Definition {
    private String dataType;
    private String scale;

    public String getDataType() {
        return this.dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getScale() {
        return this.scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }
}
