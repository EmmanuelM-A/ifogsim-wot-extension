package com.extensions.vdcreation.models;

import java.util.List;

/**
 * Represents a link to related resources or Things.
 */
public class Link {
    /**
     * URL of the related resource
     */
    private String href;

    /**
     * Defines which parent Things control this Thing
     */
    private List<String> parentThings;

    /**
     * Defines which Things are controlled by this Thing
     */
    private List<String> childThings;

    /**
     * Media type of the linked resource.
     */
    private String mediaType;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public List<String> getParentThings() {
        return parentThings;
    }

    public void setParentThings(List<String> parentThings) {
        this.parentThings = parentThings;
    }

    public List<String> getChildThings() {
        return childThings;
    }

    public void setChildThings(List<String> childThings) {
        this.childThings = childThings;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
}
