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

    @JsonProperty("output")
    private OutputSchema outputSchema;

    private boolean idempotent;

    private boolean safe;

    public InputSchema getInputSchema() {
        return this.inputSchema;
    }

    public void setInputSchema(InputSchema inputSchema) {
        this.inputSchema = inputSchema;
    }

    public OutputSchema getOutputSchema() {
        return outputSchema;
    }

    public void setOutputSchema(OutputSchema outputSchema) {
        this.outputSchema = outputSchema;
    }

    public boolean isIdempotent() {
        return idempotent;
    }

    public void setIdempotent(boolean idempotent) {
        this.idempotent = idempotent;
    }

    public boolean isSafe() {
        return safe;
    }

    public void setSafe(boolean safe) {
        this.safe = safe;
    }
}
