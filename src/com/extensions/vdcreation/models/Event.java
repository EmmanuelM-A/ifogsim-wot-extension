package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents an event that can be emitted by the Thing.
 */
public class Event extends BaseEntity {
    /**
     * Defines the data structure of the event payload.
     */
    private Data data;

    /**
     * Schema defining the data structure of the event payload.
     */
    private Schema schema;

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
