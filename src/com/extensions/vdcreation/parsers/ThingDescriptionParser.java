package com.extensions.vdcreation.parsers;

import java.io.File;

import com.extensions.utils.processors.FileProcessor;
import com.extensions.vdcreation.models.ThingDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * https://playground.thingweb.io/
 * https://github.com/w3c/wot-thing-description
 */
public class ThingDescriptionParser implements FileProcessor<ThingDescription> {
    private final ObjectMapper objectMapper;

    public ThingDescriptionParser() {
        // Create and configure object instance
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }

    @Override
    public ThingDescription process(File file) throws Exception {
        try {
            // Read JSON file and map it to Thing Description class
            return objectMapper.readValue(file, ThingDescription.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }   
}
