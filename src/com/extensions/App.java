package com.extensions;

import com.extensions.customfog.CustomController;
import com.extensions.custommetrics.CustomMetricManager;
import com.extensions.custommetrics.metrics.PeakEnergyConsumptionDevice;
import com.extensions.custommetrics.metrics.TotalEnergyConsumptionEfficiency;
import com.extensions.sysconstructor.core.ApplicationPhysicalTopology;
import com.extensions.sysconstructor.core.JsonToApplication;
import com.extensions.utils.presets.*;
import com.extensions.vdcreation.core.VirtualDevice;
import com.extensions.vdcreation.core.VirtualDeviceFactory;
import com.extensions.vdcreation.parsers.VirtualDeviceConfigParser;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.*;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.utils.TimeKeeper;

import java.io.File;
import java.util.Calendar;
import java.util.List;

// TODO - FINALISE AND TIDY UP CODE EVERYWHERE, CLEAN UP PROJECT FOLDER, DELETE UNNECESSARY CODE, ADD JAVADOC
// TODO - SORT OUT CONFIGURATIONS, PRESETS AND INPUT VALUES & INVESTIGATE OUTPUT DATA
// TODO - LOOK INTO WHY TUPLE EXECUTION DELAY & APP LOOP DELAY DON'T DISPLAY WHEN CLOUD = FALSE

public final class App {
    /**
     * Determines if the application is cloud-based
     */
    private static final boolean CLOUD = true;

    private static final String NODE_RED_APPLICATION_JSON = "src/com/extensions/tests/examples/doorSecurityApplication/door-security-application.json";

    private static final String THINGS_REPO = "src/com/extensions/tests/examples/doorSecurityApplication/things";

    private static final String VDS_CONFIG_FILE = "src/com/extensions/tests/examples/doorSecurityApplication/configs/vd-configs.json";

    private static final String VD_QUANTITIES_FILE = "src/com/extensions/tests/examples/doorSecurityApplication/configs/vd-quantities.json";
    public static void main(String[] args) {
        Log.printLine("Starting Simulation...");

        try {
            //////////////////////////////// INITIAL SETUP ////////////////////////////////

            // This instance is responsible for loading in the node red application, creating the application topology and model and setting up related data
            JsonToApplication jsonToApplication = new JsonToApplication(
                    CloudNodePreset.DEFAULT, // CHANGEABLE
                    EdgeNodePreset.DEFAULT, // CHANGEABLE
                    ApplicationPreset.DEFAULT, // CHANGEABLE
                    new File(NODE_RED_APPLICATION_JSON)
            );

            // Disables iFogSim's logging mechanism, only displaying simulation results
            Log.disable();

            // The number of cloud users/the number of components (or individuals) that interact with the cloud
            int numUsers = 1;

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
                    FogDevicePreset.DEFAULT, // CHANGEABLE
                    SensorPreset.DEFAULT, // CHANGEABLE
                    ActuatorPreset.DEFAULT, // CHANGEABLE
                    THINGS_REPO,
                    vdConfigParser.process(new File(VDS_CONFIG_FILE))
            );

            //////////////////////////////// APPLICATION SETUP ////////////////////////////////

            // Create the physical topology for the application
            ApplicationPhysicalTopology physicalTopology = jsonToApplication.createApplicationPhysicalTopology(virtualDevices, new File(VD_QUANTITIES_FILE));

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
                    //if(appModule.getName().equals("MasterModule") || appModule.getName().startsWith("WorkerModule-")) {
                        moduleMapping.addModuleToDevice(appModule.getName(), "cloud");
                    //}
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
