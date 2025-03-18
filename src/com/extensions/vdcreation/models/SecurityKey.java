package com.extensions.vdcreation.models;

/**
 * Represents a security key used for authentication and authorization in a Thing Description.
 * This can define various types of security mechanisms such as API keys, OAuth tokens, etc.
 */
public class SecurityKey {

    /**
     * The type of security key (e.g., "APIKey", "OAuth2", "BearerToken").
     */
    private String type;

    /**
     * Retrieves the security key type.
     *
     * @return A string representing the type of security mechanism.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the security key type.
     *
     * @param type A string specifying the type of security mechanism.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns a string representation of the SecurityKey object.
     *
     * @return A string containing the security key type.
     */
    @Override
    public String toString() {
        return "SecurityKey{type='" + type + "'}";
    }
}