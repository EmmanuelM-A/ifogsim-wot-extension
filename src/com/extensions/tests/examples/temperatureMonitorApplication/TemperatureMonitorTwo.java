package com.extensions.tests.examples.temperatureMonitorApplication;

import com.extensions.customfog.CustomController;
import com.extensions.customfog.FogDeviceFactory;
import com.extensions.sysconstructor.core.ThingQuantityParser;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.utils.presets.SensorPreset;
import com.extensions.utils.presets.ActuatorPreset;
import com.extensions.vdcreation.core.VirtualDevice;
import com.extensions.vdcreation.core.VirtualDeviceFactory;
import com.extensions.vdcreation.parsers.VirtualDeviceConfigParser;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.application.AppLoop;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.*;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;

import java.io.File;
import java.util.*;

public class TemperatureMonitorTwo {
    /**
     * Determines if the application is cloud-based
     */
    private static final boolean CLOUD = true;

    private static final String THINGS_REPO = "src/com/extensions/tests/examples/temperatureMonitorApplication/things";

    private static final String VDS_CONFIG_FILE = "src/com/extensions/tests/examples/temperatureMonitorApplication/configs/vd-configs.json";

    private static final String VD_QUANTITIES_FILE = "";

    public static void main(String[] args) {
        try {
            //////////////////////////////// INITIAL SETUP ////////////////////////////////

            // Parses the VD quantities file and extracts the quantity of each thing to be used in the application
            ThingQuantityParser vdQuantities = new ThingQuantityParser(new File(VD_QUANTITIES_FILE));

            // Disables iFogSim's logging mechanism, only display simulation results
            Log.disable();

            int numUsers = 1;

            Calendar calendar = Calendar.getInstance();

            CloudSim.init(numUsers, calendar, false);

            String appId = "Temperature-Monitor";

            // Create broker
            FogBroker broker = new FogBroker("broker");

            //////////////////////////////// VIRTUAL DEVICE CREATION ////////////////////////////////

            // Step 1: Create the virtual devices

            // The parser used to extract virtual device configurations if there are any
            VirtualDeviceConfigParser vdConfigParser = new VirtualDeviceConfigParser();

            // Create the virtual devices using the thing descriptions repo folder
            List<VirtualDevice> virtualDevices = VirtualDeviceFactory.createVirtualDevices(
                    broker.getId(),
                    appId,
                    FogDevicePreset.DEFAULT,
                    SensorPreset.DEFAULT,
                    ActuatorPreset.DEFAULT,
                    THINGS_REPO,
                    vdConfigParser.process(new File(VDS_CONFIG_FILE)),
                    vdQuantities.getThingFrequencies()
            );

            //////////////////////////////// APPLICATION SETUP ////////////////////////////////

            // Step 3: Create Temperature Monitoring Application
            Application application = createApplication(appId, broker.getId());
            application.setUserId(broker.getId());

            // Step 4: Create the Physical Topology
            List<FogDevice> fogDevices = createPhysicalTopology(virtualDevices);

            // Step 5: Set the application for VD's sensors and actuators
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

            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();

            if(CLOUD) {
                for(AppModule appModule : application.getModules()) {
                    moduleMapping.addModuleToDevice(appModule.getName(), "cloud");
                }
            }

            // Step 6: Create the controller for managing the simulation
            CustomController controller = new CustomController(
                    "master-controller", fogDevices, extractSensors(virtualDevices), extractActuators(virtualDevices)
            );

            // Step 7: Module Placement Strategy
            //moduleMapping.addModuleToDevice("processing", "edge-node");

            controller.submitApplication(
                    application,
                    0,
                    (CLOUD) ? (new ModulePlacementMapping(fogDevices, application, moduleMapping))
                            : (new ModulePlacementEdgewards(fogDevices, extractSensors(virtualDevices), extractActuators(virtualDevices), application, moduleMapping))
            );

            // Step 8: Start Simulation
            CloudSim.startSimulation();
            CloudSim.stopSimulation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the application model for temperature monitoring.
     */
    private static Application createApplication(String appId, int userId) {
        Application application = Application.createApplication(appId, userId);

        // Processing Module
        application.addAppModule("processing", 100);

        // Data Flow Edges
        application.addAppEdge("temperature", "processing", 5000, 2000, "temperature", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("processing", "updateDisplay", 1000, 2000, "updateDisplay", Tuple.DOWN, AppEdge.ACTUATOR);

        // Tuple Mappings
        application.addTupleMapping("processing", "temperature", "updateDisplay", new FractionalSelectivity(1.0));

        // Define Application Loops
        final AppLoop loop = new AppLoop(Arrays.asList("temperature", "processing", "updateDisplay"));

        application.setLoops(new ArrayList<>(){{add(loop);}});

        return application;
    }

    /**
     * Creates the physical topology with fog devices.
     */
    private static List<FogDevice> createPhysicalTopology(List<VirtualDevice> vds) {
        List<FogDevice> fogDevices = new ArrayList<>();

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
        for (VirtualDevice vd : vds) {
            FogDevice vdFogDevice = vd.getFogDevice();
            assert edgeNode != null;
            vdFogDevice.setParentId(edgeNode.getId());
            fogDevices.add(vdFogDevice);
        }

        return fogDevices;
    }

    /**
     * Extracts sensors from Virtual Devices.
     */
    private static List<Sensor> extractSensors(List<VirtualDevice> vds) {
        List<Sensor> sensors = new ArrayList<>();
        for (VirtualDevice vd : vds) {
            sensors.addAll(vd.getSensorProperties());
        }
        return sensors;
    }

    /**
     * Extracts actuators from Virtual Devices.
     */
    private static List<Actuator> extractActuators(List<VirtualDevice> vds) {
        List<Actuator> actuators = new ArrayList<>();
        for (VirtualDevice vd : vds) {
            actuators.addAll(vd.getActuatorActions());
        }
        return actuators;
    }
}

