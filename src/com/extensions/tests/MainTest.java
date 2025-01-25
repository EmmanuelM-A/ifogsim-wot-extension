package com.extensions.tests;

import com.extensions.sysconstructor.core.ApplicationPhysicalTopology;
import com.extensions.sysconstructor.topology.JsonToApplication;
import com.extensions.utils.FilePaths;
import com.extensions.utils.presets.*;
import com.extensions.vdcreation.core.JsonFileProcessor;
import com.extensions.vdcreation.core.VirtualDevice;
import com.extensions.vdcreation.core.VirtualDeviceFactory;
import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.parsers.ThingDescriptionParser;
import com.extensions.vdcreation.parsers.VirtualDeviceConfigParser;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

        ApplicationPhysicalTopology applicationPhysicalTopology = jsonToApplication.createPhysicalTopology(
                new File("src/com/extensions/input/application/door-security-application.json"),
                null
        );
    }
}
