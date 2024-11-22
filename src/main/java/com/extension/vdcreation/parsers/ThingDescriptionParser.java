package com.extension.vdcreation.parsers;

import java.io.File;

import com.extension.vdcreation.models.ThingDescription;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * https://playground.thingweb.io/
 * https://github.com/w3c/wot-thing-description
 */

public class ThingDescriptionParser {

    public static void main(String[] args) {
        String jsonFilePath = "src\\main\\java\\com\\extension\\in\\MyLampThing.json";

        ThingDescription td = parseData(jsonFilePath);

        printThingDescription(td);
    }

    public static ThingDescription parseData(String filepath) {
        try {
            // Create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();

            // Read JSON file and map it to Thing Description class
            return objectMapper.readValue(new File(filepath), ThingDescription.class);
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
            //System.out.println("VersionInfo: " + thingDescription.getVersionInfo());
            //System.out.println("Definitions:");
            System.out.println("Security Definitions: " + thingDescription.getSecurityDefinitions());
            System.out.println("Security: " + thingDescription.getSecurity());
            System.out.println("Properties: " + thingDescription.getProperties());
            System.out.println("Actions: " + thingDescription.getActions());
            System.out.println("Events: " + thingDescription.getEvents());
    
        } else {
            System.out.println("Failed to parse JSON!");
        }
    }
    
}
