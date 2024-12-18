package com.extensions;

import com.extensions.utils.FilePaths;
import com.extensions.vdcreation.core.JsonFileProcessor;
import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.parsers.ThingDescriptionParser;

import java.io.IOException;
import java.util.List;

public final class App {
    private App() {
    }

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
