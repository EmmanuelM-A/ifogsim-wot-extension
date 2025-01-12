package com.extensions.tests;

import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.parsers.ThingDescriptionParser;

import java.io.File;
import java.io.IOException;

public class MainTest {
    public static void main(String[] args) throws IOException {
        // Extract metadata for the TD
        ThingDescriptionParser tdParser = new ThingDescriptionParser();

        ThingDescription tempSensorTD = tdParser.process(new File("src/com/extensions/tests/input/things/TemperatureMonitorApplication/TemperatureSensor.json"));

        ThingDescription.printData(tempSensorTD);
    }
}
