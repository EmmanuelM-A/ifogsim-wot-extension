package com.extensions.tests;

import com.extensions.customfog.CustomController;
import com.extensions.customfog.FogDeviceFactory;
import com.extensions.custommetrics.CustomMetricManager;
import com.extensions.custommetrics.metrics.LongestApplicationLoopDelay;
import com.extensions.custommetrics.metrics.PeakEnergyConsumptionDevice;
import com.extensions.custommetrics.metrics.TotalEnergyConsumptionEfficiency;
import com.extensions.utils.presets.ActuatorPreset;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.utils.presets.SensorPreset;
import com.extensions.vdcreation.core.VirtualDevice;
import com.extensions.vdcreation.core.VirtualDeviceFactory;
import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.parsers.ThingDescriptionParser;
import com.extensions.vdcreation.parsers.VirtualDeviceConfigParser;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.utils.TimeKeeper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 A simple IoT application which consists of one Virtual Device, a temperature sensor. This application is a basic Temperature
 Monitoring System, where a temperature sensor reads and sends data periodically to a gateway device, that acts as a
 communication hub for the sensor.
 */
public class TemperatureMonitoring {
    /**
     * Represents all fog devices in the application including the fog devices of the virtual devices.
     */
    private static final List<FogDevice> fogDevices = new ArrayList<>();

    /**
     * Determines if the application is cloud-based
     */
    private static final boolean CLOUD = false;

