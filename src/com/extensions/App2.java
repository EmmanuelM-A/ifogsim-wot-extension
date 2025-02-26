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

public final class App2 {
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

            // This instance is responsible for loading in the node red application and setting up related data
            JsonToApplication2 jsonToApplication = new JsonToApplication2(
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
            List<VD> virtualDevices = VDFactory.createVirtualDevices(
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
            //PhysicalTopology physicalTopology = createPhysicalTopology(virtualDevices);

            // Create the application model for the application
            //Application application = createApplication(appId, broker.getId());
            Application application = jsonToApplication.createApplicationModel(appId, broker.getId());

            // Set the application for VD's sensors and actuators
            for (VD virtualDevice : virtualDevices) {
                virtualDevice.getSensor().setApp(application);
                virtualDevice.getActuator().setApp(application);
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

    private static PhysicalTopology createPhysicalTopology(List<VD> vds) {
        List<FogDevice> fogDevices = new ArrayList<>();
        List<Actuator> actuators = new ArrayList<>();
        List<Sensor> sensors = new ArrayList<>();

        // Cloud Device
        FogDevice cloud = FogDeviceFactory.createFogDevice("cloud", FogDevicePreset.DEFAULT);
        fogDevices.add(cloud);

        // Edge Node
        FogDevice edgeNode = FogDeviceFactory.createFogDevice("edge-node", FogDevicePreset.DEFAULT);

        if(edgeNode != null) {
            assert cloud != null;
            edgeNode.setParentId(cloud.getId());
            fogDevices.add(edgeNode);
        }

        // Connect Virtual Devices (VDs) to the Edge Node
        for (VD vd : vds) {
            FogDevice vdFogDevice = vd.getFogDevice();
            sensors.add(vd.getSensor());
            actuators.add(vd.getActuator());
            assert edgeNode != null;
            vdFogDevice.setParentId(edgeNode.getId());
            fogDevices.add(vdFogDevice);
        }

        PhysicalTopology physicalTopology = new PhysicalTopology();

        physicalTopology.setActuators(actuators);
        physicalTopology.setSensors(sensors);
        physicalTopology.setFogDevices(fogDevices);

        return physicalTopology;
    }

    private static Application createApplication(String appId, int brokerId) {
        Application application = Application.createApplication(appId, brokerId);

        String VD_SENSOR = "TemperatureSensor_SENSOR";
        String VD_ACTUATOR = "TemperatureSensor_ACTUATOR";

        String MM = "MasterModule";
        String WM1 = "WorkerModule-1";
        String WM2 = "WorkerModule-2";
        String WM3 = "WorkerModule-3";

        application.addAppModule(MM, 10);
        application.addAppModule(WM1, 10);
        application.addAppModule(WM2, 10);
        application.addAppModule(WM3, 10);

        application.addAppEdge(VD_SENSOR, MM, 3000, 500, VD_SENSOR, Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(MM, VD_ACTUATOR, 500, 4000, VD_ACTUATOR, Tuple.DOWN, AppEdge.ACTUATOR);

        application.addAppEdge(MM, WM1, 4034, 4590, "temperature", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge(WM1, MM, 300, 500, "updateDisplay", Tuple.DOWN, AppEdge.MODULE);

        application.addAppEdge(MM, WM2, 4034, 4590, "overheat", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge(WM2, MM, 300, 500, "showAlert", Tuple.DOWN, AppEdge.MODULE);

        application.addAppEdge(MM, WM3, 4034, 4590, "lowBattery", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge(WM3, MM, 300, 500, "showAlert", Tuple.DOWN, AppEdge.MODULE);

        // Tuple Mappings
        application.addTupleMapping(MM, VD_SENSOR, "temperature", new FractionalSelectivity(1.0));
        application.addTupleMapping(MM, VD_SENSOR, "overheat", new FractionalSelectivity(1.0));
        application.addTupleMapping(MM, VD_SENSOR, "lowBattery", new FractionalSelectivity(1.0));
        application.addTupleMapping(WM1, "temperature", "updateDisplay", new FractionalSelectivity(1.0));
        application.addTupleMapping(WM2, "overheat", "showAlert", new FractionalSelectivity(1.0));
        application.addTupleMapping(WM3, "lowBattery", "showAlert", new FractionalSelectivity(1.0));
        application.addTupleMapping(MM, "updateDisplay", VD_ACTUATOR, new FractionalSelectivity(1.0));
        application.addTupleMapping(MM, "showAlert", VD_ACTUATOR, new FractionalSelectivity(1.0));

        // Application Loops
        List<String> loop1 = Arrays.asList(VD_SENSOR, MM, WM1, MM, VD_ACTUATOR);
        List<String> loop2 = Arrays.asList(VD_SENSOR, MM, WM2, MM, VD_ACTUATOR);
        List<String> loop3 = Arrays.asList(VD_SENSOR, MM, WM3, MM, VD_ACTUATOR);

        application.getLoops().add(new AppLoop(loop1));
        application.getLoops().add(new AppLoop(loop2));
        application.getLoops().add(new AppLoop(loop3));

        return application;
    }
}
