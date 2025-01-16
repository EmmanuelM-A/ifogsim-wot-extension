package com.extensions.vdcreation.models;

/**
 * Represents a response for an action form in a Thing Description.
 */
public class Response extends BaseEntity {
    /**
     * The content type of the response (e.g. "image/jpeg")
     */
    private String contentType;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
