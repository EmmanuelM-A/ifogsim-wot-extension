package com.extensions.tests;

import com.extensions.customfog.CustomController;
import com.extensions.customfog.FogDeviceFactory;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.utils.presets.SensorPreset;
import com.extensions.utils.presets.ActuatorPreset;
import com.extensions.vdcreation.core.VirtualDevice;
import com.extensions.vdcreation.core.VirtualDeviceFactory;
import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.parsers.ThingDescriptionParser;
import com.extensions.vdcreation.parsers.VirtualDeviceConfigParser;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.Application;
import org.fog.application.AppLoop;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.*;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;

import java.io.File;
import java.util.*;

public class TemperatureMonitorTwo {

    public static void main(String[] args) {
        try {
            Log.disable(); // Enable logging

            int numUsers = 1;
            Calendar calendar = Calendar.getInstance();
            CloudSim.init(numUsers, calendar, false);

            String appId = "temperature-monitor";

            // Create broker
            FogBroker broker = new FogBroker("broker");

            // Step 1: Extract TD Metadata
            ThingDescriptionParser tdParser = new ThingDescriptionParser();
            ThingDescription tempSensorThing = tdParser.process(new File("src/com/extensions/input/things/temperature-sensor.json"));
            ThingDescription smartDisplayThing = tdParser.process(new File("src/com/extensions/input/things/smart-display.json"));

            List<ThingDescription> tds = Arrays.asList(tempSensorThing, smartDisplayThing);

            // Step 2: Create Virtual Devices from TDs
            VirtualDeviceFactory vdFactory = new VirtualDeviceFactory(
                    broker.getId(), appId, FogDevicePreset.DEFAULT, SensorPreset.DEFAULT, ActuatorPreset.DEFAULT
            );

            List<VirtualDevice> vds = new ArrayList<>();
            for (ThingDescription td : tds) {
                VirtualDevice vd = vdFactory.createVirtualDevice(
                        td, new VirtualDeviceConfigParser().process(new File("src/com/extensions/input/configs/vd-configs.json"))
                );
                vds.add(vd);
            }

            // Step 3: Create Temperature Monitoring Application
            Application application = createApplication(appId, broker.getId());
            application.setUserId(broker.getId());

            // Step 4: Create the Physical Topology
            List<FogDevice> fogDevices = createPhysicalTopology(vds);

            // Step 5: Set the application for VD's sensors and actuators
            for (VirtualDevice virtualDevice : vds) {
                for (Sensor sensor : virtualDevice.getSensorProperties()) {
                    sensor.setApp(application);
                    //System.out.println(sensor.toString());
                }
                for (Actuator actuator : virtualDevice.getActuatorActions()) {
                    actuator.setApp(application);
                    //System.out.println(actuator.toString());
                }
            }

            // Step 6: Create the controller for managing the simulation
            CustomController controller = new CustomController(
                    "master-controller", fogDevices, extractSensors(vds), extractActuators(vds)
            );

            // Step 7: Module Placement Strategy
            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();
            moduleMapping.addModuleToDevice("processing", "edge-node");

            controller.submitApplication(
                    application, 0, new ModulePlacementEdgewards(
                            fogDevices, extractSensors(vds), extractActuators(vds), application, moduleMapping
                    )
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
        System.out.println("LOOP ID at creation = " + loop.getLoopId());
        application.setLoops(Collections.singletonList(loop));

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

