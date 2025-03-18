package com.extensions.vdcreation.models;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a Thing Description for a WoT-compliant device.
 */
public class ThingDescription extends BaseEntity {
    /**
     * JSON-LD context
     */
    @JsonProperty("@context")
    private Object context;

    /**
     * The base URL used as the prefix for all forms in this Thing.
     */
    private String base;

    /**
     * Version information for this Thing Description.
     */
    private VersionInfo versionInfo;

    /**
     * Definitions for reusable data schemas in the Thing.
     */
    private Map<String, Definition> definitions;

    /** 
     * Security schemes available for accessing the Thing's properties, actions and events.
     */
    private Map<String, SecurityScheme> securityDefinitions;

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

    /**
     * Gets the context of the Thing Description.
     *
     * @return the context as an Object (can be a String or List)
     */
    public Object getContext() {
        return this.context;
    }

    /**
     * Sets the context of the Thing Description.
     *
     * @param context the context to set
     */
    public void setContext(Object context) {
        this.context = context;
    }

    /**
     * Gets the base URI of the Thing.
     *
     * @return the base URI as a String
     */
    public String getBase() {
        return this.base;
    }

    /**
     * Sets the base URI of the Thing.
     *
     * @param base the base URI to set
     */
    public void setBase(String base) {
        this.base = base;
    }

    /**
     * Gets the version information of the Thing Description.
     *
     * @return the version information
     */
    public VersionInfo getVersionInfo() {
        return this.versionInfo;
    }

    /**
     * Sets the version information of the Thing Description.
     *
     * @param versionInfo the version information to set
     */
    public void setVersionInfo(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    /**
     * Gets the definitions map.
     *
     * @return a map of definitions
     */
    public Map<String, Definition> getDefinitions() {
        return this.definitions;
    }

    /**
     * Sets the definitions map.
     *
     * @param definitions a map of definitions to set
     */
    public void setDefinitions(Map<String, Definition> definitions) {
        this.definitions = definitions;
    }

    /**
     * Gets the security definitions of the Thing.
     *
     * @return a map of security schemes
     */
    public Map<String, SecurityScheme> getSecurityDefinitions() {
        return this.securityDefinitions;
    }

    /**
     * Sets the security definitions of the Thing.
     *
     * @param securityDefinitions a map of security schemes to set
     */
    public void setSecurityDefinitions(Map<String, SecurityScheme> securityDefinitions) {
        this.securityDefinitions = securityDefinitions;
    }

    /**
     * Gets the properties of the Thing.
     *
     * @return a map of properties
     */
    public Map<String, Property> getProperties() {
        return this.properties;
    }

    /**
     * Sets the properties of the Thing.
     *
     * @param properties a map of properties to set
     */
    public void setProperties(Map<String, Property> properties) {
        this.properties = properties;
    }

    /**
     * Gets the actions of the Thing.
     *
     * @return a map of actions
     */
    public Map<String, Action> getActions() {
        return this.actions;
    }

    /**
     * Sets the actions of the Thing.
     *
     * @param actions a map of actions to set
     */
    public void setActions(Map<String, Action> actions) {
        this.actions = actions;
    }

    /**
     * Gets the events of the Thing.
     *
     * @return a map of events
     */
    public Map<String, Event> getEvents() {
        return this.events;
    }

    /**
     * Sets the events of the Thing.
     *
     * @param events a map of events to set
     */
    public void setEvents(Map<String, Event> events) {
        this.events = events;
    }

    /**
     * Gets the links associated with the Thing.
     *
     * @return a list of links
     */
    public List<Link> getLinks() {
        return this.links;
    }

    /**
     * Sets the links associated with the Thing.
     *
     * @param links a list of links to set
     */
    public void setLinks(List<Link> links) {
        this.links = links;
    }

    /**
     * Prints the attributes of a given map.
     *
     * @param title the title of the section
     * @param map   the map of attributes to print
     * @param <T>   the type of values in the map
     */
    public static <T> void printAttributes(String title, Map<String, T> map) {
        if (map == null || map.isEmpty()) {
            System.out.println(title + ": No data available.");
            return;
        }

        System.out.println(title + ":");
        for (Map.Entry<String, T> entry : map.entrySet()) {
            System.out.println("Name: " + entry.getKey());
            System.out.println("Details: " + entry.getValue());
        }
    }

    /**
     * Prints the names of attributes in a given map.
     *
     * @param title the title of the section
     * @param map   the map containing the attributes
     */
    public static void printAttributeNames(String title, Map<String, ?> map) {
        if (map == null || map.isEmpty()) {
            System.out.println(title + ": No data available.");
            return;
        }

        System.out.print(title + ": [");
        System.out.print(String.join(", ", map.keySet()));
        System.out.println("]");
    }

    /**
     * Prints the key details of a ThingDescription instance.
     *
     * @param thingDescription the ThingDescription object to print
     */
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
            ThingDescription.printAttributeNames("Definitions", thingDescription.getDefinitions());
            ThingDescription.printAttributeNames("Security Definitions", thingDescription.getSecurityDefinitions());
            System.out.println("Security: " + thingDescription.getSecurity());
            ThingDescription.printAttributeNames("Properties", thingDescription.getProperties());
            ThingDescription.printAttributeNames("Actions", thingDescription.getActions());
            ThingDescription.printAttributeNames("Events", thingDescription.getEvents());
            System.out.println("Links: " + thingDescription.getLinks());
            System.out.println("-----------------------------------------------------------------");
        } else {
            System.out.println("Failed to parse JSON!");
        }
    }
}