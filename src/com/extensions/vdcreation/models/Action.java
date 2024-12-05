package com.extension.vdcreation.models;

import java.util.List;

/**
 * Represents an action that can be invoked on the Thing.
 */
public class Action {
    /** 
     * The name of the action. 
     */
    private String name;

    /** 
     * The description of the action.
     */
    private String description;

    /** 
     * Schema defining the input parameters for the action. 
     */
    private InputSchema inputSchema;

    /** 
     * Interaction endpoints for invoking this action. 
     */
    private List<Form> forms;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public InputSchema getInputSchema() {
        return this.inputSchema;
    }

    public void setInputSchema(InputSchema inputSchema) {
        this.inputSchema = inputSchema;
    }

    public List<Form> getForms() {
        return this.forms;
    }

    public void setForms(List<Form> forms) {
        this.forms = forms;
    }
}