    public static void main(String[] args) {
        Log.printLine("Starting Temperature Monitor....");

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

            ThingDescription tempSensorTD = tdParser.process(new File("src/com/extensions/tests/input/things/TemperatureMonitorApplication/MyTemperatureSensor.json"));

            //ThingDescription.printData(tempSensorTD);

            // Create VD from TD
            VirtualDeviceFactory virtualDeviceFactory = new VirtualDeviceFactory(broker.getId(), appId, FogDevicePreset.DEFAULT, SensorPreset.DEFAULT, ActuatorPreset.DEFAULT);

            VirtualDeviceConfigParser vdConfigParser = new VirtualDeviceConfigParser();


            VirtualDevice temperatureSensorVD = virtualDeviceFactory.createVirtualDevice(
                    tempSensorTD,
                    vdConfigParser.process(new File("src/com/extensions/tests/input/configs/TestConfig.json"))
            );

            // Create Temperature Monitoring application
            Application application = createApplication(appId, broker.getId());

            // Create the physical topology for the fog devices
            createPhysicalTopology(temperatureSensorVD);

            temperatureSensorVD.getSensorProperty("temperature").setApp(application);

            // Initialize a module mapping
            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();

            if(CLOUD) {
                for(AppModule appModule : application.getModules()) {
                    moduleMapping.addModuleToDevice(appModule.getName(), "cloud");
                }
            }

            // Create the controller for managing the simulation
            CustomController controller = new CustomController(
                    "iot-controller",
                    fogDevices,
                    temperatureSensorVD.getSensorProperties(),
                    temperatureSensorVD.getActuatorActions()
            );

            CustomMetricManager customMetricManager = controller.getCustomMetricManager();

            customMetricManager.registerMetric(new LongestApplicationLoopDelay());
            customMetricManager.registerMetric(new PeakEnergyConsumptionDevice());
            customMetricManager.registerMetric(new TotalEnergyConsumptionEfficiency());

            // Submit the application to the controller with the appropriate placement strategy
            controller.submitApplication(
                    application,
                    0,
                    (CLOUD) ? (new ModulePlacementMapping(fogDevices, application, moduleMapping))
                            : (new ModulePlacementEdgewards(
                            fogDevices,
                            temperatureSensorVD.getSensorProperties(),
                            temperatureSensorVD.getActuatorActions(),
                            application,
                            moduleMapping
                    ))
            );

            // Set the simulation start time
            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            // Start the CloudSim simulation
            CloudSim.startSimulation();

            // Stop the simulation once it completes
            CloudSim.stopSimulation();

            Log.printLine("IoT Application simulation finished!");
        } catch (IOException e) {
            Log.printLine(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void createPhysicalTopology(VirtualDevice virtualDevice) {
        // Create the cloud device at the top of the hierarchy
        FogDevice cloud = FogDeviceFactory.createFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25);

        // Cloud has no parent, it is the root of the hierarchy
        cloud.setParentId(-1);

        // Create the proxy server device as an intermediary between cloud and gateways
        FogDevice proxy = FogDeviceFactory.createFogDevice("proxy-server", 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333);

        // Set the cloud device as the parent of the proxy server
        proxy.setParentId(cloud.getId());

        // Latency of the connection from proxy server to cloud is 100 ms
        proxy.setUplinkLatency(100);

        // Add the cloud and proxy devices to the list of fog devices
        fogDevices.add(cloud);
        fogDevices.add(proxy);

        // Create a gateway device to represent the edge node closer to IoT devices
        FogDevice gateway = FogDeviceFactory.createFogDevice("gateway", 1000, 400, 10000, 10000, 50, 0.0,  81.63, 67.59);

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

        String rawTempProcessor = "temperature_sensor";
        String rawHumidityProcessor = "humidity_sensor";
        String dataProcessor = "data_processor";
        String TEMP_SENSOR = "temperature"; // THE TUPLE TYPE OF THE EDGE DEVICE NEEDS TO MATCH THE TUPLE TYPE OF THE CORRESPONDING SENSOR
        String HUMIDITY_SENSOR = "humidity";

        /*
         * Adding modules (vertices) to the application model (directed graph).
         * Each module represents a processing or functional unit in the application.
         */
        application.addAppModule(rawTempProcessor, 10); // Module for processing raw sensor data.
        application.addAppModule(rawHumidityProcessor, 10);
        application.addAppModule(dataProcessor, 10);     // Module for evaluating data and making decisions.

        /*
         * Connecting the application modules (vertices) in the application model (directed graph) with edges.
         * Each edge represents data flow (tuples) between modules, sensors, or actuators.
         */
        // Edge from the physical sensor to the processing module.
        application.addAppEdge(TEMP_SENSOR, rawTempProcessor, 500, 800, TEMP_SENSOR, Tuple.UP, AppEdge.SENSOR);

        // Edge from the sensor module to the data processor module.
        application.addAppEdge(rawTempProcessor, dataProcessor, 1000, 700, "PROCESSED_TEMP", Tuple.UP, AppEdge.MODULE);

        application.addAppEdge(HUMIDITY_SENSOR, rawHumidityProcessor, 500, 200, HUMIDITY_SENSOR, Tuple.UP, AppEdge.SENSOR);

        application.addAppEdge(rawHumidityProcessor, dataProcessor, 1000, 650, "PROCESSED_HUMIDITY", Tuple.UP, AppEdge.MODULE);

        /*
         * Defining tuple mappings for input-output relationships in each module.
         * Selectivity ratios specify how many output tuples are generated per input tuple.
         */
        application.addTupleMapping(rawTempProcessor, TEMP_SENSOR, "PROCESSED_TEMP",
                new FractionalSelectivity(1.0)); // 1 output tuple per input tuple in the sensor module.

        application.addTupleMapping(rawHumidityProcessor, HUMIDITY_SENSOR, "PROCESSED_HUMIDITY",
                new FractionalSelectivity(1.0));


        /*
         * Defining application loops to monitor and analyze application latency.
         * Loops represent a complete flow of data from source to destination.
         */
        final AppLoop loop1 = new AppLoop(new ArrayList<String>() {{
            add(TEMP_SENSOR);  // Start from the physical sensor.
            add(rawTempProcessor); // Pass through the sensor processing module.
            add(dataProcessor);     // End at the data processor module.
        }});

        final AppLoop loop2 = new AppLoop(new ArrayList<String>() {{
            add(HUMIDITY_SENSOR);  // Start from the physical sensor.
            add(rawHumidityProcessor); // Pass through the sensor processing module.
            add(dataProcessor);     // End at the data processor module.
        }});
        List<AppLoop> loops =  new ArrayList<AppLoop>() {{
            add(loop1); // Add the defined loop to the application.
            add(loop2);
        }};

        application.setLoops(loops); // Set the application loops for monitoring.

        return application; // Return the constructed application model.
    }
}
