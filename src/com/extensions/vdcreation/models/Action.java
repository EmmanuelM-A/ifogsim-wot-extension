package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an action that can be invoked on the Thing. This class defines the schema for the input and output of the
 * action, as well as its properties like idempotency and safety.
 */
public class Action extends BaseEntity {

    /**
     * Schema defining the input parameters for the action.
     */
    @JsonProperty("input")
    private InputSchema inputSchema;

    /**
     * Schema defining the output parameters for the action.
     */
    @JsonProperty("output")
    private OutputSchema outputSchema;

    /**
     * Indicates whether the action is idempotent (i.e., multiple invocations have the same effect as a single invocation).
     */
    private boolean idempotent;

    /**
     * Indicates whether the action is safe to invoke multiple times without causing unintended side effects.
     */
    private boolean safe;

    /**
     * Gets the input schema for the action.
     *
     * @return The input schema defining the parameters required for the action.
     */
    public InputSchema getInputSchema() {
        return this.inputSchema;
    }

    /**
     * Sets the input schema for the action.
     *
     * @param inputSchema The input schema defining the parameters required for the action.
     */
    public void setInputSchema(InputSchema inputSchema) {
        this.inputSchema = inputSchema;
    }

    /**
     * Gets the output schema for the action.
     *
     * @return The output schema defining the parameters returned by the action.
     */
    public OutputSchema getOutputSchema() {
        return outputSchema;
    }

    /**
     * Sets the output schema for the action.
     *
     * @param outputSchema The output schema defining the parameters returned by the action.
     */
    public void setOutputSchema(OutputSchema outputSchema) {
        this.outputSchema = outputSchema;
    }

    /**
     * Checks if the action is idempotent.
     *
     * @return true if the action is idempotent, false otherwise.
     */
    public boolean isIdempotent() {
        return idempotent;
    }

    /**
     * Sets whether the action is idempotent.
     *
     * @param idempotent true if the action should be idempotent, false otherwise.
     */
    public void setIdempotent(boolean idempotent) {
        this.idempotent = idempotent;
    }

    /**
     * Checks if the action is safe to invoke multiple times without causing unintended side effects.
     *
     * @return true if the action is safe, false otherwise.
     */
    public boolean isSafe() {
        return safe;
    }

    /**
     * Sets whether the action is safe to invoke multiple times.
     *
     * @param safe true if the action is safe, false otherwise.
     */
    public void setSafe(boolean safe) {
        this.safe = safe;
    }
}