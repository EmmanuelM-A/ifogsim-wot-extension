package com.extensions.tests;

import com.extensions.customfog.FogDeviceFactory;
import com.extensions.sysconstructor.core.ApplicationPhysicalTopology;
import com.extensions.sysconstructor.core.JsonToPhysicalTopology;
import com.extensions.utils.presets.ActuatorPreset;
import com.extensions.utils.presets.FogDeviceHostPreset;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.utils.presets.SensorPreset;
import com.extensions.vdcreation.core.VirtualDevice;
import com.extensions.vdcreation.core.VirtualDeviceConfig;
import com.extensions.vdcreation.core.VirtualDeviceFactory;
import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.parsers.ThingDescriptionParser;
import com.extensions.vdcreation.parsers.VirtualDeviceConfigParser;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.*;
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

// TODO - CREATE AN EXACT COPY OF TEMPERATURE MONITORING APPLICATION
// TODO - TEST CUSTOM PERFORMANCE CHECKS
// TODO - TEST EVENT DRIVEN HANDLING
// TODO - TRY AND AUTOMATE CONSTRUCTION SO IT TOPOLOGY AND MODEL ARE SIMILAR TO THE MANUAL CONSTRUCTION

/**
    A simple IoT application which consists of one Virtual Device, a temperature sensor. This application is a basic Temperature
    Monitoring System, where a temperature sensor reads and sends data periodically to a gateway device, that acts as a
    communication hub for the sensor.
 */
public class TemperatureMonitor {
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

            ThingDescription tempSensorThing = tdParser.process(
                    new File("src/com/extensions/input/things/temperature-sensor.json")
            );

            ThingDescription smartDisplayThing = tdParser.process(
                    new File("src/com/extensions/input/things/smart-display.json")
            );

            List<ThingDescription> tds = new ArrayList<>();
            tds.add(tempSensorThing);
            tds.add(smartDisplayThing);

            // Create VD from TD
            VirtualDeviceFactory virtualDeviceFactory = new VirtualDeviceFactory(broker.getId(), appId, FogDevicePreset.DEFAULT, SensorPreset.DEFAULT, ActuatorPreset.DEFAULT);
            List<VirtualDevice> vds = new ArrayList<>();
            for(ThingDescription td : tds) {
                VirtualDevice vd = virtualDeviceFactory.createVirtualDevice(
                        td,
                        new VirtualDeviceConfigParser().process(new File("src/com/extensions/input/configs/vd-configs.json"))
                );
                //VirtualDevice.printVirtualDeviceData(vd);
                vds.add(vd);
            }

            // Create Temperature Monitoring application
            Application application = createApplication(appId, broker.getId());

            // Create the physical topology for the fog devices
            ApplicationPhysicalTopology physicalTopology = createPhysicalTopology(vds);


            /*for(Sensor sensor : physicalTopology.getSensors()) {
                sensor.setApp(application);
                //System.out.println(sensor.getName());
            }

            for(Actuator actuator : physicalTopology.getActuators()) {
                actuator.setApp(application);
                //System.out.println(actuator.getName());
            }*/

            // Set the application for VD's sensors and actuators
            for(VirtualDevice virtualDevice : vds) {
                for(Sensor sensorProperty : virtualDevice.getSensorProperties()) {
                    sensorProperty.setApp(application);
                }

                for(Actuator actuatorAction : virtualDevice.getActuatorActions()) {
                    actuatorAction.setApp(application);
                }
            }

            // Initialize a module mapping to map application modules to fog devices
            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();

            // Assign specific application modules to the cloud
            moduleMapping.addModuleToDevice("processing", "processing_node"); // Assign data processing to the cloud

            System.out.println(physicalTopology.getFogDevices());

            // Create the controller for managing the simulation
            Controller controller = new Controller(
                    "iot-controller",
                    physicalTopology.getFogDevices(),
                    physicalTopology.getSensors(),
                    physicalTopology.getActuators()
            );

