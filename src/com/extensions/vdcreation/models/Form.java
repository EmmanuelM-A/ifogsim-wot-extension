package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a form, specifying an interaction endpoint. This class describes an endpoint for interacting with a Thing,
 * including its URL, supported operations, security requirements, and response format.
 */
public class Form extends BaseEntity {
    /**
     * URL of the endpoint.
     * This property specifies the location where the interaction with the Thing can be performed.
     */
    private String href;

    /**
     * Media type of the payload for the interaction.
     * This defines the format of the data expected in the interaction.
     */
    private String contentType;

    /**
     * Operations supported by the endpoint, such as "readProperty".
     * These operations define what actions can be performed at the endpoint.
     */
    private List<String> op;

    /**
     * Security requirements for accessing this form.
     * This lists the security mechanisms required to access the endpoint.
     */
    private List<String> security;

    /**
     * Subprotocol for the interaction.
     * This specifies the subprotocol used for communication with the endpoint.
     */
    private String subProtocol;

    /**
     * Method name for the HTTP operation (e.g., "GET", "POST").
     * This is the method used in HTTP requests to interact with the endpoint.
     */
    @JsonProperty("htv:methodName")
    private String methodName;

    /**
     * Response expected from the interaction.
     * This defines the structure of the response returned after interacting with the endpoint.
     */
    private Response response;

    /**
     * Gets the URL of the interaction endpoint.
     *
     * @return The URL of the endpoint.
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the URL of the interaction endpoint.
     *
     * @param href The URL to set for the interaction endpoint.
     */
    public void setHref(String href) {
        this.href = href;
    }

    /**
     * Gets the media type of the payload for the interaction.
     *
     * @return The content type of the payload.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the media type of the payload for the interaction.
     *
     * @param contentType The content type to set for the payload.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the operations supported by the endpoint.
     *
     * @return A list of operations supported by the endpoint.
     */
    public List<String> getOp() {
        return op;
    }

    /**
     * Sets the operations supported by the endpoint.
     *
     * @param op The list of operations to set.
     */
    public void setOp(List<String> op) {
        this.op = op;
    }

    /**
     * Gets the security requirements for accessing the form.
     *
     * @return A list of security requirements for the endpoint.
     */
    public List<String> getSecurity() {
        return security;
    }

    /**
     * Sets the security requirements for accessing the form.
     *
     * @param security The list of security requirements to set.
     */
    public void setSecurity(List<String> security) {
        this.security = security;
    }

    /**
     * Gets the sub protocol for the interaction.
     *
     * @return The sub protocol used for communication with the endpoint.
     */
    public String getSubProtocol() {
        return subProtocol;
    }

    /**
     * Sets the sub protocol for the interaction.
     *
     * @param subProtocol The sub protocol to set for the interaction.
     */
    public void setSubProtocol(String subProtocol) {
        this.subProtocol = subProtocol;
    }

    /**
     * Gets the response expected from the interaction.
     *
     * @return The response expected from the endpoint.
     */
    public Response getResponse() {
        return response;
    }

    /**
     * Sets the response expected from the interaction.
     *
     * @param response The response to set for the endpoint.
     */
    public void setResponse(Response response) {
        this.response = response;
    }

    /**
     * Gets the method name for the HTTP operation.
     *
     * @return The HTTP method (e.g., "GET", "POST").
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the method name for the HTTP operation.
     *
     * @param methodName The HTTP method to set.
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}