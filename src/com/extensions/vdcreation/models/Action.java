package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents an action that can be invoked on the Thing.
 */
public class Action extends BaseEntity {
    /** 
     * Schema defining the input parameters for the action. 
     */
    @JsonProperty("input")
    private InputSchema inputSchema;

    public InputSchema getInputSchema() {
        return this.inputSchema;
    }

    public void setInputSchema(InputSchema inputSchema) {
        this.inputSchema = inputSchema;
    }
}
