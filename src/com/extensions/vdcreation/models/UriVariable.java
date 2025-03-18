package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a URI variable in the context of a Thing Description (TD).
 * URI variables can be used to define dynamic parts of a URI that can take values
 * from a specified enumeration.
 */
public class UriVariable extends BaseEntity {

    @JsonProperty("enum")
    private List<String> uriVariableEnum;

    /**
     * Gets the list of possible values for the URI variable.
     *
     * @return a list of possible values for the URI variable
     */
    public List<String> getUriVariableEnum() {
        return uriVariableEnum;
    }

    /**
     * Sets the list of possible values for the URI variable.
     *
     * @param uriVariableEnum a list of values to set for the URI variable
     */
    public void setUriVariableEnum(List<String> uriVariableEnum) {
        this.uriVariableEnum = uriVariableEnum;
    }
}