package com.extensions.tests.testing;

import com.extensions.customfog.CustomController;
import com.extensions.custommetrics.CustomMetricManager;
import com.extensions.custommetrics.metrics.MeanTupleExecutionDelay;
import com.extensions.sysconstructor.core.ApplicationPhysicalTopology;
import com.extensions.sysconstructor.core.JsonToApplication;
import com.extensions.sysconstructor.core.ThingQuantityParser;
import com.extensions.tests.testing.helper.InputHandler;
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
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.utils.TimeKeeper;

import java.io.File;
import java.util.Calendar;
import java.util.List;

public final class MainTester {
    /**
     * Determines if the application is cloud-based
     */
    private static final boolean CLOUD = true;

    //private static final InputHandler inputHandler = new InputHandler("industrial-equipment-maintenance-system.json", "src/com/extensions/tests/testing/industrialEquipmentMaintenanceSystem", true);
    //private static final InputHandler inputHandler = new InputHandler("remote-weather-station.json", "src/com/extensions/tests/testing/remoteWeatherStation", false);
    //private static final InputHandler inputHandler = new InputHandler("smart-city-traffic-management.json", "src/com/extensions/tests/testing/SmartCityTrafficManagement", true);
    //private static final InputHandler inputHandler = new InputHandler("smart-home-temperature-control.json", "src/com/extensions/tests/testing/smartHomeTemperatureControl", false);
    //private static final InputHandler inputHandler = new InputHandler("smart-agriculture-greenhouse-monitoring.json", "src/com/extensions/tests/testing/smartAgricultureGreenhouseMonitoring", false);
    private static final InputHandler inputHandler = new InputHandler("smart-hospital-management.json", "src/com/extensions/tests/testing/smartHospitalManagement", true);
    //private static final InputHandler inputHandler = new InputHandler("dcns-fog-application.json", "src/com/extensions/tests/testing/dcnsFog", true);

    public static void main(String[] args) {
        try {
            //////////////////////////////// INITIAL SETUP ////////////////////////////////

            // Parses the VD quantities file and extracts the quantity of each thing to be used in the application
            ThingQuantityParser vdQuantities = new ThingQuantityParser(new File(inputHandler.THINGS_QUANTITIES));

            // This instance is responsible for loading in the node red application, creating the application topology and model and setting up related data
            JsonToApplication jsonToApplication = new JsonToApplication(
                    CloudNodePreset.DEFAULT, // CHANGEABLE
                    EdgeNodePreset.DEFAULT, // CHANGEABLE
                    ApplicationPreset.DEFAULT, // CHANGEABLE
                    new File(inputHandler.NODE_RED_APPLICATION_JSON),
                    vdQuantities
            );

            Log.printLine("Starting Simulation...");

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
                    inputHandler.THINGS_REPO,
                    vdConfigParser.process(new File(inputHandler.VDS_CONFIG_FILE)),
                    vdQuantities.getThingFrequencies()
            );

            System.out.println("Virtual Devices Created Successfully!");

            //////////////////////////////// APPLICATION SETUP ////////////////////////////////

            // Create the physical topology for the application
            ApplicationPhysicalTopology physicalTopology = jsonToApplication.createApplicationPhysicalTopology(virtualDevices);

            // Set the sensors list (NEEDED TO CREATE THE APPLICATION MODEL BELOW)
            jsonToApplication.setAllSensors(physicalTopology.getSensors());

            // Create the application model for the application
            Application application = jsonToApplication.createApplicationModel(appId, broker.getId());
            //application.setUserId(broker.getId());

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
                //moduleMapping.addModuleToDevice("MasterModule", "cloud");
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

            //customMetricManager.registerMetric(new PeakEnergyConsumptionDevice());
            //customMetricManager.registerMetric(new TotalEnergyConsumptionEfficiency());
            customMetricManager.registerMetric(new MeanTupleExecutionDelay());

            //////////////////////////////// SIMULATION ////////////////////////////////

            // Set the simulation start time
            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            // Start the CloudSim simulation
            CloudSim.startSimulation();

            // Stop the simulation once it completes
            CloudSim.stopSimulation();

        } catch(Exception e) {
            System.out.println("Simulation Terminated! Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
