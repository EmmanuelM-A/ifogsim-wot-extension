package com.extension.vdcreation.parsers;

import java.io.File;

import com.extension.vdcreation.models.ThingDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
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

            objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

            // Read JSON file and map it to Thing Description class
            return objectMapper.readValue(new File(filepath), ThingDescription.class);
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        } 
    }

    public static void printThingDescription(ThingDescription thingDescription) { 
        if (thingDescription != null) {
            System.out.println("-----------------------------------------------------------------");
            System.out.println("Thing Description:");
            System.out.println("Context: " + thingDescription.getContext());
            System.out.println("Id: " + thingDescription.getId());
            System.out.println("Type: " + thingDescription.getType());
            System.out.println("Base: " + thingDescription.getBase());
            System.out.println("Title: " + thingDescription.getTitle());
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
