package com.extensions.vdcreation.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.extensions.utils.FilePaths;
import com.extensions.vdcreation.models.Property;
import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.parsers.ThingDescriptionParser;
import org.fog.entities.Sensor;


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

    public static VirtualDevice createVirtualDevice(ThingDescription thingDescription) {
        // Create an empty virtual device
        VirtualDevice virtualDevice = new VirtualDevice();

        // Create the sensors for the TD properties
        for(Map.Entry<String, Property> propertyEntry : thingDescription.getProperties().entrySet()) {
            String propertyName = propertyEntry.getKey();
            Property property = propertyEntry.getValue();

            // Map the property to a Sensor
            //Sensor sensor = new Sensor()
        }

        // Create actuators for the TD actions

        // Create event triggers for the events (Create a Trigger class and an EventTrigger class)


        return null;
    }
}
