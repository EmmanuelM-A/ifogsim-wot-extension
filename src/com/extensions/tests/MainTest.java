package com.extensions.tests;

import com.extensions.sysconstructor.topology.JsonToApplication;
import com.extensions.utils.presets.CloudNodePreset;
import com.extensions.utils.presets.EdgeNodePreset;
import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.parsers.ThingDescriptionParser;

import java.io.File;
import java.io.IOException;

public class MainTest {
    public static void main(String[] args) throws IOException {
        // Extract metadata for the TD
        /*ThingDescriptionParser tdParser = new ThingDescriptionParser();

        String tempSensor = "src/com/extensions/tests/input/things/TemperatureMonitorApplication/TemperatureSensor.json";
        String smartCoffeeMachine = "src/com/extensions/tests/input/things/smartCoffeeMachine/SmartCoffeeMachine.json";
        String smartLock = "src/com/extensions/input/things/smart-door-lock-thing.json";

        ThingDescription td = tdParser.process(new File(smartLock));

        ThingDescription.printData(td);*/

        JsonToApplication jsonToApplication = new JsonToApplication(CloudNodePreset.DEFAULT, EdgeNodePreset.DEFAULT);

        jsonToApplication.createPhysicalTopology(
                0,
                null,
                new File("src/com/extensions/input/application/door-security-application.json"),
                null
        );
    }
}
