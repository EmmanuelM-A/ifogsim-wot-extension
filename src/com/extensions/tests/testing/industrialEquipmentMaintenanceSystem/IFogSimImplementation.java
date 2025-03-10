package com.extensions.tests.testing.industrialEquipmentMaintenanceSystem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;

import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

/**
 * Industrial Equipment Maintenance System Implementation for iFogSim
 *
 * This simulation creates a fog computing topology for an industrial maintenance system
 * with sensors, edge devices, fog nodes, and a cloud data center.
 */
public class IFogSimImplementation {

    private final static boolean CLOUD_PLACEMENT = false; // Set to true to place all modules in cloud

    public static void main(String[] args) {
        try {
            Log.disable();
            int num_user = 1; // Number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // Resource utilization tracking

            // Initialize CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            // Create broker
            FogBroker broker = new FogBroker("broker");

            // Create application
            Application application = createApplication("industrial-maintenance", broker.getId());
            application.setUserId(broker.getId());

            // Create physical topology
            List<FogDevice> fogDevices = createFogDevices(broker.getId(), application);

            // Create sensors and actuators
            List<Sensor> sensors = createSensors(broker.getId(), fogDevices, application);
            List<Actuator> actuators = createActuators(broker.getId(), fogDevices, application);

            // Module deployment
            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();

            if (CLOUD_PLACEMENT) {
                // Place all modules in the cloud
                for (FogDevice device : fogDevices) {
                    if (device.getName().equals("cloud")) {
                        moduleMapping.addModuleToDevice("dataAnalyzer", "cloud");
                        moduleMapping.addModuleToDevice("maintenancePredictor", "cloud");
                        moduleMapping.addModuleToDevice("alertGenerator", "cloud");
                    }
                }
            } else {
                // Place modules across edge, fog, and cloud
                for (FogDevice device : fogDevices) {
                    if (device.getName().equals("edge-node-1") || device.getName().equals("edge-node-2")) {
                        moduleMapping.addModuleToDevice("dataPreprocessor", device.getName());
                    } else if (device.getName().equals("fog-node-1")) {
                        moduleMapping.addModuleToDevice("dataAnalyzer", device.getName());
                        moduleMapping.addModuleToDevice("alertGenerator", device.getName());
                    } else if (device.getName().equals("cloud")) {
                        moduleMapping.addModuleToDevice("maintenancePredictor", device.getName());
                        moduleMapping.addModuleToDevice("dataStorage", device.getName());
                    }
                }
            }

            // Create controller
            Controller controller = new Controller("master-controller", fogDevices, sensors,
                    actuators);

            // Map modules to devices
            controller.submitApplication(application,
                    new ModulePlacementMapping(fogDevices, application, moduleMapping));

            // Start the simulation
            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
            CloudSim.startSimulation();
            CloudSim.stopSimulation();

            Log.print("Industrial Maintenance System simulation finished!");

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }

    /**
     * Creates the fog computing application with modules and data flows
     */
    private static Application createApplication(String appId, int userId) {

        Application application = Application.createApplication(appId, userId);

        // Add modules (processing elements of the application)
        application.addAppModule("dataPreprocessor", 10);
        application.addAppModule("dataAnalyzer", 50);
        application.addAppModule("maintenancePredictor", 100);
        application.addAppModule("alertGenerator", 30);
        application.addAppModule("dataStorage", 50);

        // Add edges (data flow between modules)
        // Sensor data -> Preprocessor
        application.addAppEdge("SENSOR_DATA", "dataPreprocessor", 1000, 500, "SENSOR_DATA",
                Tuple.UP, AppEdge.SENSOR);

        // Preprocessor -> Analyzer
        application.addAppEdge("dataPreprocessor", "dataAnalyzer", 1000, 200,
                "PREPROCESSED_DATA", Tuple.UP, AppEdge.MODULE);

        // Analyzer -> Predictor
        application.addAppEdge("dataAnalyzer", "maintenancePredictor", 1000, 100,
                "ANALYZED_DATA", Tuple.UP, AppEdge.MODULE);

        // Analyzer -> Storage
        application.addAppEdge("dataAnalyzer", "dataStorage", 1000, 200,
                "HISTORICAL_DATA", Tuple.UP, AppEdge.MODULE);

        // Predictor -> Alert Generator
        application.addAppEdge("maintenancePredictor", "alertGenerator", 1000, 100,
                "MAINTENANCE_PREDICTION", Tuple.DOWN, AppEdge.MODULE);

        // Alert Generator -> Actuator
        application.addAppEdge("alertGenerator", "MAINTENANCE_ALERT", 1000, 100,
                "MAINTENANCE_ALERT", Tuple.DOWN, AppEdge.ACTUATOR);

        // Define application loops for tracking delays
        final AppLoop loop1 = new AppLoop(new ArrayList<String>() {
            {
                add("SENSOR_DATA");
                add("dataPreprocessor");
                add("dataAnalyzer");
                add("maintenancePredictor");
                add("alertGenerator");
                add("MAINTENANCE_ALERT");
            }
        });

        List<AppLoop> loops = new ArrayList<AppLoop>() {
            {
                add(loop1);
            }
        };
        application.setLoops(loops);

        // Data selectivity: what percentage of input tuples are transformed to output tuples
        application.addTupleMapping("dataPreprocessor", "SENSOR_DATA", "PREPROCESSED_DATA",
                new FractionalSelectivity(0.9));
        application.addTupleMapping("dataAnalyzer", "PREPROCESSED_DATA", "ANALYZED_DATA",
                new FractionalSelectivity(0.8));
        application.addTupleMapping("dataAnalyzer", "PREPROCESSED_DATA", "HISTORICAL_DATA",
                new FractionalSelectivity(1.0));
        application.addTupleMapping("maintenancePredictor", "ANALYZED_DATA", "MAINTENANCE_PREDICTION",
                new FractionalSelectivity(0.7));
        application.addTupleMapping("alertGenerator", "MAINTENANCE_PREDICTION", "MAINTENANCE_ALERT",
                new FractionalSelectivity(0.5));

        return application;
    }

    /**
     * Creates sensors that generate data from industrial equipment
     */
    private static List<Sensor> createSensors(int userId, List<FogDevice> fogDevices,
                                              Application application) {
        List<Sensor> sensors = new ArrayList<Sensor>();

        // Find the edge devices where sensors will be attached
        FogDevice edgeDevice1 = null;
        FogDevice edgeDevice2 = null;
        for (FogDevice device : fogDevices) {
            if (device.getName().equals("edge-node-1"))
                edgeDevice1 = device;
            else if (device.getName().equals("edge-node-2"))
                edgeDevice2 = device;
        }

        // Create vibration sensors for each machine
        Sensor vibrationSensor1 = new Sensor("vibration-sensor-1", "SENSOR_DATA", userId,
                application.getAppId(),
                new DeterministicDistribution(5000)); // Emit data every 5 seconds
        vibrationSensor1.setGatewayDeviceId(edgeDevice1.getId());
        vibrationSensor1.setLatency(100.0); // Latency in transmitting data to gateway (in milliseconds)
        sensors.add(vibrationSensor1);

        Sensor vibrationSensor2 = new Sensor("vibration-sensor-2", "SENSOR_DATA", userId,
                application.getAppId(),
                new DeterministicDistribution(5000));
        vibrationSensor2.setGatewayDeviceId(edgeDevice1.getId());
        vibrationSensor2.setLatency(100.0);
        sensors.add(vibrationSensor2);

        // Create temperature sensors
        Sensor temperatureSensor1 = new Sensor("temperature-sensor-1", "SENSOR_DATA", userId,
                application.getAppId(),
                new DeterministicDistribution(10000)); // Every 10 seconds
        temperatureSensor1.setGatewayDeviceId(edgeDevice2.getId());
        temperatureSensor1.setLatency(120.0);
        sensors.add(temperatureSensor1);

        Sensor temperatureSensor2 = new Sensor("temperature-sensor-2", "SENSOR_DATA", userId,
                application.getAppId(),
                new DeterministicDistribution(10000));
        temperatureSensor2.setGatewayDeviceId(edgeDevice2.getId());
        temperatureSensor2.setLatency(120.0);
        sensors.add(temperatureSensor2);

        // Create pressure sensors
        Sensor pressureSensor = new Sensor("pressure-sensor", "SENSOR_DATA", userId,
                application.getAppId(),
                new DeterministicDistribution(15000)); // Every 15 seconds
        pressureSensor.setGatewayDeviceId(edgeDevice2.getId());
        pressureSensor.setLatency(150.0);
        sensors.add(pressureSensor);

        return sensors;
    }

    /**
     * Creates actuators that respond to maintenance alerts
     */
    private static List<Actuator> createActuators(int userId, List<FogDevice> fogDevices,
                                                  Application application) {
        List<Actuator> actuators = new ArrayList<Actuator>();

        // Find the fog node where actuators will be attached
        FogDevice fogNode = null;
        for (FogDevice device : fogDevices) {
            if (device.getName().equals("fog-node-1"))
                fogNode = device;
        }

        // Create maintenance alert actuator
        Actuator maintenanceAlert = new Actuator("maintenance-alert-actuator", userId,
                application.getAppId(), "MAINTENANCE_ALERT");
        maintenanceAlert.setGatewayDeviceId(fogNode.getId());
        maintenanceAlert.setLatency(150.0);
        actuators.add(maintenanceAlert);

        return actuators;
    }

    /**
     * Creates the fog computing devices (edge nodes, fog nodes, cloud)
     */
    private static List<FogDevice> createFogDevices(int userId, Application application) {
        List<FogDevice> devices = new ArrayList<FogDevice>();

        // Cloud data center specs
        FogDevice cloud = createFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25);
        cloud.setParentId(-1);
        devices.add(cloud);

        // Fog node specs
        FogDevice fogNode1 = createFogDevice("fog-node-1", 2800, 4000, 10000, 1000, 1, 0.0, 107.339, 83.4333);
        fogNode1.setParentId(cloud.getId());
        fogNode1.setUplinkLatency(100); // Latency of connection between fog and cloud
        devices.add(fogNode1);

        // Edge node specs
        FogDevice edgeNode1 = createFogDevice("edge-node-1", 1000, 1000, 10000, 270, 2, 0, 107.339, 83.4333);
        edgeNode1.setParentId(fogNode1.getId());
        edgeNode1.setUplinkLatency(20); // Latency of connection between edge and fog
        devices.add(edgeNode1);

        FogDevice edgeNode2 = createFogDevice("edge-node-2", 1000, 1000, 10000, 270, 2, 0, 107.339, 83.4333);
        edgeNode2.setParentId(fogNode1.getId());
        edgeNode2.setUplinkLatency(20);
        devices.add(edgeNode2);

        return devices;
    }

