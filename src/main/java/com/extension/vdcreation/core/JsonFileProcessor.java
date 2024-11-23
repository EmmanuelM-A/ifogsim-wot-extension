package com.extension.vdcreation.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JsonFileProcessor {
    public static void processJsonFiles(String folderPath, FileProcessor processor) throws IOException {
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
                        processor.process(path.toFile()); // Process each JSON file
                    } catch (Exception e) {
                        System.err.println("Error processing file: " + path);
                        e.getMessage();
                    }
                });
    }
}
