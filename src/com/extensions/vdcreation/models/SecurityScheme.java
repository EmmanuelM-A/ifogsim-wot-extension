package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a security scheme for authenticating or authorizing access to the Thing.
 */
public class SecurityScheme extends BaseEntity {
    @JsonProperty("scheme")
    private String scheme;

    // Where the security information should be sent, e.g., "header", "query", etc.
    private String in;

    /////////////////////////// Getters and Setters ///////////////////////////
    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }
}
