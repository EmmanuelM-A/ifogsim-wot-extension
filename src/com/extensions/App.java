package com.extensions;

import com.extensions.customfog.CustomController;
import com.extensions.customfog.SensorProperty;
import com.extensions.custommetrics.CustomMetricManager;
import com.extensions.custommetrics.metrics.AverageTupleProcessingTimeMetric;
import com.extensions.custommetrics.metrics.SystemEfficiencyMetric;
import com.extensions.custommetrics.metrics.TaskCompletionCountMetric;
import com.extensions.sysconstructor.core.*;
import com.extensions.sysconstructor.eventdriver.EventDrivenApplication;
import com.extensions.utils.FilePaths;
import com.extensions.utils.presets.*;
import com.extensions.vdcreation.core.JsonFileProcessor;
import com.extensions.vdcreation.core.VirtualDevice;
import com.extensions.vdcreation.core.VirtualDeviceFactory;
import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.parsers.ThingDescriptionParser;
import com.extensions.vdcreation.parsers.VirtualDeviceConfigParser;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.Sensor;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.utils.TimeKeeper;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public final class App {
    public static void main(String[] args) {
        List<VirtualDevice> virtualDevices = new ArrayList<>();

        Log.printLine("Starting Simulation...");

        try {
            Log.disable();

            //////////////////////////////// INITIAL SETUP ////////////////////////////////

            // Specifies the number of users interacting with the cloud.
            int numUsers = 1;

            // Initializes a calendar object to track simulation time and events.
            Calendar calendar = Calendar.getInstance();

            // Determines whether to enable tracing of simulation events for debugging purposes.
            boolean traceFlag = false;

            // Initializes the CloudSim toolkit with the specified number of users, the calendar instance, and trace settings.
            CloudSim.init(numUsers, calendar, traceFlag);

            /*
            * Assigns a unique identifier to the application being simulated.
            * This ID is used to manage the application's components and operations.
            * */
            String appId = "Door-Security-Application"; // SET APPLICATION ID HERE

            // Initializes a FogBroker, which manages application modules and coordinates communication between them in the simulation.
            FogBroker broker = new FogBroker("broker");

            //////////////////////////////// VIRTUAL DEVICE CREATION ////////////////////////////////

            // Extract the metadata from the TDs
            List<ThingDescription> thingDescriptions = JsonFileProcessor.processJsonFiles(
                    FilePaths.JSON_THINGS_REPO, // SET THINGS REPO HERE
                    new ThingDescriptionParser()
            );

            // Set up the VD factory to create VDs with the appropriate presets
            VirtualDeviceFactory virtualDeviceFactory = new VirtualDeviceFactory(
                    broker.getId(),
                    appId,
                    FogDevicePreset.DEFAULT,
                    SensorPreset.DEFAULT,
                    ActuatorPreset.DEFAULT
            );
            VirtualDeviceConfigParser vdConfigParser = new VirtualDeviceConfigParser();

            // Create the virtual devices using the thing descriptions and factory method
            for(ThingDescription thingDescription : thingDescriptions) {
                VirtualDevice vd = virtualDeviceFactory.createVirtualDevice(
                        thingDescription,
                        null //vdConfigParser.process(new File(FilePaths.VD_CONFIG_FILE)) // SET VD'S CONFIG FILE HERE
                );
                // Validate VD HERE
                virtualDevices.add(vd);
            }

            //////////////////////////////// APPLICATION SETUP ////////////////////////////////

            ApplicationContext applicationContext = new ApplicationContext(
                    new File("src/com/extensions/input/application/door-security-application.json"), // SET THE NODE RED APPLICATION JSON FILE PATH HERE
                    CloudNodePreset.DEFAULT,
                    EdgeNodePreset.DEFAULT,
                    ApplicationPreset.DEFAULT
            );

            // Create the physical topology for the node red application
            ApplicationPhysicalTopology physicalTopology = JsonToPhysicalTopology.createApplicationPhysicalTopology(
                    virtualDevices,
                    applicationContext
            );

            // Create the application model for the node red application
            EventDrivenApplication application = JsonToApplicationModel.createApplicationModel(appId, broker.getId(), applicationContext);

            // Set the application for VD's sensors and actuators
            for(VirtualDevice virtualDevice : virtualDevices) {
                for(Sensor sensorProperty : virtualDevice.getSensorProperties()) {
                    sensorProperty.setApp(application);
                }

                for(Actuator actuatorAction : virtualDevice.getActuatorActions()) {
                    actuatorAction.setApp(application);
                }
            }

            // Create the controller for managing the simulation
            assert physicalTopology != null;
            CustomController controller = new CustomController(
                    "master-controller",
                    physicalTopology.getFogDevices(),
                    physicalTopology.getSensors(),
                    physicalTopology.getActuators()
            );

            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();

            //System.out.println(physicalTopology.getFogDevices().size());
            //System.out.println(physicalTopology.getActuators().size());
            //System.out.println(physicalTopology.getSensors().size());
            System.out.println(moduleMapping.getModuleMapping().isEmpty() ? "Empty" : "Not Empty");


            // Submit the application to the controller with the appropriate placement strategy
            controller.submitApplication(
                    application,
                    0,
                    new ModulePlacementEdgewards(
                            physicalTopology.getFogDevices(),
                            physicalTopology.getSensors(),
                            physicalTopology.getActuators(),
                            application,
                            moduleMapping
                    )
            );

            //////////////////////////////// REGISTER CUSTOM PERFORMANCE METRICS ////////////////////////////////

            /*CustomMetricManager customMetricManager = controller.getCustomMetricManager();

            customMetricManager.registerMetric(new AverageTupleProcessingTimeMetric());
            customMetricManager.registerMetric(new SystemEfficiencyMetric());
            customMetricManager.registerMetric(new TaskCompletionCountMetric());*/

            //////////////////////////////// SIMULATION ////////////////////////////////

            // Set the simulation start time
            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            // Start the CloudSim simulation
            CloudSim.startSimulation();

            // Stop the simulation once it completes
            CloudSim.stopSimulation();

            Log.printLine("IoT Application simulation finished!");

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
