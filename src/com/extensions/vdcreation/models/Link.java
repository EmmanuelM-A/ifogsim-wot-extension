package com.extensions.vdcreation.models;

import java.util.List;

/**
 * Represents a link to related resources or Things.
 */
public class Link extends BaseEntity {
    private String rel;
    /**
     * URL of the related resource
     */
    private String href;

    private String hreflang;

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getHreflang() {
        return hreflang;
    }

    public void setHreflang(String hreflang) {
        this.hreflang = hreflang;
    }
}
