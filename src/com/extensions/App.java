package com.extensions;

import com.extensions.customfog.CustomActuator;
import com.extensions.customfog.CustomController;
import com.extensions.customfog.CustomSensor;
import com.extensions.customfog.FogDeviceFactory;
import com.extensions.custommetrics.CustomMetricManager;
import com.extensions.custommetrics.metrics.LongestApplicationLoopDelay;
import com.extensions.custommetrics.metrics.PeakEnergyConsumptionDevice;
import com.extensions.custommetrics.metrics.TotalEnergyConsumptionEfficiency;
import com.extensions.sysconstructor.core.ApplicationPhysicalTopology;
import com.extensions.sysconstructor.core.JsonToApplication;
import com.extensions.sysconstructor.core.JsonToApplication2;
import com.extensions.utils.presets.*;
import com.extensions.vdcreation.core.VD;
import com.extensions.vdcreation.core.VDFactory;
import com.extensions.vdcreation.core.VirtualDevice;
import com.extensions.vdcreation.core.VirtualDeviceFactory;
import com.extensions.vdcreation.parsers.VirtualDeviceConfigParser;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.*;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.utils.Logger;
import org.fog.utils.TimeKeeper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public final class App {
    /**
     * Determines if the application is cloud-based
     */
    private static final boolean CLOUD = true;

    private static List<Sensor> allSensors;

    private static List<Actuator> allActuators;

    public static void main(String[] args) {
        Log.printLine("Starting Simulation...");

        try {
            //////////////////////////////// INITIAL SETUP ////////////////////////////////

            // This instance is responsible for loading in the node red application, creating the application topology and model and setting up related data
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
                    vdConfigParser.process(new File("src/com/extensions/input/configs/repo2-vd-configs.json")) // SET VDS CONFIG FILE HERE
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

            //////////////////////////////// REGISTER CUSTOM PERFORMANCE METRICS ////////////////////////////////

            CustomMetricManager customMetricManager = controller.getCustomMetricManager();

            //customMetricManager.registerMetric(new LongestApplicationLoopDelay());
            customMetricManager.registerMetric(new PeakEnergyConsumptionDevice());
            customMetricManager.registerMetric(new TotalEnergyConsumptionEfficiency());

            //////////////////////////////// SIMULATION ////////////////////////////////

            // Set the simulation start time
            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            // Start the CloudSim simulation
            CloudSim.startSimulation();

            // Stop the simulation once it completes
            CloudSim.stopSimulation();

        } catch(Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