    /**
     * Helper method to create a fog device with specific characteristics
     */
    private static FogDevice createFogDevice(String nodeName, long mips, int ram, long upBw, long downBw,
                                             int level, double ratePerMips, double busyPower, double idlePower) {

        List<Pe> peList = new ArrayList<Pe>();
        peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // Processing elements

        int hostId = FogUtils.generateEntityId();
        long storage = 1000000; // 1 GB storage
        int bw = 10000; // 10 Gbps network

        PowerHost host = new PowerHost(
                hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerOverbooking(bw),
                storage,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(busyPower, idlePower)
        );

        List<Host> hostList = new ArrayList<Host>();
        hostList.add(host);

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double timez = 10.0; // Time zone
        double cost = 3.0; // Cost of using this resource
        double costPerMem = 0.05; // Cost per memory
        double costPerStorage = 0.001; // Cost per storage
        double costPerBw = 0.0; // Cost per bandwidth

        LinkedList<Storage> storageList = new LinkedList<Storage>();

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                arch, os, vmm, host, timez, cost, costPerMem, costPerStorage, costPerBw);

        FogDevice fogdevice = null;
        try {
            fogdevice = new FogDevice(nodeName, characteristics,
                    new AppModuleAllocationPolicy(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
        } catch (Exception e) {
            e.printStackTrace();
        }

        fogdevice.setLevel(level);
        return fogdevice;
    }
}