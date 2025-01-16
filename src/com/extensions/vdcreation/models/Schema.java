package com.extensions.vdcreation.models;

import java.util.List;

/**
 * Represents the schema of a property, action or event payload
 */
public class Schema extends BaseEntity {
    /** Optional reference to a definition, for reusable types */
    private String ref;

    /** List of allowable values for enum types */
    private List<String> enumValues;
}
