package com.extensions.tests;

import com.extensions.customfog.CustomController;
import com.extensions.custommetrics.CustomMetricManager;
import com.extensions.custommetrics.metrics.LongestApplicationLoopDelay;
import com.extensions.custommetrics.metrics.PeakEnergyConsumptionDevice;
import com.extensions.custommetrics.metrics.TotalEnergyConsumptionEfficiency;
import com.extensions.sysconstructor.core.ApplicationPhysicalTopology;
import com.extensions.sysconstructor.core.JsonToApplication;
import com.extensions.utils.FilePaths;
import com.extensions.utils.Utility;
import com.extensions.utils.presets.*;
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
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.utils.TimeKeeper;

import java.io.File;
import java.io.IOException;
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

    public static void main(String[] args) {
        Log.printLine("Starting Simulation....");

        try {
            //////////////////////////////// INITIAL SETUP ////////////////////////////////

            // This instance is responsible for loading in the node red application and setting up connections
            JsonToApplication jsonToApplication = new JsonToApplication(
                    CloudNodePreset.DEFAULT,
                    EdgeNodePreset.DEFAULT,
                    ApplicationPreset.DEFAULT,
                    new File("src/com/extensions/input/application/door-security-application.json")
            );

            // Disables iFogSim's logging mechanism, only display simulation results
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
                    vdConfigParser.process(new File(FilePaths.VD_CONFIG_FILE)) // SET VD'S CONFIG FILE HERE
            );

            //Utility.printVirtualDevices(virtualDevices, "Main Class");

            //////////////////////////////// APPLICATION SETUP ////////////////////////////////

            // Create the physical topology for the application
            ApplicationPhysicalTopology physicalTopology = jsonToApplication.createApplicationPhysicalTopology(virtualDevices);

            // Create the application model for the application
            Application application = createApplication(appId, broker.getId());
            //Application application = jsonToApplication.createApplicationModel(appId, broker.getId());

            System.out.println("AppModules: " + application.getModules().size());
            System.out.println("AppEdges: " + application.getEdges().size());
            //System.out.println("AppModules: " + application.getModules().size());

            // Set the application for VD's sensors and actuators
            for(VirtualDevice virtualDevice : virtualDevices) {
                for(Sensor sensorProperty : virtualDevice.getSensorProperties()) {
                    sensorProperty.setApp(application);
                }
                for(Actuator actuatorAction : virtualDevice.getActuatorActions()) {
                    actuatorAction.setApp(application);
                }
                for(Sensor eventSensor : virtualDevice.getEventSensors()) {
                    eventSensor.setApp(application);
                }
            }

            // Create the controller for managing the simulation
            CustomController controller = new CustomController("master-controller", physicalTopology.getFogDevices(), physicalTopology.getSensors(), physicalTopology.getActuators());

            // Set up module mapping
            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();

            // If cloud based deployment then connect all app modules to the cloud device/node
            if(CLOUD) {
                for(AppModule appModule : application.getModules()) {
                    moduleMapping.addModuleToDevice(appModule.getName(), "cloud");
                }
            }

            // Submit the application to the controller with the appropriate placement strategy
            controller.submitApplication(
                    application,
                    0,
                    (CLOUD)?(new ModulePlacementMapping(physicalTopology.getFogDevices(), application, moduleMapping))
                            :(new ModulePlacementEdgewards(
                            physicalTopology.getFogDevices(),
                            physicalTopology.getSensors(),
                            physicalTopology.getActuators(),
                            application,
                            moduleMapping
                    ))
            );

            //////////////////////////////// REGISTER CUSTOM PERFORMANCE METRICS ////////////////////////////////

            /*CustomMetricManager customMetricManager = controller.getCustomMetricManager();

            customMetricManager.registerMetric(new LongestApplicationLoopDelay());
            customMetricManager.registerMetric(new PeakEnergyConsumptionDevice());
            customMetricManager.registerMetric(new TotalEnergyConsumptionEfficiency());*/

            //////////////////////////////// SIMULATION ////////////////////////////////

            // Set the simulation start time
            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            // Start the CloudSim simulation
            CloudSim.startSimulation();

            // Stop the simulation once it completes
            CloudSim.stopSimulation();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Application createApplication(String appId, int userId) {
        Application application = Application.createApplication(appId, userId);

        application.setLoops(new ArrayList<>());

        // Data Flow
        setDataFlowForApplication(application);

        // Event Flow

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
}
