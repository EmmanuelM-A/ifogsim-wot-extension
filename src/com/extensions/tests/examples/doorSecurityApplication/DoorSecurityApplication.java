package com.extensions.tests.examples.doorSecurityApplication;

import com.extensions.sysconstructor.eventdriver.EventManager;
import com.extensions.sysconstructor.eventdriver.EventSensor;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Actuator;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;

import java.util.*;

/**
 * This application uses the automatic physical topology constructor mechanism but the application model and
 * event handling are done manually. This class tests the effectiveness of the physical topology constructor mechanism.
 */
public class DoorSecurityApplication {
    /**
     * Determines if the application is cloud-based
     */
    private static final boolean CLOUD = true;

    //private static final List<CustomFogDevice> fogDevices;

    private static List<Sensor> allSensors;

    private static List<Actuator> allActuators;

    public static void main(String[] args) {
        /*Log.printLine("Starting Simulation....");

        try {
            //////////////////////////////// INITIAL SETUP ////////////////////////////////

            // This instance is responsible for loading in the node red application and setting up related data
            JsonToApplication jsonToApplication = new JsonToApplication(
                    CloudNodePreset.DEFAULT,
                    EdgeNodePreset.DEFAULT,
                    ApplicationPreset.DEFAULT,
                    new File("src/com/extensions/input/application/door-security-application.json") // The file path of the Node-RED application design
            );

            // Disables iFogSim's logging mechanism, only displaying simulation results
            Log.disable();

            // The number of cloud users
            int numUsers = 1;

            // An instance of the calendar
            Calendar calendar = Calendar.getInstance();

            // Mean trace events
            boolean trace_flag = false;

            CloudSim.init(numUsers, calendar, trace_flag);

            // Identifier of the application
            String appId = jsonToApplication.getApplicationTopologyParser().parseApplicationTitle();

            // Initializes a FogBroker, which manages application modules and coordinates communication between them in the simulation.
            FogBroker broker = new FogBroker("broker");

            //////////////////////////////// VIRTUAL DEVICE CREATION ////////////////////////////////

            // The parser used to extract virtual device configurations if there are any
            VirtualDeviceConfigParser vdConfigParser = new VirtualDeviceConfigParser();

            // Create the virtual devices using the thing descriptions repo folder
            List<VirtualDevice> virtualDevices = VirtualDeviceFactory.createVirtualDevices(
                    broker.getId(),
                    appId,
                    FogDevicePreset.DEFAULT,
                    SensorPreset.DEFAULT,
                    ActuatorPreset.DEFAULT,
                    "src/com/extensions/input/things/repo2", // SET THINGS REPO HERE
                    vdConfigParser.process(new File("src/com/extensions/input/configs/repo2-vd-configs.json")) // SET VD'S CONFIG FILE HERE
            );

            //////////////////////////////// APPLICATION SETUP ////////////////////////////////

            // Create the physical topology for the application
            ApplicationPhysicalTopology physicalTopology = jsonToApplication.createApplicationPhysicalTopology(virtualDevices);

            allSensors = physicalTopology.getSensors();
            allActuators = physicalTopology.getActuators();

            // Create the application model for the application
            Application application = createApplication(appId, broker.getId());
            //List<Application> applications = jsonToApplication.createApplicationModels(appId, broker.getId(), allSensors);

            for(int index = 0; index < applications.size(); index++) {
                // Get application
                Application application = applications.get(index);

                // Set the application for VD's sensors and actuators
                for (VirtualDevice virtualDevice : virtualDevices) {
                    for (Sensor sensorProperty : virtualDevice.getSensorProperties()) {
                        sensorProperty.setApp(application);
                    }
                    for (Actuator actuatorAction : virtualDevice.getActuatorActions()) {
                        actuatorAction.setApp(application);
                    }
                    for (Sensor eventSensor : virtualDevice.getEventSensors()) {
                        eventSensor.setApp(application);
                    }
                }

                // Create the controller for managing the simulation
                CustomController controller = new CustomController("master-controller", physicalTopology.getFogDevices(), physicalTopology.getSensors(), physicalTopology.getActuators());

                // Set up module mapping
                ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();

                // If cloud based deployment then connect all app modules to the cloud device/node
                if (CLOUD) {
                    for (AppModule appModule : application.getModules()) {
                        moduleMapping.addModuleToDevice(appModule.getName(), "cloud");
                    }
                }

                // Submit the application to the controller with the appropriate placement strategy
                controller.submitApplication(
                        application,
                        (index + 1) * 100,
                        (CLOUD) ? (new ModulePlacementMapping(physicalTopology.getFogDevices(), application, moduleMapping))
                                : (new ModulePlacementEdgewards(
                                physicalTopology.getFogDevices(),
                                physicalTopology.getSensors(),
                                physicalTopology.getActuators(),
                                application,
                                moduleMapping
                        ))
                );
            }

            //////////////////////////////// REGISTER CUSTOM PERFORMANCE METRICS ////////////////////////////////

            /*CustomMetricManager customMetricManager = controller.getCustomMetricManager();

            customMetricManager.registerMetric(new LongestApplicationLoopDelay());
            customMetricManager.registerMetric(new PeakEnergyConsumptionDevice());
            customMetricManager.registerMetric(new TotalEnergyConsumptionEfficiency());

            //////////////////////////////// SIMULATION ////////////////////////////////

            // Set the simulation start time
            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            // Start the CloudSim simulation
            CloudSim.startSimulation();

            // Stop the simulation once it completes
            CloudSim.stopSimulation();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/
    }

    private static Application createApplication(String appId, int userId) {
        Application application = Application.createApplication(appId, userId);

        application.setLoops(new ArrayList<>());

        // Data Flow
        setDataFlowForApplication(application);

        // Event Flow
        setEventFlowForApplication(application);

        return application;
    }

    private static void setDataFlowForApplication(Application application) {
        // Processing modules
        application.addAppModule("processing-0", 50);

        // App Edges
        application.addAppEdge("lockState", "processing-0", 700, 900, "lockState", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("processing-0", "lockDoor", 850, 350, "lockDoor", Tuple.DOWN, AppEdge.ACTUATOR);

        // Tuple Mappings
        application.addTupleMapping("processing-0", "lockState", "lockDoor", new FractionalSelectivity(1.0));

        // App Loops
        AppLoop loop = new AppLoop(Arrays.asList("lockState", "processing-0", "lockDoor"));
        application.getLoops().add(loop);

    }

    private static Sensor getSensorBy(String name) {
        for(Sensor sensor : allSensors) {
            if(sensor.getName().equals(name)) return sensor;
        }
        return null;
    }

    private static void setEventFlowForApplication(Application application) {
        String EPM1 = "EventProcessor";
        String EPM2 = "event-processing-2";
        /*String EPM3 = "event-processing-3";
        String EPM4 = "event-processing-4";
        String EPM5 = "event-processing-5";
        String EPM6 = "event-processing-6";*/

        application.addAppModule(EPM1, 50); // For onDoorLockedEvent()
        application.addAppModule(EPM2, 50); // For onTamperAlertEvent()
        /*application.addAppModule(EPM3, 50); // For onSnapshotTakenEvent()
        application.addAppModule(EPM4, 50); // For onSnapshotTakenEvent()
        application.addAppModule(EPM5, 50); // For onAlarmTriggeredEvent()
        application.addAppModule(EPM6, 50);*/

        // onDoorLockedEvent()
        EventSensor doorLockedSensor =  (EventSensor) getSensorBy("doorLocked");
        EventManager.getInstance().registerEventSensor(doorLockedSensor);

        application.addAppModule(EPM1, 50);

        application.addAppEdge("doorLocked", EPM1, 485, 890, "doorLocked", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(EPM1, "showAlert", 866, 789, "showAlert", Tuple.DOWN, AppEdge.ACTUATOR);

        application.addTupleMapping(EPM1, "doorLocked", "showAlert", new FractionalSelectivity(1.0));

        AppLoop loop0 = new AppLoop(Arrays.asList("doorLocked", EPM1, "showAlert"));
        application.getLoops().add(loop0);

        EventManager.getInstance().triggerEvent("doorLocked");

        // onTamperAlertEvent()
        EventSensor tamperAlertSensor =  (EventSensor) getSensorBy("tamperAlert");
        if(tamperAlertSensor == null) {
            System.out.println("SENSOR NOT HERE!");
        } else {
            System.out.println("SENSOR HERE!");
        }
        EventManager.getInstance().registerEventSensor(tamperAlertSensor);

        application.addAppEdge("tamperAlert", EPM2, 500, 1000, "tamperAlert", Tuple.UP, AppEdge.SENSOR);

        application.addAppEdge(EPM2, "activate", 600, 570, "activate", Tuple.DOWN, AppEdge.ACTUATOR);
        application.addAppEdge(EPM2, "takeSnapshot", 569, 458, "takeSnapshot", Tuple.DOWN, AppEdge.ACTUATOR);

        application.addTupleMapping(EPM2, "tamperAlert", "activate", new FractionalSelectivity(1.0));
        application.addTupleMapping(EPM2, "tamperAlert", "takeSnapshot", new FractionalSelectivity(1.0));

        AppLoop loop1 = new AppLoop(Arrays.asList("tamperAlert", EPM2, "activate"));
        AppLoop loop2 = new AppLoop(Arrays.asList("tamperAlert",  EPM2, "takeSnapshot"));

        application.getLoops().add(loop1);
        application.getLoops().add(loop2);

        EventManager.getInstance().triggerEvent("tamperAlert");

        // Sensor to Main Processing Module
        /*application.addAppEdge("tamperAlert", EPM2, 500, 1000, "tamperAlert", Tuple.UP, AppEdge.SENSOR);

        // Processing Module to GENERAL-ACTUATOR
        application.addAppEdge(EPM2, "GENERAL-ACTUATOR", 600, 570, "broadcast", Tuple.DOWN, AppEdge.ACTUATOR);

        // Tuple Mapping - EPM2 sends data to GENERAL-ACTUATOR
        application.addTupleMapping(EPM2, "tamperAlert", "broadcast", new FractionalSelectivity(1.0));

        // Define Application Loop (Only GENERAL-ACTUATOR is explicitly modeled)
        AppLoop loop = new AppLoop(Arrays.asList("tamperAlert", EPM2, "GENERAL-ACTUATOR"));
        application.getLoops().add(loop);*/

        // onSnapshotTakenEvent()
        /*application.addAppModule("getStreamURL()", 50);
        application.addAppModule("getLastSnapshot()", 50);

        // Branch 1
        application.addAppEdge("snapshotTaken", "getStreamURL()", 768, 679, "snapshotTaken", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("getStreamURL()", EPM3, 567, 589, "PROCESSED_DATA_1", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge(EPM3, "updateDisplay", 520, 310, "updateDisplay", Tuple.DOWN, AppEdge.ACTUATOR);

        application.addTupleMapping("getStreamURL()", "snapshotTaken", "PROCESSED_DATA_1", new FractionalSelectivity(1.0));
        application.addTupleMapping(EPM3, "PROCESSED_DATA_1", "updateDisplay", new FractionalSelectivity(1.0));

        // Branch 2
        application.addAppEdge("snapshotTaken_snapshot", "getLastSnapshot()", 768, 679, "snapshotTaken", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("getLastSnapshot()", EPM4, 567, 589, "PROCESSED_DATA_2", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge(EPM4, "updateDisplay", 520, 310, "updateDisplay", Tuple.DOWN, AppEdge.ACTUATOR);

        application.addTupleMapping("getLastSnapshot()", "snapshotTaken", "PROCESSED_DATA_2", new FractionalSelectivity(1.0));
        application.addTupleMapping(EPM4, "PROCESSED_DATA_2", "updateDisplay", new FractionalSelectivity(1.0));

        AppLoop loop3 = new AppLoop(Arrays.asList("snapshotTaken", "getStreamURL()", EPM3, "updateDisplay"));
        AppLoop loop4 = new AppLoop(Arrays.asList("snapshotTaken_snapshot", "getLastSnapshot()", EPM4, "updateDisplay"));
        application.getLoops().add(loop3);
        application.getLoops().add(loop4);

        // onAlarmTriggeredEvent()
        application.addAppEdge("alarmTriggered", EPM5, 485, 890, "alarmTriggered", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(EPM5, "showAlert", 866, 789, "showAlert", Tuple.DOWN, AppEdge.ACTUATOR);

        application.addTupleMapping(EPM5, "alarmTriggered", "showAlert", new FractionalSelectivity(1.0));

        AppLoop loop5 = new AppLoop(Arrays.asList("alarmTriggered", EPM5, "showAlert"));
        application.getLoops().add(loop5);*/
    }
}
