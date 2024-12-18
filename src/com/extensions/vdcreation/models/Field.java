package com.extensions.vdcreation.models;

/**
 * Represents a field in an input schema or an event schema.
 */
public class Field {
    /** Name of the field */
    private String name;

    /** Schema defining the type and constraints of the field */
    private Schema schema;

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
