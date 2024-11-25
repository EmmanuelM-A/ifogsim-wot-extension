package com.extension.vdcreation.core;

import java.io.IOException;
import java.util.List;

import com.extension.utils.FilePaths;
import com.extension.vdcreation.models.ThingDescription;
import com.extension.vdcreation.parsers.ThingDescriptionParser;

public class VirtualDeviceCreation {
    public static void main(String[] args) {
        ThingDescriptionParser tdParser = new ThingDescriptionParser();
        
        try {
            List<ThingDescription> thingDescriptions = JsonFileProcessor.processJsonFiles(FilePaths.JSON_THINGS_REPO.getFilepath(), tdParser);

            for (ThingDescription thingDescription : thingDescriptions) {
                ThingDescription.printData(thingDescription);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
