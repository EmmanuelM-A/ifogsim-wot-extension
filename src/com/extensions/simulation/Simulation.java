package com.extensions.simulation;

import com.extensions.customfog.CustomController;
import com.extensions.custommetrics.CustomMetricManager;
import com.extensions.custommetrics.CustomPerformanceMetric;
import com.extensions.sysconstructor.core.ApplicationPhysicalTopology;
import com.extensions.sysconstructor.core.JsonToApplication;
import com.extensions.sysconstructor.core.ThingQuantityParser;
import com.extensions.utils.presets.*;
import com.extensions.vdcreation.core.VirtualDevice;
import com.extensions.vdcreation.core.VirtualDeviceFactory;
import com.extensions.vdcreation.parsers.VirtualDeviceConfigParser;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.Sensor;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.utils.TimeKeeper;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * This class handles everything to do with the application and its simulation.
 */
public class Simulation {
    /**
     * Responsible for collecting all data after the simulation is complete and printing it out.
     */
    private static CustomController controller = null;

    /**
     * Responsible for handling all custom metrics registered to the application and their evaluation.
     */
    private static CustomMetricManager customMetricManager = null;

    /**
     * Sets up all data and components need to run the application.
     *
     * @param isCloudDeployment Determines if the application will be cloud deployment or not.
     * @param nodeRedApplicationJsonFile The location of the node red application json file.
     * @param thingsRepo The location of the folder that contains the things used in the application.
     * @param vdsConfigFile The location of virtual device configuration file.
     * @param thingQuantitiesFile The location of thing quantities file.
     * @param cloudNodePreset The preset used to configure the cloud node.
     * @param edgeNodePreset The presets used to configure all edge nodes.
     * @param applicationPreset The presets used to configure the application.
     * @param numUsers The number of cloud users/components that interact with the cloud.
     * @param fogDevicePreset The presets used to configure any fog device.
     * @param sensorPreset The presets used to configure a sensor.
     * @param actuatorPreset The presets used to configure an actuator.
     */
    public static void setupApplication(
            final boolean isCloudDeployment,
            final String nodeRedApplicationJsonFile,
            final String thingsRepo,
            final String vdsConfigFile,
            final String thingQuantitiesFile,
            final CloudNodePreset cloudNodePreset,
            final EdgeNodePreset edgeNodePreset,
            final ApplicationPreset applicationPreset,
            final int numUsers,
            final FogDevicePreset fogDevicePreset,
            final SensorPreset sensorPreset,
            final ActuatorPreset actuatorPreset
    ) throws Exception {
        Log.printLine("Starting Simulation....");

        //////////////////////////////// INITIAL SETUP ////////////////////////////////

        // Parses the VD quantities file and extracts the quantity of each thing to be used in the application
        ThingQuantityParser vdQuantities = new ThingQuantityParser(new File(thingQuantitiesFile));

        // This instance is responsible for loading in the node red application, creating the application topology and model and setting up related data
        JsonToApplication jsonToApplication = new JsonToApplication(
                cloudNodePreset,
                edgeNodePreset,
                applicationPreset,
                new File(nodeRedApplicationJsonFile),
                vdQuantities
        );

        // Disables iFogSim's logging mechanism, only displaying simulation results
        Log.disable();

        // Initialise the CloudSim Simulation engine
        CloudSim.init(numUsers, Calendar.getInstance(), false);

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
                fogDevicePreset,
                sensorPreset,
                actuatorPreset,
                thingsRepo,
                vdConfigParser.process(new File(vdsConfigFile)),
                vdQuantities.getThingFrequencies()
        );

        //////////////////////////////// APPLICATION SETUP ////////////////////////////////

        // Create the physical topology for the application
        ApplicationPhysicalTopology physicalTopology = jsonToApplication.createApplicationPhysicalTopology(virtualDevices);

        // Set the sensors list (NEEDED TO CREATE THE APPLICATION MODEL BELOW)
        jsonToApplication.setAllSensors(physicalTopology.getSensors());

        // Create the application model for the application
        Application application = jsonToApplication.createApplicationModel(appId, broker.getId());

        // Set the application value for VD
        for(VirtualDevice virtualDevice : virtualDevices) {
            virtualDevice.getSensor().setApp(application);
            virtualDevice.getActuator().setApp(application);

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
        controller = new CustomController("master-controller", physicalTopology.getFogDevices(), physicalTopology.getSensors(), physicalTopology.getActuators());

        // Set up module mapping
        ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();

        // If cloud based deployment then connect all app modules to the cloud device/node
        if (isCloudDeployment) {
            for (AppModule appModule : application.getModules()) {
                //if(appModule.getName().equals("MasterModule") || appModule.getName().startsWith("WorkerModule-")) {
                moduleMapping.addModuleToDevice(appModule.getName(), "cloud");
                //}
            }
        }

        // Submit the application to the controller with the appropriate placement strategy
        controller.submitApplication(
                application,
                0,
                (isCloudDeployment) ? (new ModulePlacementMapping(physicalTopology.getFogDevices(), application, moduleMapping))
                        : (new ModulePlacementEdgewards(
                        physicalTopology.getFogDevices(),
                        physicalTopology.getSensors(),
                        physicalTopology.getActuators(),
                        application,
                        moduleMapping
                ))
        );

        // Initialize the custom metrics manager
        if(customMetricManager == null) {
            customMetricManager = controller.getCustomMetricManager();
        }
    }

    /**
     * Runs the application once it has been set up and all custom metrics have been registered (if any).
     * @throws IOException If an error occurs when simulation process.
     */
    public static void run() throws IOException {
        // Checks if the setupApplication method has been called
        if(controller == null && customMetricManager == null) throw new IOException("Application has not been setup! You must setup application first - please call Simulation.setupApplication(...) prior to this method!");

        // Set the simulation start time
        TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

        // Start the CloudSim simulation
        CloudSim.startSimulation();

        // Stop the simulation once it completes
        CloudSim.stopSimulation();
    }

    /**
     * Registers a custom performance metric to the application.
     * @param metric The custom performance metric to register.
     * @throws IOException If any error occurs during the registration process.
     */
    public static void registerCustomMetric(CustomPerformanceMetric<?> metric) throws IOException {
        // Checks if the setupApplication method has been called
        if(controller == null && customMetricManager == null) throw new IOException("Application has not been setup! You must setup application first - please call Simulation.setupApplication(...) prior to this method!");

        // Register custom metric
        customMetricManager.registerMetric(metric);
    }
}
