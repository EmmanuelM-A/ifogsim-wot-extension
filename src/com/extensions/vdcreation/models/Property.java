package com.extensions.vdcreation.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a property of the Thing which can be read or written.
 */
public class Property {
    /**
     * Determines the property type like number, string, boolean, etc
     */
    private String type;

    /**
     *
     */
    private String description;

    /**
     * Determines if the property is observable/readable
     */
    private boolean observable;

    private boolean writeable;

    private boolean readOnly;

    private boolean writeOnly;

    @JsonProperty("enum")
    private List<String> propertyEnum;

    private List<Form> forms;

    /////////////////////////// Getters and Setters ///////////////////////////
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Form> getForms() {
        return forms;
    }

    public void setForms(List<Form> forms) {
        this.forms = forms;
    }

    public boolean isObservable() {
        return observable;
    }

    public void setObservable(boolean observable) {
        this.observable = observable;
    }

    public boolean isWriteable() {
        return writeable;
    }

    public void setIsWriteable(boolean writeable) {
        this.writeable = writeable;
    }

    public boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean getWriteOnly() {
        return writeOnly;
    }

    public void setWriteOnly(boolean writeOnly) {
        this.writeOnly = writeOnly;
    }

    public List<String> getPropertyEnum() {
        return propertyEnum;
    }

    public void setPropertyEnum(List<String> propertyEnum) {
        this.propertyEnum = propertyEnum;
    }
}
