package com.extensions.vdcreation.models;

import java.util.List;

/**
 * Represents the schema of a property, action or event payload
 */
public class Schema {
    /** Data type of the schema, such as "boolean", "number", or "object" */
    private String type;

    /** Optional reference to a definition, for reusable types */
    private String ref;

    /** List of allowable values for enum types */
    private List<String> enumValues;

    /** Whether the schema represents a read-only property */
    private boolean readOnly;

    /** Whether the schema represents a write-only property */
    private boolean writeOnly;

    
}
