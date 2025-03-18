package com.extensions.vdcreation.models;

/**
 * Represents a link to related resources or Things.
 * This class follows the Web of Things (WoT) standard for linking related entities.
 */
public class Link extends BaseEntity {
    /**
     * The relationship type between the current entity and the linked resource.
     * This follows standard link relation types, such as "self", "next", "alternate", etc.
     */
    private String rel;

    /**
     * The URL of the related resource.
     * This defines the endpoint or reference to the linked entity.
     */
    private String href;

    /**
     * The language of the linked resource, represented as a language tag (e.g., "en", "fr").
     */
    private String hreflang;

    /**
     * Retrieves the relationship type of the link.
     *
     * @return The relationship type as a string.
     */
    public String getRel() {
        return rel;
    }

    /**
     * Sets the relationship type of the link.
     *
     * @param rel The relationship type to set.
     */
    public void setRel(String rel) {
        this.rel = rel;
    }

    /**
     * Retrieves the URL of the related resource.
     *
     * @return The URL as a string.
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the URL of the related resource.
     *
     * @param href The URL to set.
     */
    public void setHref(String href) {
        this.href = href;
    }

    /**
     * Retrieves the language of the linked resource.
     *
     * @return The language tag as a string.
     */
    public String getHreflang() {
        return hreflang;
    }

    /**
     * Sets the language of the linked resource.
     *
     * @param hreflang The language tag to set.
     */
    public void setHreflang(String hreflang) {
        this.hreflang = hreflang;
    }
}