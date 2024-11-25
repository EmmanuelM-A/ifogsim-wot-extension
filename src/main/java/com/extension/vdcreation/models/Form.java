package com.extension.vdcreation.models;

import java.util.List;

/**
 * Represents a form, specifying an interaction endpoint.
 */
public class Form {
    /** URL of the endpoint */
    private String href;

    /** Media type of the payload for the interaction */
    private String contentType;

    /** Operations supported by the endpoint, like "readProperty" */
    private List<String> op;

    /** Security requirements for accessing this form */
    private List<String> security;

    private String subprotocol;

    private Response response;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public List<String> getOp() {
        return op;
    }

    public void setOp(List<String> op) {
        this.op = op;
    }

    public List<String> getSecurity() {
        return security;
    }

    public void setSecurity(List<String> security) {
        this.security = security;
    }

    public String getSubprotocol() {
        return subprotocol;
    }

    public void setSubprotocol(String subprotocol) {
        this.subprotocol = subprotocol;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}
