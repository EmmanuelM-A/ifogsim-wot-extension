package com.extensions.tests;

import com.extensions.customfog.FogDeviceFactory;
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
import org.fog.entities.FogDevice;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.utils.FogEntityFactory;

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
    /**
     * Represents all fog devices in the application including the fog devices of the virtual devices.
     */
    private static final List<FogDevice> fogDevices = new ArrayList<>();

    /**
     * Determines if the application is cloud-based
     */
    private static final boolean CLOUD = false;

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
            String appId = "Temperature-Monitor";

            FogBroker broker = new FogBroker("broker");

            // Extract metadata for the TD
            ThingDescriptionParser tdParser = new ThingDescriptionParser();

            ThingDescription tempSensorTD = tdParser.process(new File("src/com/extensions/input/things/MyTemperatureSensor.json"));

            // Create VD from TD
            VirtualDeviceFactory virtualDeviceFactory = new VirtualDeviceFactory(broker.getId(), appId, FogDevicePreset.DEFAULT, SensorPreset.DEFAULT, ActuatorPreset.DEFAULT);

            VirtualDevice temperatureSensorVD = virtualDeviceFactory.createVirtualDevice(tempSensorTD, null);

            // Create Temperature Monitoring application
            Application application = createApplication(appId, broker.getId());

            // Create the physical topology for the fog devices
            createPhysicalTopology(broker.getId(), appId, temperatureSensorVD);

            // Initialize a module mapping to map application modules to fog devices
            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();

            // Check if the deployment is cloud-based
            if (CLOUD) {
                // Assign specific application modules to the cloud
                moduleMapping.addModuleToDevice("data_processor", "cloud"); // Assign data processing to the cloud
                moduleMapping.addModuleToDevice("alert_manager", "cloud");  // Assign alert management to the cloud
            } else {
                // Edge-ward placement: Other modules will be dynamically assigned
                for (FogDevice device : fogDevices) {
                    if (device.getName().startsWith("sensor_device")) {
                        // Assign the "temperature_sensor" module to devices that represent sensors
                        moduleMapping.addModuleToDevice("temperature_sensor", device.getName());
                    }
                    if (device.getName().startsWith("edge_processor")) {
                        // Assign the "data_processor" module to devices that act as edge processors
                        moduleMapping.addModuleToDevice("data_processor", device.getName());
                    }
                    if (device.getName().startsWith("alert_device")) {
                        // Assign the "alert_manager" module to devices responsible for alerting
                        moduleMapping.addModuleToDevice("alert_manager", device.getName());
                    }
                }
            }

            // Create the controller for managing the simulation
            Controller controller = new Controller("iot-controller", fogDevices, temperatureSensorVD.get, actuators);

            // Submit the application to the controller with the appropriate placement strategy



        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void createPhysicalTopology(int userId, String appId, VirtualDevice virtualDevice) {
        // Create the cloud device at the top of the hierarchy
        FogDevice cloud = FogDeviceFactory.createFogDevice("cloud", 44800, 100, 10000, 100, 0.01);

        // Cloud has no parent, it is the root of the hierarchy
        cloud.setParentId(-1);

        // Create the proxy server device as an intermediary between cloud and gateways
        FogDevice proxy = FogDeviceFactory.createFogDevice("proxy-server", 2800, 10000, 10000, 100, 0.0);

        // Set the cloud device as the parent of the proxy server
        proxy.setParentId(cloud.getId());

        // Latency of the connection from proxy server to cloud is 100 ms
        proxy.setUplinkLatency(100);

        // Add the cloud and proxy devices to the list of fog devices
        fogDevices.add(cloud);
        fogDevices.add(proxy);

        // Create a gateway device to represent the edge node closer to IoT devices
        FogDevice gateway = FogDeviceFactory.createFogDevice("gateway", 1000, 10000, 10000, 50, 0.0);

        // Set the proxy server as the parent of the gateway
        gateway.setParentId(proxy.getId());

        // Latency of the connection from gateway to proxy server is 50 ms
        gateway.setUplinkLatency(50);

        // Add the gateway device to the list of fog devices
        fogDevices.add(gateway);

        // Configure the Virtual Device's FogDevice instance to represent the physical IoT device (Temp Sensor)
        FogDevice tempSensorDevice = virtualDevice.getFogDevice();

        // Set the gateway as the parent of the Temp Sensor device
        tempSensorDevice.setParentId(gateway.getId());

        // Latency of the connection from Temperature Sensor to Gateway is 10 ms
        tempSensorDevice.setUplinkLatency(10);

        // Add the Temp Sensor device to the list of fog devices
        fogDevices.add(tempSensorDevice);
    }

    /**
     * Creates an IoT application model for temperature monitoring and alert management.
     * This method models an application with a temperature sensor sending data to a processing module,
     * which evaluates the temperature and triggers an alert manager to send notifications
     * to an actuator/display device. The application defines modules, edges, tuple mappings,
     * and loops to simulate the application's behavior.
     *
     * @param appId  The unique identifier for the application.
     * @param userId The ID of the user deploying the application.
     * @return The constructed application model.
     */
    private static Application createApplication(String appId, int userId) {
        // Creates an empty application model with the given app ID and user ID.
        Application application = Application.createApplication(appId, userId);

        /*
         * Adding modules (vertices) to the application model (directed graph).
         * Each module represents a processing or functional unit in the application.
         */
        application.addAppModule("temperature_sensor", 10); // Module for processing raw sensor data.
        application.addAppModule("data_processor", 10);     // Module for evaluating data and making decisions.
        application.addAppModule("alert_manager", 10);      // Module for managing alerts or user notifications.

        /*
         * Connecting the application modules (vertices) in the application model (directed graph) with edges.
         * Each edge represents data flow (tuples) between modules, sensors, or actuators.
         */
        // Edge from the physical sensor to the processing module.
        application.addAppEdge("TemperatureSensor", "temperature_sensor", 500, 200, "TEMP_DATA", Tuple.UP, AppEdge.SENSOR);

        // Edge from the sensor module to the data processor module.
        application.addAppEdge("temperature_sensor", "data_processor", 1000, 500, "PROCESSED_TEMP", Tuple.UP, AppEdge.MODULE);

        // Edge from the processor to the alert manager module.
        application.addAppEdge("data_processor", "alert_manager", 500, 500, "TEMP_ALERT", Tuple.UP, AppEdge.MODULE);

        // Edge from the alert manager to the actuator/display.
        application.addAppEdge("alert_manager", "DISPLAY", 1000, 200, "ALERT_DISPLAY", Tuple.DOWN, AppEdge.ACTUATOR);

        /*
         * Defining tuple mappings for input-output relationships in each module.
         * Selectivity ratios specify how many output tuples are generated per input tuple.
         */
        application.addTupleMapping("temperature_sensor", "TEMP_DATA", "PROCESSED_TEMP",
                new FractionalSelectivity(1.0)); // 1 output tuple per input tuple in the sensor module.

        application.addTupleMapping("data_processor", "PROCESSED_TEMP", "TEMP_ALERT",
                new FractionalSelectivity(0.8)); // 0.8 alerts generated per processed tuple in the processor module.

        application.addTupleMapping("alert_manager", "TEMP_ALERT", "ALERT_DISPLAY",
                new FractionalSelectivity(1.0)); // 1 output tuple per alert in the alert manager module.

        /*
         * Defining application loops to monitor and analyze application latency.
         * Loops represent a complete flow of data from source to destination.
         */
        final AppLoop loop1 = new AppLoop(new ArrayList<String>() {{
            add("TemperatureSensor");  // Start from the physical sensor.
            add("temperature_sensor"); // Pass through the sensor processing module.
            add("data_processor");     // Continue to the data processor module.
            add("alert_manager");      // Next, send alerts via the alert manager.
            add("DISPLAY");            // End at the display actuator.
        }});
        List<AppLoop> loops = new ArrayList<AppLoop>() {{
            add(loop1); // Add the defined loop to the application.
        }};
        application.setLoops(loops); // Set the application loops for monitoring.

        return application; // Return the constructed application model.
    }

}
