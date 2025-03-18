package com.extensions.vdcreation.models;

/**
 * Represents a response for an action form in a Thing Description.
 * This class defines the structure of a response, including its content type.
 */
public class Response extends BaseEntity {

    /**
     * The content type of the response, specifying the media type of the data returned.
     * Example values: "application/json", "image/jpeg", "text/plain".
     */
    private String contentType;

    /**
     * Retrieves the content type of the response.
     *
     * @return A string representing the content type (e.g., "image/jpeg").
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type of the response.
     *
     * @param contentType A string specifying the media type of the response.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}