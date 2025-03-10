package com.extensions.tests.testing.smartAgricultureGreenhouseMonitoring;

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
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

public class IFogSimImplementation {
    static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
    static List<Sensor> sensors = new ArrayList<Sensor>();
    static List<Actuator> actuators = new ArrayList<Actuator>();
    static int numOfEdgeNodes = 1;

    public static void main(String[] args) {
        try {
            Log.disable();
            // Initialize the CloudSim library
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            CloudSim.init(num_user, calendar, trace_flag);

            // Create broker
            FogBroker broker = new FogBroker("broker");

            // Create application
            Application application = createApplication("smart_agriculture", broker.getId());
            application.setUserId(broker.getId());

            // Create physical topology
            createFogDevices(broker.getId(), application.getAppId());

            // Create the controller
            Controller controller = new Controller("master-controller", fogDevices, sensors, actuators);

            // Map the application modules to devices
            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();

            // Place processing modules on EDGE layer
            for (FogDevice device : fogDevices) {
                if (device.getName().startsWith("edge-")) {
                    moduleMapping.addModuleToDevice("sensorProcessor", device.getName());
                }
            }

            // Place controller modules on CLOUD
            moduleMapping.addModuleToDevice("displayController", "cloud");
            moduleMapping.addModuleToDevice("moistureController", "cloud");
            moduleMapping.addModuleToDevice("ventilationController", "cloud");

            // Initialize the controller
            controller.submitApplication(application,
                    new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping));

            // Start the simulation
            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
            CloudSim.startSimulation();
            CloudSim.stopSimulation();

            Log.printLine("Smart Agriculture System simulation finished!");

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }

    /**
     * Creates the fog devices in the simulation.
     */
    private static void createFogDevices(int userId, String appId) {
        // Create CLOUD device (Tier 1)
        FogDevice cloud = createFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25);
        cloud.setParentId(-1);
        fogDevices.add(cloud);

