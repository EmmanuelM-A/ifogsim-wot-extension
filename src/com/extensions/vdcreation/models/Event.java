package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents an event that can be emitted by the Thing.
 */
public class Event {
    /**
     * The description of the event.
     */
    private String description;

    /**
     * Defines the data structure of the event payload.
     */
    private Data data;

    /**
     * Schema defining the data structure of the event payload.
     */
    private Schema schema;

    /**
     * Interaction endpoints for subscribing to this event.
     */
    private List<Form> forms;

    public String getDescription() {
        return description;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
