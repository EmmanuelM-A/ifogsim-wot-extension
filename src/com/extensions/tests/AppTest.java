package com.extensions.tests;

import com.extensions.utils.presets.ActuatorPreset;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.utils.presets.SensorPreset;
import com.extensions.vdcreation.core.VirtualDevice;
import com.extensions.vdcreation.core.VirtualDeviceFactory;
import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.parsers.ThingDescriptionParser;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.FogBroker;
import org.fog.entities.Tuple;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/*
    A simple IoT application which consists of one Virtual Device, a temperature sensor. This application is a basic Temperature
    Monitoring System, where a temperature sensor reads and sends data periodically to a gateway device, that acts as a
    communication hub for the sensor.
 */
public class AppTest {
    public static void main(String[] args) {
        Log.printLine("Loading Simulation....");

        try {
            Log.disable();

            // The number of cloud users
            int numUsers = 1;

            // An instance of the calendar
            Calendar calendar = Calendar.getInstance();

            // Mean trace events
            boolean trace_flag = false;

            CloudSim.init(numUsers, calendar, trace_flag);

            // Identifier of the application
            String appId = "Tester";

            FogBroker broker = new FogBroker("broker");

            // Extract metadata for the TD
            ThingDescriptionParser tdParser = new ThingDescriptionParser();

            ThingDescription tempSensorTD = tdParser.process(new File("src/com/extensions/input/things/MyTemperatureSensor.json"));

            // Create VD from TD
            VirtualDeviceFactory virtualDeviceFactory = new VirtualDeviceFactory(broker.getId(), appId, 0, FogDevicePreset.DEFAULT, SensorPreset.DEFAULT, null);

            VirtualDevice tempSensorVD = virtualDeviceFactory.createVirtualDevice(tempSensorTD, null);

            // Create Temperature Monitoring application
            Application application = createApplication(appId, broker.getId());



        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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

    private static Application createApplication(String appId, int userId) {
        // Creates an empty application model
        Application application = Application.createApplication(appId, userId);

        /*
         * Adding modules (vertices) to the application model (directed graph)
         */
        application.addAppModule("temperature_sensor", 10); // Module for the sensor
        application.addAppModule("data_processor", 10); // Module for processing the data
        application.addAppModule("alert_manager", 10); // Module for managing alerts or outputs

        /*
         * Connecting the application modules (vertices) in the application model (directed graph) with edges
         */
        application.addAppEdge("TemperatureSensor", "temperature_sensor", 500, 200, "TEMP_DATA", Tuple.UP, AppEdge.SENSOR); // Edge from sensor to module
        application.addAppEdge("temperature_sensor", "data_processor", 1000, 500, "PROCESSED_TEMP", Tuple.UP, AppEdge.MODULE); // Edge from sensor module to processing module
        application.addAppEdge("data_processor", "alert_manager", 500, 500, "TEMP_ALERT", Tuple.UP, AppEdge.MODULE); // Edge from processing to alert manager
        application.addAppEdge("alert_manager", "DISPLAY", 1000, 200, "ALERT_DISPLAY", Tuple.DOWN, AppEdge.ACTUATOR); // Edge from alert manager to display actuator

        /*
         * Defining the input-output relationships (represented by selectivity) of the application modules
         */
        application.addTupleMapping("temperature_sensor", "TEMP_DATA", "PROCESSED_TEMP", new FractionalSelectivity(1.0)); // 1.0 tuples emitted by the sensor module per incoming tuple
        application.addTupleMapping("data_processor", "PROCESSED_TEMP", "TEMP_ALERT", new FractionalSelectivity(0.8)); // 0.8 alerts emitted per processed tuple
        application.addTupleMapping("alert_manager", "TEMP_ALERT", "ALERT_DISPLAY", new FractionalSelectivity(1.0)); // 1.0 tuples emitted to the display per alert

        /*
         * Defining application loops to monitor the latency of.
         * Here, we monitor the loop: TemperatureSensor -> SensorModule -> Processor -> AlertManager -> Display
         */
        final AppLoop loop1 = new AppLoop(new ArrayList<String>() {{
            add("TemperatureSensor");
            add("temperature_sensor");
            add("data_processor");
            add("alert_manager");
            add("DISPLAY");
        }});
        List<AppLoop> loops = new ArrayList<AppLoop>() {{
            add(loop1);
        }};
        application.setLoops(loops);

        return application;
    }
}
