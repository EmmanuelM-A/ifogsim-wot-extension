package com.extension.vdcreation.parsers;

import com.extension.vdcreation.models.ThingDescription;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * https://playground.thingweb.io/
 * https://github.com/w3c/wot-thing-description
 */

public class ThingDescriptionParser {

    public static void main(String[] args) {
        String jsonFilePath = "input\\things\\MyLampThing.json";

        ThingDescription td = parseData(jsonFilePath);

        printThingDescription(td);
    }

    public static ThingDescription parseData(String filepath) {
        try {
            // Create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();

            // Read JSON file and map it to Thing Description class
            //return objectMapper.readValue(new File(filepath), ThingDescription.class);
            return new ThingDescription();
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        } 
    }

    public static void printThingDescription(ThingDescription thingDescription) { 
        if (thingDescription != null) {
            System.out.println("Thing Description:");
            System.out.println("Context: " + thingDescription.getContext());
            System.out.println("ID: " + thingDescription.getId());
            System.out.println("Type: " + thingDescription.getType());
            System.out.println("Base: " + thingDescription.getBase());
            System.out.println("Title: " + thingDescription.getTitle());
            System.out.println("Description: " + thingDescription.getDescription());
            
            if (thingDescription.getVersionInfo() != null) {
                System.out.println("Version Info: " + thingDescription.getVersionInfo().toString());
            } else {
                System.out.println("Version Info: Not provided");
            }
            
            System.out.println("Definitions:");
            if (thingDescription.getDefinitions() != null && !thingDescription.getDefinitions().isEmpty()) {
                thingDescription.getDefinitions().forEach((name, definition) -> 
                    System.out.println("  " + name + ": " + definition.toString())
                );
            } else {
                System.out.println("  None");
            }
    
            System.out.println("Security Definitions:");
            if (thingDescription.getSecurityDefinitions() != null && !thingDescription.getSecurityDefinitions().isEmpty()) {
                thingDescription.getSecurityDefinitions().forEach((name, security) -> 
                    System.out.println("  " + name + ": " + security.toString())
                );
            } else {
                System.out.println("  None");
            }
    
            System.out.println("Security Requirements:");
            if (thingDescription.getSecurity() != null && !thingDescription.getSecurity().isEmpty()) {
                thingDescription.getSecurity().forEach(security -> 
                    System.out.println("  " + security)
                );
            } else {
                System.out.println("  None");
            }
    
            System.out.println("Properties:");
            if (thingDescription.getProperties() != null && !thingDescription.getProperties().isEmpty()) {
                thingDescription.getProperties().forEach((name, property) -> 
                    System.out.println("  " + name + ": " + property.toString())
                );
            } else {
                System.out.println("  None");
            }
    
            System.out.println("Actions:");
            if (thingDescription.getActions() != null && !thingDescription.getActions().isEmpty()) {
                thingDescription.getActions().forEach((name, action) -> 
                    System.out.println("  " + name + ": " + action.toString())
                );
            } else {
                System.out.println("  None");
            }
    
            System.out.println("Events:");
            if (thingDescription.getEvents() != null && !thingDescription.getEvents().isEmpty()) {
                thingDescription.getEvents().forEach((name, event) -> 
                    System.out.println("  " + name + ": " + event.toString())
                );
            } else {
                System.out.println("  None");
            }
    
            System.out.println("Links:");
            if (thingDescription.getLinks() != null && !thingDescription.getLinks().isEmpty()) {
                thingDescription.getLinks().forEach(link -> 
                    System.out.println("  " + link.toString())
                );
            } else {
                System.out.println("  None");
            }
    
        } else {
            System.out.println("Failed to parse JSON!");
        }
    }
    
}