            // Submit the application to the controller with the appropriate placement strategy
            controller.submitApplication(
                    application,
                    0,
                    (CLOUD) ? (new ModulePlacementMapping(physicalTopology.getFogDevices(), application, moduleMapping))
                            : (new ModulePlacementEdgewards(
                                    physicalTopology.getFogDevices(),
                                    physicalTopology.getSensors(),
                                    physicalTopology.getActuators(),
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

    private static ApplicationPhysicalTopology createPhysicalTopology(List<VirtualDevice> virtualDevices) {
        List<FogDevice> fogDevices = new ArrayList<>();
        List<FogDevice> edgeNodes = new ArrayList<>();

        // Create the cloud device at the top of the hierarchy
        FogDevice cloud = FogDeviceFactory.createFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25);

        // Cloud has no parent, it is the root of the hierarchy
        cloud.setParentId(-1);

        // Add the cloud device to the list of fog devices
        fogDevices.add(cloud);

        // An edge node that will act as a processing node
        FogDevice processingNode = FogDeviceFactory.createFogDevice("processing_node", 2800, 2000, 100, 100, 0, 0.05, 2500, 800);

        // Set the parent id to the cloud
        processingNode.setParentId(cloud.getId());

        // Add to the fog device list
        fogDevices.add(processingNode);
        edgeNodes.add(processingNode);

        // Set parent id of all the vd's fog devices
        for(VirtualDevice virtualDevice : virtualDevices) {
            virtualDevice.getFogDevice().setParentId(processingNode.getId());
            fogDevices.add(virtualDevice.getFogDevice());
        }

        List<String> sensorsAndActuatorsUsed = List.of("temperature", "updateDisplay");

        List<Sensor> allSensorsUsedInApplication = new ArrayList<>();
        List<Actuator> allActuatorsUsedInApplication = new ArrayList<>();

        // Iterate once over VDs to collect sensors & actuators
        for (VirtualDevice virtualDevice : virtualDevices) {
            for (Sensor sensor : virtualDevice.getSensorProperties()) {
                if (sensorsAndActuatorsUsed.contains(sensor.getName())) {
                    allSensorsUsedInApplication.add(sensor);
                    //System.out.println(sensor.getName());
                }
            }
            for (Actuator actuator : virtualDevice.getActuatorActions()) {
                if (sensorsAndActuatorsUsed.contains(actuator.getName())) {
                    allActuatorsUsedInApplication.add(actuator);
                    //System.out.println(actuator.getName());
                }
            }
        }

        ApplicationPhysicalTopology physicalTopology = new ApplicationPhysicalTopology();
        physicalTopology.setFogDevices(fogDevices);
        physicalTopology.setEdgeNodes(edgeNodes);
        physicalTopology.setActuators(allActuatorsUsedInApplication);
        physicalTopology.setSensors(allSensorsUsedInApplication);

        return physicalTopology;
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

        //String TEMP_SENSOR = "temperature"; // THE TUPLE TYPE OF THE EDGE DEVICE NEEDS TO MATCH THE TUPLE TYPE OF THE CORRESPONDING SENSOR

        /*
         * Adding modules (vertices) to the application model (directed graph).
         * Each module represents a processing or functional unit in the application.
         */
        application.addAppModule("temperature", 10);
        application.addAppModule("processing", 20); // Module for evaluating data and making decisions.


        /*
         * Connecting the application modules (vertices) in the application model (directed graph) with edges.
         * Each edge represents data flow (tuples) between modules, sensors, or actuators.
         */
        // Edge from the physical sensor to the processing module.
        application.addAppEdge("temperature", "processing", 500, 200, "temperature", Tuple.UP, AppEdge.SENSOR);

        // Edge from the processing module to the updateDisplay actuator.
        application.addAppEdge("processing", "updateDisplay", 500, 200, "temperature", Tuple.DOWN, AppEdge.ACTUATOR);

        System.out.println(application.getEdges());

        /*
         * Defining tuple mappings for input-output relationships in each module.
         * Selectivity ratios specify how many output tuples are generated per input tuple.
         */
        application.addTupleMapping("processing", "temperature", "temperature",
                new FractionalSelectivity(1.0)); // 1 output tuple per input tuple in the sensor module.


        /*
         * Defining application loops to monitor and analyze application latency.
         * Loops represent a complete flow of data from source to destination.
         */
        final AppLoop loop1 = new AppLoop(new ArrayList<String>() {{
            add("temperature");  // Start from the physical sensor.
            add("processing"); // Pass through the sensor processing module.
            //add("updateDisplay");     // End at the updateDisplay actuator.
        }});
        List<AppLoop> loops =  new ArrayList<AppLoop>() {{
            add(loop1); // Add the defined loop to the application.
        }};

        application.setLoops(loops); // Set the application loops for monitoring.

        return application; // Return the constructed application model.
    }
}
