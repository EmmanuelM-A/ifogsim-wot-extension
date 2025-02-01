package com.extensions.tests;

import com.extensions.sysconstructor.core.JsonToApplicationModel;
import com.extensions.utils.presets.*;

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

        /*JsonToApplication jsonToApplication = new JsonToApplication(CloudNodePreset.DEFAULT, EdgeNodePreset.DEFAULT);

        ApplicationPhysicalTopology applicationPhysicalTopology = jsonToApplication.createPhysicalTopology(
                new File("src/com/extensions/input/application/door-security-application.json"),
                null
        );*/

        //NodeRedTranslator.nodeRedToInputJson(new File("src/com/extensions/input/application/door-security-application.json"));
    }
}
