package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a security scheme for authenticating or authorizing access to a Thing.
 * This defines how security credentials should be provided when interacting with the Thing.
 */
public class SecurityScheme extends BaseEntity {

    /**
     * The type of security scheme (e.g., "basic", "digest", "bearer", "apikey", "oauth2").
     */
    @JsonProperty("scheme")
    private String scheme;

    /**
     * Where the security credentials should be sent (e.g., "header", "query", "cookie").
     */
    private String in;

    /////////////////////////// Getters and Setters ///////////////////////////

    public String getScheme() {
        return scheme;
    }

    /**
     * Sets the security scheme type.
     * Common values: "basic", "digest", "bearer", "apikey", "oauth2".
     *
     * @param scheme The type of security scheme.
     */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getIn() {
        return in;
    }

    /**
     * Specifies where the security credentials should be sent.
     * Typical values: "header", "query", "cookie".
     *
     * @param in The location of the security credentials.
     */
    public void setIn(String in) {
        this.in = in;
    }

    @Override
    public String toString() {
        return "SecurityScheme{" +
                "scheme='" + scheme + '\'' +
                ", in='" + in + '\'' +
                '}';
    }
}