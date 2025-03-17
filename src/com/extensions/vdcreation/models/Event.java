package com.extensions.vdcreation.models;


/**
 * Represents an event that can be emitted by the Thing.
 * This class encapsulates the data and schema associated with an event that a Thing can emit.
 */
public class Event extends BaseEntity {

    /**
     * Defines the data structure of the event payload.
     * This property holds the actual data being transmitted with the event.
     */
    private Data data;

    /**
     * Schema defining the data structure of the event payload.
     * This schema provides the structure or format for the data that accompanies the event.
     */
    private Schema schema;

    /**
     * Gets the schema defining the structure of the event payload.
     *
     * @return The schema associated with the event payload.
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * Sets the schema defining the structure of the event payload.
     *
     * @param schema The schema to be set for the event payload.
     */
    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    /**
     * Gets the data associated with the event payload.
     *
     * @return The data associated with the event.
     */
    public Data getData() {
        return data;
    }

    /**
     * Sets the data for the event payload.
     *
     * @param data The data to be set for the event.
     */
    public void setData(Data data) {
        this.data = data;
    }
}