        // Create EDGE NODES (Tier 2)
        for (int i = 0; i < numOfEdgeNodes; i++) {
            FogDevice edgeNode = createFogDevice("edge-" + i, 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333);
            edgeNode.setParentId(cloud.getId());
            edgeNode.setUplinkLatency(100); // latency of connection to cloud
            fogDevices.add(edgeNode);

            // Create END DEVICES (Tier 3)
            // Create temperature sensor device
            FogDevice tempDevice = createFogDevice("temperature-sensor-" + i, 500, 1000, 5000, 5000, 2, 0, 87.53, 82.44);
            tempDevice.setParentId(edgeNode.getId());
            tempDevice.setUplinkLatency(2);
            fogDevices.add(tempDevice);
            addSensor("temperature", "TEMP", tempDevice.getId(), userId, appId, 5);

            // Create humidity sensor device
            FogDevice humidityDevice = createFogDevice("humidity-sensor-" + i, 500, 1000, 5000, 5000, 2, 0, 87.53, 82.44);
            humidityDevice.setParentId(edgeNode.getId());
            humidityDevice.setUplinkLatency(2);
            fogDevices.add(humidityDevice);
            addSensor("humidity", "HUMIDITY", humidityDevice.getId(), userId, appId, 5);

            // Create soil moisture sensor device
            FogDevice soilMoistureDevice = createFogDevice("soil-moisture-sensor-" + i, 500, 1000, 5000, 5000, 2, 0, 87.53, 82.44);
            soilMoistureDevice.setParentId(edgeNode.getId());
            soilMoistureDevice.setUplinkLatency(2);
            fogDevices.add(soilMoistureDevice);
            addSensor("soilMoisture", "SOIL_MOISTURE", soilMoistureDevice.getId(), userId, appId, 5);

            // Create soil nutrition monitor device (includes N, P, K sensors)
            FogDevice soilNutritionDevice = createFogDevice("soil-nutrition-monitor-" + i, 500, 1000, 5000, 5000, 2, 0, 87.53, 82.44);
            soilNutritionDevice.setParentId(edgeNode.getId());
            soilNutritionDevice.setUplinkLatency(2);
            fogDevices.add(soilNutritionDevice);
            addSensor("nitrogen", "NITROGEN", soilNutritionDevice.getId(), userId, appId, 10);
            addSensor("phosphorus", "PHOSPHORUS", soilNutritionDevice.getId(), userId, appId, 10);
            addSensor("potassium", "POTASSIUM", soilNutritionDevice.getId(), userId, appId, 10);

            // Add actuator devices

            // Smart display actuator
            FogDevice displayDevice = createFogDevice("smart-display-" + i, 500, 1000, 5000, 5000, 2, 0, 87.53, 82.44);
            displayDevice.setParentId(edgeNode.getId());
            displayDevice.setUplinkLatency(2);
            fogDevices.add(displayDevice);
            addActuator("display-" + i, userId, appId, displayDevice.getId(), "DISPLAY");

            // Water pump actuator
            FogDevice pumpDevice = createFogDevice("water-pump-" + i, 500, 1000, 5000, 5000, 2, 0, 87.53, 82.44);
            pumpDevice.setParentId(edgeNode.getId());
            pumpDevice.setUplinkLatency(2);
            fogDevices.add(pumpDevice);
            addActuator("pump-" + i, userId, appId, pumpDevice.getId(), "WATER_PUMP");

            // Smart ventilation system actuator
            FogDevice ventilationDevice = createFogDevice("smart-ventilation-" + i, 500, 1000, 5000, 5000, 2, 0, 87.53, 82.44);
            ventilationDevice.setParentId(edgeNode.getId());
            ventilationDevice.setUplinkLatency(2);
            fogDevices.add(ventilationDevice);
            addActuator("ventilation-" + i, userId, appId, ventilationDevice.getId(), "SMART_VENT");
        }
    }

    /**
     * Creates a fog device
     */
    private static FogDevice createFogDevice(String name, long mips, int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower) {
        List<Pe> peList = new ArrayList<Pe>();
        peList.add(new Pe(0, new PeProvisionerOverbooking(mips)));

        int hostId = FogUtils.generateEntityId();
        long storage = 1000000;
        int bw = 10000;

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
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;
        LinkedList<Storage> storageList = new LinkedList<Storage>();

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                arch, os, vmm, host, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        FogDevice fogdevice = null;
        try {
            fogdevice = new FogDevice(name, characteristics,
                    new AppModuleAllocationPolicy(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
        } catch (Exception e) {
            e.printStackTrace();
        }

        fogdevice.setLevel(level);
        return fogdevice;
    }

    /**
     * Creates the application model
     */
    private static Application createApplication(String appId, int userId) {
        Application application = Application.createApplication(appId, userId);

        // Add modules
        application.addAppModule("sensorProcessor", 10);
        application.addAppModule("displayController", 10);
        application.addAppModule("moistureController", 10);
        application.addAppModule("ventilationController", 10);

        // Add edges (data flow)
        // Temperature edges
        application.addAppEdge("TEMP", "sensorProcessor", 1000, 500, "TEMP", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("sensorProcessor", "displayController", 1000, 500, "TEMP_PROCESSED", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("displayController", "DISPLAY_ACTUATOR", 1000, 500, "DISPLAY_TEMP", Tuple.DOWN, AppEdge.ACTUATOR);

        // Humidity edges
        application.addAppEdge("HUMIDITY", "sensorProcessor", 1000, 500, "HUMIDITY", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("sensorProcessor", "displayController", 1000, 500, "HUMIDITY_PROCESSED", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("displayController", "DISPLAY_ACTUATOR", 1000, 500, "DISPLAY_HUMIDITY", Tuple.DOWN, AppEdge.ACTUATOR);
        application.addAppEdge("sensorProcessor", "ventilationController", 1000, 500, "HUMIDITY_FOR_VENTILATION", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("ventilationController", "VENTILATION_ACTUATOR", 1000, 500, "CONTROL_VENTILATION", Tuple.DOWN, AppEdge.ACTUATOR);

        // Soil moisture edges
        application.addAppEdge("SOIL_MOISTURE", "sensorProcessor", 1000, 500, "SOIL_MOISTURE", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("sensorProcessor", "displayController", 1000, 500, "MOISTURE_PROCESSED", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("displayController", "DISPLAY_ACTUATOR", 1000, 500, "DISPLAY_MOISTURE", Tuple.DOWN, AppEdge.ACTUATOR);
        application.addAppEdge("sensorProcessor", "moistureController", 1000, 500, "MOISTURE_FOR_PUMP", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("moistureController", "PUMP_ACTUATOR", 1000, 500, "CONTROL_PUMP", Tuple.DOWN, AppEdge.ACTUATOR);

        // Soil nutrition edges (N, P, K)
        application.addAppEdge("NITROGEN", "sensorProcessor", 1000, 500, "NITROGEN", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("PHOSPHORUS", "sensorProcessor", 1000, 500, "PHOSPHORUS", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("POTASSIUM", "sensorProcessor", 1000, 500, "POTASSIUM", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("sensorProcessor", "displayController", 1000, 500, "NPK_PROCESSED", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("displayController", "DISPLAY_ACTUATOR", 1000, 500, "DISPLAY_NPK", Tuple.DOWN, AppEdge.ACTUATOR);

        // Add tuples' fraction of transmission
        application.addTupleMapping("sensorProcessor", "TEMP", "TEMP_PROCESSED", new FractionalSelectivity(1.0));
        application.addTupleMapping("sensorProcessor", "HUMIDITY", "HUMIDITY_PROCESSED", new FractionalSelectivity(1.0));
        application.addTupleMapping("sensorProcessor", "HUMIDITY", "HUMIDITY_FOR_VENTILATION", new FractionalSelectivity(1.0));
        application.addTupleMapping("sensorProcessor", "SOIL_MOISTURE", "MOISTURE_PROCESSED", new FractionalSelectivity(1.0));
        application.addTupleMapping("sensorProcessor", "SOIL_MOISTURE", "MOISTURE_FOR_PUMP", new FractionalSelectivity(1.0));
        application.addTupleMapping("sensorProcessor", "NITROGEN", "NPK_PROCESSED", new FractionalSelectivity(0.3));
        application.addTupleMapping("sensorProcessor", "PHOSPHORUS", "NPK_PROCESSED", new FractionalSelectivity(0.3));
        application.addTupleMapping("sensorProcessor", "POTASSIUM", "NPK_PROCESSED", new FractionalSelectivity(0.3));

        application.addTupleMapping("displayController", "TEMP_PROCESSED", "DISPLAY_TEMP", new FractionalSelectivity(1.0));
        application.addTupleMapping("displayController", "HUMIDITY_PROCESSED", "DISPLAY_HUMIDITY", new FractionalSelectivity(1.0));
        application.addTupleMapping("displayController", "MOISTURE_PROCESSED", "DISPLAY_MOISTURE", new FractionalSelectivity(1.0));
        application.addTupleMapping("displayController", "NPK_PROCESSED", "DISPLAY_NPK", new FractionalSelectivity(1.0));

        application.addTupleMapping("moistureController", "MOISTURE_FOR_PUMP", "CONTROL_PUMP", new FractionalSelectivity(1.0));
        application.addTupleMapping("ventilationController", "HUMIDITY_FOR_VENTILATION", "CONTROL_VENTILATION", new FractionalSelectivity(1.0));

        // Define application loops to monitor the delays - FIXED SECTION
        List<AppLoop> loops = new ArrayList<AppLoop>();

        // Temperature display loop
        List<String> temperatureDisplayLoop = new ArrayList<String>();
        temperatureDisplayLoop.add("TEMP");
        temperatureDisplayLoop.add("sensorProcessor");
        temperatureDisplayLoop.add("displayController");
        temperatureDisplayLoop.add("DISPLAY_ACTUATOR");
        loops.add(new AppLoop(temperatureDisplayLoop));

        // Humidity ventilation control loop
        List<String> humidityVentilationLoop = new ArrayList<String>();
        humidityVentilationLoop.add("HUMIDITY");
        humidityVentilationLoop.add("sensorProcessor");
        humidityVentilationLoop.add("ventilationController");
        humidityVentilationLoop.add("VENTILATION_ACTUATOR");
        loops.add(new AppLoop(humidityVentilationLoop));

        // Moisture pump control loop
        List<String> moisturePumpLoop = new ArrayList<String>();
        moisturePumpLoop.add("SOIL_MOISTURE");
        moisturePumpLoop.add("sensorProcessor");
        moisturePumpLoop.add("moistureController");
        moisturePumpLoop.add("PUMP_ACTUATOR");
        loops.add(new AppLoop(moisturePumpLoop));

        // Set the application loops
        application.setLoops(loops);

        return application;
    }

    /**
     * Adds a sensor to the physical topology
     */
    private static void addSensor(String name, String sensorType, int deviceId, int userId, String appId, int transmitDistribution) {
        Sensor sensor = new Sensor(name, sensorType, userId, appId, new DeterministicDistribution(transmitDistribution));
        sensor.setGatewayDeviceId(deviceId);
        sensor.setLatency(1.0);
        sensors.add(sensor);
    }

    /**
     * Adds an actuator to the physical topology
     */
    private static void addActuator(String name, int userId, String appId, int deviceId, String actuatorType) {
        Actuator actuator = new Actuator(name, userId, appId, actuatorType);
        actuator.setGatewayDeviceId(deviceId);
        actuator.setLatency(1.0);
        actuators.add(actuator);
    }
}
