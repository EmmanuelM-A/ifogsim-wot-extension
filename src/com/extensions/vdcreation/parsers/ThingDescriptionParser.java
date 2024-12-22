package com.extensions.vdcreation.parsers;

import java.io.File;
import java.io.IOException;

import com.extensions.utils.processors.FileProcessor;
import com.extensions.vdcreation.models.ThingDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ThingDescriptionParser implements FileProcessor<ThingDescription> {
    private final ObjectMapper objectMapper;

    public ThingDescriptionParser() {
        // Create and configure object instance
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }

    @Override
    public ThingDescription process(File file) throws IOException {
        // Read JSON file and map it to Thing Description class
        return objectMapper.readValue(file, ThingDescription.class);
    }   
}
