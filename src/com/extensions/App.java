package com.extensions;

import com.extensions.utils.FilePaths;
import com.extensions.vdcreation.core.JsonFileProcessor;
import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.parsers.ThingDescriptionParser;

import java.io.IOException;
import java.util.List;

public final class App {
    public static void main(String[] args) {
        /*ThingDescriptionParser tdParser = new ThingDescriptionParser();

        try {
            List<ThingDescription> thingDescriptions = JsonFileProcessor.processJsonFiles(FilePaths.JSON_THINGS_REPO.getFilepath(), tdParser);

            for (ThingDescription thingDescription : thingDescriptions) {
                ThingDescription.printData(thingDescription);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        // User passed in documents
            // A folder containing TDs
            // JSON file of the nodeRED system design
            // Config files for VDs (OPTIONAL)

        // Load and create VDs from TDs

        // Load NodeRED translated system design

        // Construct system using system design and VDs
            // Apply configs to tagged element if any

        // Start simulation application

        // Stop simulation
    }
}
