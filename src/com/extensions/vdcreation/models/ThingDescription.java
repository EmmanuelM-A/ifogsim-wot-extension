package com.extension.vdcreation.models;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a Thing Description for a WoT-compliant device.
 */
public class ThingDescription {
    /**
     * JSON-LD context
     */
    @JsonProperty("@context")
    private String context;

    /**
     * The unique identifier for the Thing.
     */
    private String id;

    /**
     * Semantic type of the Thing.
     */
    @JsonProperty("@type")
    private String type;

    /**
     * The base URL used as the prefix for all forms in this Thing.
     */
    private String base;

    /**
     * Title of the Thing.
     */
    private String title;

    /**
     * Description of the Thing, providing addtional information.
     */
    private String description;

    /**
     * Version information for this Thing Description.
     */
    private VersionInfo versionInfo;

    /**
     * Definitions for resuable data schemas in the Thing.
     */
    private Map<String, Definition> definitions;

    /** 
     * Security schemes available for accessing the Thing's properties, actions and events.
     */
    private Map<String, SecurityScheme> securityDefinitions;

    /**
     * List of security requirements to interact with this Thing.
     */
    private List<String> security;

    /**
     * Properties available on this Thing paired with the name of the property.
     */
    private Map<String, Property> properties;

    /**
     * Actions available on this Thing paired with the name of the action.
     */
    private Map<String, Action> actions;

    /**
     * Events emitted by this paired with the name of the event.
     */
    private Map<String, Event> events;

    /**
     * Links to related resources of Things.
     */
    private List<Link> links;

    /////////////////////////// Getters and Setters ///////////////////////////

    public String getContext() {
        return this.context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBase() {
        return this.base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public VersionInfo getVersionInfo() {
        return this.versionInfo;
    }

    public void setVersionInfo(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    public Map<String, Definition> getDefinitions() {
        return this.definitions;
    }

    public void setDefinitions(Map<String, Definition> definitions) {
        this.definitions = definitions;
    }

    public Map<String, SecurityScheme> getSecurityDefinitions() {
        return this.securityDefinitions;
    }

    public void setSecurityDefinitions(Map<String, SecurityScheme> securityDefinitions) {
        this.securityDefinitions = securityDefinitions;
    }

    public List<String> getSecurity() {
        return this.security;
    }

    public void setSecurity(List<String> security) {
        this.security = security;
    }

    public Map<String, Property> getProperties() {
        return this.properties;
    }

    public void setProperty(Map<String, Property> properties) {
        this.properties = properties;
    }

    public Map<String, Action> getActions() {
        return this.actions;
    }

    public void setActions(Map<String, Action> actions) {
        this.actions = actions;
    }
    
    public Map<String, Event> getEvents() {
        return this.events;
    }

    public void setEvents(Map<String, Event> events) {
        this.events = events;
    }

    public List<Link> getLinks() {
        return this.links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public static <T> void printAttributes(String title, Map<String, T> map) {
        if (map == null || map.isEmpty()) {
            System.out.println(title + ": No data available.");
            return;
        }

        System.out.println(title + ":");
        for (Map.Entry<String, T> entry : map.entrySet()) {
            String name = entry.getKey();
            T value = entry.getValue();
            System.out.println("Name: " + name);
            System.out.println("Details: " + value);
        }
    }

    public static void printData(ThingDescription thingDescription) { 
        if (thingDescription != null) {
            System.out.println("-----------------------------------------------------------------");
            System.out.println("Thing Description: " + thingDescription.getTitle());
            System.out.println("Context: " + thingDescription.getContext());
            System.out.println("Id: " + thingDescription.getId());
            System.out.println("Type: " + thingDescription.getType());
            System.out.println("Base: " + thingDescription.getBase());
            System.out.println("Description: " + thingDescription.getDescription());
            System.out.println("VersionInfo: " + thingDescription.getVersionInfo());
            ThingDescription.printAttributes("Definitions", thingDescription.getDefinitions());
            ThingDescription.printAttributes("Security Definitions", thingDescription.getSecurityDefinitions());
            System.out.println("Security: " + thingDescription.getSecurity());
            ThingDescription.printAttributes("Properties", thingDescription.getProperties());
            ThingDescription.printAttributes("Actions", thingDescription.getActions());
            ThingDescription.printAttributes("Events", thingDescription.getEvents());
            System.out.println("Links: " + thingDescription.getLinks());
            System.out.println("-----------------------------------------------------------------");
        } else {
            System.out.println("Failed to parse JSON!");
        }
    }
}
