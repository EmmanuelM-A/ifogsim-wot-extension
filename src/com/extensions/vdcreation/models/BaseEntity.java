package com.extensions.vdcreation.models;

import java.util.List;

/**
 * Represents a base entity in the Thing Description model.
 * This class contains common attributes shared across various entities,
 * such as properties, actions, and events.
 *
 * <p>The attributes it contains are as follows:</p>
 * <ul>
 *     <li><b>id</b>: A unique identifier for the entity.</li>
 *     <li><b>title</b>: A human-readable title or name for the entity.</li>
 *     <li><b>description</b>: A brief textual description of the entity.</li>
 *     <li><b>type</b>: The type or semantic category of the entity.</li>
 *     <li><b>unit</b>: The unit of measurement associated with the entity's data or values.</li>
 *     <li><b>required</b>: A list of required attributes or fields for this entity.</li>
 *     <li><b>minimum</b>: The minimum allowable value for numeric attributes of the entity.</li>
 *     <li><b>maximum</b>: The maximum allowable value for numeric attributes of the entity.</li>
 *     <li><b>observable</b>: Indicates whether the entity supports observation for real-time updates.</li>
 *     <li><b>writeable</b>: Indicates whether the entity supports writeable attributes..</li>
 *     <li><b>readOnly</b>: Indicates whether the entity is read-only.</li>
 *     <li><b>writeOnly</b>: Indicates whether the entity is write-only.</li>
 *     <li><b>security</b>: A list of security mechanisms or schemes associated with the entity.</li>
 * </ul>
 */
public class BaseEntity {
    /**
     * A unique identifier for the entity.
     */
    private String id;

    /**
     * A human-readable title or name for the entity.
     */
    private String title;

    /**
     * A brief textual description of the entity.
     */
    private String description;

    /**
     * The type or semantic category of the entity.
     */
    private String type;

    /**
     * A list of forms that define interaction details for the entity.
     */
    private List<Form> forms;

    /**
     * The unit of measurement associated with the entity's data or values.
     */
    private String unit;

    /**
     * A list of required attributes or fields for this entity.
     */
    private List<String> required;

    /**
     * The minimum allowable value for numeric attributes of the entity.
     */
    private double minimum;

    /**
     * The maximum allowable value for numeric attributes of the entity.
     */
    private double maximum;

    /**
     * Indicates whether the entity supports observation for real-time updates.
     */
    private boolean observable;

    /**
     * Indicates whether the entity supports writeable attributes.
     */
    private boolean writeable;

    /**
     * Indicates whether the entity is read-only.
     */
    private boolean readOnly;

    /**
     * Indicates whether the entity is write-only.
     */
    private boolean writeOnly;

    /**
     * A list of security mechanisms or schemes associated with the entity.
     */
    private List<String> security;

    /**
     * Gets the unique identifier for the entity.
     * @return the unique identifier
     */
    public String getId() {
        return id;
    }
    /**
     * Sets the unique identifier for the entity.
     * @param id the unique identifier to set
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * Gets the title of the entity.
     * @return the title of the entity
     */
    public String getTitle() {
        return title;
    }
    /**
     * Sets the title of the entity.
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
    /**
     * Gets the description of the entity.
     * @return the description of the entity
     */
    public String getDescription() {
        return description;
    }
    /**
     * Sets the description of the entity.
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * Gets the type of the entity.
     * @return the type of the entity
     */
    public String getType() {
        return type;
    }
    /**
     * Sets the type of the entity.
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }
    /**
     * Gets the list of forms for the entity.
     * @return the list of forms
     */
    public List<Form> getForms() {
        return forms;
    }
    /**
     * Sets the list of forms for the entity.
     * @param forms the list of forms to set
     */
    public void setForms(List<Form> forms) {
        this.forms = forms;
    }
    /**
     * Gets the unit of measurement for the entity.
     * @return the unit of measurement
     */
    public String getUnit() {
        return unit;
    }
    /**
     * Sets the unit of measurement for the entity.
     * @param unit the unit to set
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }
    /**
     * Gets the list of required attributes for the entity.
     * @return the list of required attributes
     */
    public List<String> getRequired() {
        return required;
    }
    /**
     * Sets the list of required attributes for the entity.
     * @param required the list of required attributes to set
     */
    public void setRequired(List<String> required) {
        this.required = required;
    }
    /**
     * Gets the minimum allowable value for the entity.
     * @return the minimum allowable value
     */
    public double getMinimum() {
        return minimum;
    }
    /**
     * Sets the minimum allowable value for the entity.
     * @param minimum the minimum value to set
     */
    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }
    /**
     * Gets the maximum allowable value for the entity.
     * @return the maximum allowable value
     */
    public double getMaximum() {
        return maximum;
    }
    /**
     * Sets the maximum allowable value for the entity.
     * @param maximum the maximum value to set
     */
    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }
    /**
     * Checks if the entity supports observation.
     * @return true if observable, otherwise false
     */
    public boolean isObservable() {
        return observable;
    }
    /**
     * Sets the observable property for the entity.
     * @param observable the observable property to set
     */
    public void setObservable(boolean observable) {
        this.observable = observable;
    }

    /**
     * Checks if an entity supports writeable operations.
     * @return True if possible, otherwise false.
     */
    public boolean isWriteable() {
        return writeable;
    }

    /**
     * Sets the writeable property for the entity.
     * @param writeable The writeable property to be set.
     */
    public void setIsWriteable(boolean writeable) {
        this.writeable = writeable;
    }

    /**
     * Checks if the entity is read-only.
     * @return true if read-only, otherwise false
     */
    public boolean isReadOnly() {
        return readOnly;
    }
    /**
     * Sets the read-only property for the entity.
     * @param readOnly the read-only property to set
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    /**
     * Checks if the entity is write-only.
     * @return true if write-only, otherwise false
     */
    public boolean isWriteOnly() {
        return writeOnly;
    }
    /**
     * Sets the write-only property for the entity.
     * @param writeOnly the write-only property to set
     */
    public void setWriteOnly(boolean writeOnly) {
        this.writeOnly = writeOnly;
    }
    /**
     * Gets the list of security mechanisms for the entity.
     * @return the list of security mechanisms
     */
    public List<String> getSecurity() {
        return security;
    }
    /**
     * Sets the list of security mechanisms for the entity.
     * @param security the list of security mechanisms to set
     */
    public void setSecurity(List<String> security) {
        this.security = security;
    }
}