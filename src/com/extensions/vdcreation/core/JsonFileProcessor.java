package com.extensions.vdcreation.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.extensions.utils.processors.FileProcessor;
import com.extensions.vdcreation.models.ThingDescription;

/**
 * This class provides a method to process JSON files from a specified folder.
 * It reads Thing Description (TD) JSON files, processes them using a provided {@link FileProcessor},
 * and returns a list of processed {@link ThingDescription} objects.
 */
public class JsonFileProcessor {

    /**
     * Processes all JSON files in the given folder path using the provided file processor.
     * <p>
     * This method reads JSON files from the specified directory, applies the given processor
     * to each file, and collects the resulting {@link ThingDescription} objects into a list.
     * </p>
     *
     * @param folderPath the path to the folder containing JSON files
     * @param processor  the {@link FileProcessor} responsible for converting JSON files into
     *                   {@link ThingDescription} objects
     * @return a list of processed {@link ThingDescription} objects
     * @throws IOException if an I/O error occurs while accessing the folder
     * @throws IllegalArgumentException if the provided folder path is invalid (does not exist or is not a directory)
     */
    public static List<ThingDescription> processJsonFiles(String folderPath, FileProcessor<ThingDescription> processor) throws IOException {
        List<ThingDescription> tds = new ArrayList<>();

        Path folder = Paths.get(folderPath);

        // Check if the folder exists and is a directory
        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            throw new IllegalArgumentException("Invalid folder path: " + folderPath);
        }

        // Iterate through the files in the folder
        Files.list(folder)
                .filter(path -> path.toString().endsWith(".json")) // Filter JSON files
                .forEach(path -> {
                    try {
                        ThingDescription newThingDescription = processor.process(path.toFile()); // Parse each JSON file

                        tds.add(newThingDescription);
                    } catch (Exception e) {
                        System.err.println("Error processing file: " + path);
                        System.out.println(e.getMessage());
                    }
                });

        return tds;
    }
}
