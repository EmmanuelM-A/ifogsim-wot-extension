package com.extensions.vdcreation.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.extensions.utils.processors.FileProcessor;
import com.extensions.vdcreation.models.ThingDescription;

public class JsonFileProcessor {
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
                        e.getMessage();
                    }
                });

        return tds;
    }
}
