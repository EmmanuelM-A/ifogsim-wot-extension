package com.extensions.vdcreation.parsers;

import java.io.File;
import java.io.IOException;

import com.extensions.utils.processors.FileProcessor;
import com.extensions.vdcreation.models.ThingDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class is responsible for parsing Thing Descriptions (TDs) from JSON files.
 * It implements the {@code FileProcessor<ThingDescription>} interface to provide
 * a method for processing files and converting them into ThingDescription objects.
 */
public class ThingDescriptionParser implements FileProcessor<ThingDescription> {
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new {@code ThingDescriptionParser} instance.
     * Configures the Jackson {@code ObjectMapper} to allow single-value arrays
     * and to ignore unknown properties during deserialization.
     */
    public ThingDescriptionParser() {
        this.objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Processes a JSON file containing a Thing Description and converts it into
     * a {@code ThingDescription} object.
     *
     * @param file The JSON file containing the Thing Description.
     * @return A {@code ThingDescription} object parsed from the JSON file.
     * @throws IOException If an error occurs while reading or parsing the file.
     * @throws IllegalArgumentException If the provided file is null or does not exist.
     */
    @Override
    public ThingDescription process(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File is null or does not exist: " + file);
        }
        return objectMapper.readValue(file, ThingDescription.class);
    }
}

