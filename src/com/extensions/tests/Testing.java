package com.extensions.tests;

import com.extensions.sysconstructor.nodered.NodeRedTranslator;

import java.io.File;

public class Testing {
    public static void main(String[] args) throws Exception {
        String NODE_RED_APPLICATION_JSON = "src/com/extensions/tests/examples/smartCoffeeMachineApplication/smart-coffee-machine-application.json";

        // Convert the Node-RED application description into a structured input format
        NodeRedTranslator.nodeRedToInputJson(new File(NODE_RED_APPLICATION_JSON));
    }
}
