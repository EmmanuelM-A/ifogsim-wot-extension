package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a security scheme for authenticating or authorizing access to the Thing.
 */
public class SecurityScheme {
    @JsonProperty("scheme")
    private String scheme;

    @JsonProperty("description")
    private String description;

    // Where the security information should be sent, e.g., "header", "query", etc.
    private String in;

    // Optional: Name of the security field (e.g., for API keys)
    @JsonProperty("name")
    private String name;

    /////////////////////////// Getters and Setters ///////////////////////////
    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
