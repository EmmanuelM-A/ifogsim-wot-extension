package com.extensions.tests;

import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.parsers.ThingDescriptionParser;

import java.io.File;
import java.io.IOException;

public class MainTest {
    public static void main(String[] args) throws IOException {
        // Extract metadata for the TD
        ThingDescriptionParser tdParser = new ThingDescriptionParser();

        ThingDescription td = tdParser.process(new File("src/com/extensions/tests/input/things/smartCoffeeMachine/SmartCoffeeMachine.json"));

        ThingDescription.printData(td);
    }
}
