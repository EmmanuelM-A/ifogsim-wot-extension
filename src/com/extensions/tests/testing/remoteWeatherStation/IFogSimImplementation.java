package com.extensions.tests.testing.remoteWeatherStation;

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
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

/**
 * Simulation setup for Remote Weather Application
 *
 */
public class IFogSimImplementation {
    static List<FogDevice> fogDevices = new ArrayList<>();
    static List<Sensor> sensors = new ArrayList<>();
    static List<Actuator> actuators = new ArrayList<>();
    static int numOfAreas = 1;

    private final static boolean CLOUD = true;

    public static void main(String[] args) {

        Log.printLine("Starting Remote Weather Application...");

        try {
            Log.disable();

            int num_user = 1;

            CloudSim.init(num_user, Calendar.getInstance(), false);

            String appId = "remote_weather_station_application"; // identifier of the application

            FogBroker broker = new FogBroker("broker");

            Application application = createApplication(appId, broker.getId());

            createFogDevices(broker.getId(), appId);

            Controller controller = null;

            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping

            // Place data collection modules on edge devices
            for(FogDevice device : fogDevices) {
                if(device.getName().startsWith("edge")) {
                    moduleMapping.addModuleToDevice("data_collector", device.getName());
                }
            }

            // Place data processor on edge devices as well since there's no gateway in createFogDevices
            for(FogDevice device : fogDevices) {
                if(device.getName().startsWith("edge")) {
                    moduleMapping.addModuleToDevice("data_processor", device.getName());
                }
            }

            // Place display controller on edge devices to minimize latency to display actuators
            for(FogDevice device : fogDevices) {
                if(device.getName().startsWith("edge")) {
                    moduleMapping.addModuleToDevice("display_controller", device.getName());
                }
            }

            // Place weather analyzer in the cloud as it's more computationally intensive
            moduleMapping.addModuleToDevice("weather_analyzer", "cloud");

            if(CLOUD) {
                // if the mode of deployment is cloud-based, override previous mappings
                moduleMapping.addModuleToDevice("data_collector", "cloud");
                moduleMapping.addModuleToDevice("data_processor", "cloud");
                moduleMapping.addModuleToDevice("display_controller", "cloud");
                moduleMapping.addModuleToDevice("weather_analyzer", "cloud");
            }

            controller = new Controller("master-controller", fogDevices, sensors, actuators);

            controller.submitApplication(application,
                    (CLOUD)?(new ModulePlacementMapping(fogDevices, application, moduleMapping))
                            :(new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping)));

            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            CloudSim.startSimulation();

            CloudSim.stopSimulation();

            Log.printLine("Weather Application finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }

    /**
     * Creates the fog devices in the physical topology of the simulation.
     * @param userId
     * @param appId
     */
    private static void createFogDevices(int userId, String appId) {
        FogDevice cloud = createFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25);
        cloud.setParentId(-1);
        fogDevices.add(cloud);


        for(int i=0; i<numOfAreas; i++) {
            addEdgeAndEndDevices(i+"", userId, appId, cloud.getId());
        }
    }

    private static void addEdgeAndEndDevices(String id, int userId, String appId, int parentId) {
        // Add edge device
        FogDevice edge = createFogDevice("edge-"+id, 1000, 2000, 10000, 10000, 2, 0.0, 87.53, 82.44);
        fogDevices.add(edge);
        edge.setUplinkLatency(2); // latency of connection between edge and gateway is 2 ms
        edge.setParentId(parentId);

        // Add temperature sensor
        Sensor tempSensor = new Sensor("temp-sensor-"+id, "TEMPERATURE", userId, appId, new DeterministicDistribution(5));
        sensors.add(tempSensor);
        tempSensor.setGatewayDeviceId(edge.getId());
        tempSensor.setLatency(1.0);  // latency of connection between sensor and edge device

        // Add humidity sensor
        Sensor humiditySensor = new Sensor("humidity-sensor-"+id, "HUMIDITY", userId, appId, new DeterministicDistribution(5));
        sensors.add(humiditySensor);
        humiditySensor.setGatewayDeviceId(edge.getId());
        humiditySensor.setLatency(1.0);

        // Add air quality sensor
        Sensor airQualitySensor = new Sensor("air-quality-sensor-"+id, "AIR_QUALITY", userId, appId, new DeterministicDistribution(5));
        sensors.add(airQualitySensor);
        airQualitySensor.setGatewayDeviceId(edge.getId());
        airQualitySensor.setLatency(1.0);

        // Add smart display as actuator
        Actuator display = new Actuator("display-"+id, userId, appId, "DISPLAY");
        actuators.add(display);
        display.setGatewayDeviceId(edge.getId());
        display.setLatency(1.0);
    }

    private static FogDevice createFogDevice(String nodeName, long mips,
                                             int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower) {

        List<Pe> peList = new ArrayList<Pe>();

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

        int hostId = FogUtils.generateEntityId();
        long storage = 1000000; // host storage
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

        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN devices by now

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                arch, os, vmm, host, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

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

    /**
     * Function to create the Remote Weather application in the DDF model.
     * @param appId unique identifier of the application
     * @param userId identifier of the user of the application
     * @return
     */
    @SuppressWarnings({"serial" })
    private static Application createApplication(String appId, int userId) {

        Application application = Application.createApplication(appId, userId);

        /*
         * Adding modules (vertices) to the application model (directed graph)
         */
        application.addAppModule("data_collector", 10);
        application.addAppModule("data_processor", 50);
        application.addAppModule("weather_analyzer", 80);
        application.addAppModule("display_controller", 10);

        /*
         * Connecting the application modules (vertices) in the application model (directed graph) with edges
         */
        application.addAppEdge("TEMPERATURE", "data_collector", 1000, 500, "TEMPERATURE", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("HUMIDITY", "data_collector", 1000, 500, "HUMIDITY", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("AIR_QUALITY", "data_collector", 1000, 500, "AIR_QUALITY", Tuple.UP, AppEdge.SENSOR);

        application.addAppEdge("data_collector", "data_processor", 2000, 1000, "SENSOR_DATA", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("data_processor", "weather_analyzer", 1000, 1000, "PROCESSED_DATA", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("weather_analyzer", "display_controller", 1000, 1000, "ANALYZED_DATA", Tuple.DOWN, AppEdge.MODULE);
        application.addAppEdge("display_controller", "DISPLAY", 100, 50, 1000, "DISPLAY_DATA", Tuple.DOWN, AppEdge.ACTUATOR);

        /*
         * Defining the input-output relationships (represented by selectivity) of the application modules.
         */
        application.addTupleMapping("data_collector", "TEMPERATURE", "SENSOR_DATA", new FractionalSelectivity(1.0));
        application.addTupleMapping("data_collector", "HUMIDITY", "SENSOR_DATA", new FractionalSelectivity(1.0));
        application.addTupleMapping("data_collector", "AIR_QUALITY", "SENSOR_DATA", new FractionalSelectivity(1.0));

        application.addTupleMapping("data_processor", "SENSOR_DATA", "PROCESSED_DATA", new FractionalSelectivity(1.0));
        application.addTupleMapping("weather_analyzer", "PROCESSED_DATA", "ANALYZED_DATA", new FractionalSelectivity(1.0));
        application.addTupleMapping("display_controller", "ANALYZED_DATA", "DISPLAY_DATA", new FractionalSelectivity(1.0));

        /*
         * Defining application loops to monitor the latency of.
         */
        final AppLoop loop1 = new AppLoop(new ArrayList<String>(){{
            add("data_collector");
            add("data_processor");
            add("weather_analyzer");
            add("display_controller");
            add("DISPLAY");
        }});

        List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);}};

        application.setLoops(loops);
        return application;
    }
}