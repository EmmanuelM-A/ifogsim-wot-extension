package com.extensions.tests.testing.smartHomeTemperatureControl;

import com.extensions.tests.helper.Helper;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.*;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class IFogSimImplementation {

    private static final List<FogDevice> fogDevices = new ArrayList<>();
    private static final List<Sensor> sensors = new ArrayList<>();
    private static final List<Actuator> actuators = new ArrayList<>();

    private static final String appId = "SimpleTemperatureControl";

    private static final String TEMPERATURE_PROCESSING_MODULE = "temperatureProcessingModule";
    private static final String THERMOSTAT_MODULE = "thermostatModule";

    private static final boolean CLOUD = true;

    public static void main(String[] args) {
        Log.printLine("Starting " + appId + "...");
        try {
            Log.disable();
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;

            CloudSim.init(num_user, calendar, trace_flag);

            FogBroker broker = new FogBroker("broker");

            Application application = createApplication(appId, broker.getId());
            application.setUserId(broker.getId());

            createFogDevices(appId, broker.getId());

            for (Sensor sensor : sensors) {
                sensor.setApp(application);
            }
            for (Actuator actuator : actuators) {
                actuator.setApp(application);
            }

            Controller controller = new Controller("controller", fogDevices, sensors, actuators);


            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();
            for (FogDevice device : fogDevices) {
                if (device.getName().equals("home-gateway")) {
                    moduleMapping.addModuleToDevice(TEMPERATURE_PROCESSING_MODULE, device.getName());
                    moduleMapping.addModuleToDevice(THERMOSTAT_MODULE, device.getName());
                }
            }

            if (CLOUD) {
                for (AppModule appModule : application.getModules()) {
                    moduleMapping.addModuleToDevice(appModule.getName(), "cloud");
                }
            }

            // Submit the application to the controller with the appropriate placement strategy
            controller.submitApplication(
                    application,
                    0,
                    (CLOUD) ? (new ModulePlacementMapping(fogDevices, application, moduleMapping))
                            : (new ModulePlacementEdgewards(
                            fogDevices,
                            sensors,
                            actuators,
                            application,
                            moduleMapping
                    ))
            );

            // Set the simulation start time
            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            CloudSim.startSimulation();
            CloudSim.stopSimulation();

            Log.printLine(appId + " finished!");

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happened");
        }
    }

    private static void createFogDevices(String appId, int userId) throws Exception {
        // Cloud node
        FogDevice cloud = Helper.createFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16 * 103, 16 * 83.25);
        cloud.setParentId(-1);
        fogDevices.add(cloud);

        // Edge node
        FogDevice homeGateway = Helper.createFogDevice("home-gateway", 2000, 4000, 10000, 10000, 1, 0.0, 107.53, 83.44);
        homeGateway.setParentId(cloud.getId());
        fogDevices.add(homeGateway);

        // Temperature Sensor End Device
        FogDevice temperatureSensor = createTemperatureSensor("temperature", appId, userId);
        temperatureSensor.setParentId(homeGateway.getId());
        fogDevices.add(temperatureSensor);

        // Thermostat End Device
        FogDevice thermostat = createThermostat("thermostat", appId, userId);
        thermostat.setParentId(homeGateway.getId());
        fogDevices.add(thermostat);
    }

    private static FogDevice createTemperatureSensor(String name, String appId, int userId) {
        FogDevice device = Helper.createFogDevice(name, 300, 1024, 100, 100, 3, 0.2, 50.5, 20.0);

        // Create sensor
        Sensor sensor = new Sensor(name, name, userId, appId, new DeterministicDistribution(100));
        sensor.setGatewayDeviceId(device.getId());
        sensor.setLatency(1.0);
        sensors.add(sensor);

        return device;
    }

    private static FogDevice createThermostat(String name, String appId, int userId) {
        FogDevice device = Helper.createFogDevice(name, 200, 1024, 300, 200, 3, 0.1, 25, 10.0);

        // Create actuator
        Actuator actuator = new Actuator(name, userId, appId, name);
        actuator.setGatewayDeviceId(device.getId());
        actuator.setLatency(1.0);
        actuators.add(actuator);

        return device;
    }

    private static Application createApplication(String appId, int userId) {
        Application application = Application.createApplication(appId, userId);

// Define Master Module
        application.addAppModule("MASTER_MODULE", 20); // Higher computation power

// Define Worker Modules
        application.addAppModule("WORKER_MODULE_1", 10);
        application.addAppModule("WORKER_MODULE_2", 10);

// Define Thermostat Module
        application.addAppModule(THERMOSTAT_MODULE, 10);

// Sensor Data to Master
        application.addAppEdge("temperature", "MASTER_MODULE", 900, 2100, "temperature", Tuple.UP, AppEdge.SENSOR);

// Master to Worker Modules (Splitting Work)
        application.addAppEdge("MASTER_MODULE", "WORKER_MODULE_1", 1000, 900, "temperature_task_1", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("MASTER_MODULE", "WORKER_MODULE_2", 1000, 900, "temperature_task_2", Tuple.UP, AppEdge.MODULE);

// Worker Modules Process and Return Data to Master
        application.addAppEdge("WORKER_MODULE_1", "MASTER_MODULE", 1000, 900, "processed_data_1", Tuple.DOWN, AppEdge.MODULE);
        application.addAppEdge("WORKER_MODULE_2", "MASTER_MODULE", 1000, 900, "processed_data_2", Tuple.DOWN, AppEdge.MODULE);

// Master Aggregates and Sends Final Data to Thermostat
        application.addAppEdge("MASTER_MODULE", THERMOSTAT_MODULE, 1000, 900, "final_processed_data", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge(THERMOSTAT_MODULE, "thermostat", 500, 100, "thermostat", Tuple.DOWN, AppEdge.ACTUATOR);

// Tuple Mappings (Master Sends Tasks, Workers Process, Master Collects)
        application.addTupleMapping("MASTER_MODULE", "temperature", "temperature_task_1", new FractionalSelectivity(0.5));
        application.addTupleMapping("MASTER_MODULE", "temperature", "temperature_task_2", new FractionalSelectivity(0.5));

        application.addTupleMapping("WORKER_MODULE_1", "temperature_task_1", "processed_data_1", new FractionalSelectivity(1.0));
        application.addTupleMapping("WORKER_MODULE_2", "temperature_task_2", "processed_data_2", new FractionalSelectivity(1.0));

        application.addTupleMapping("MASTER_MODULE", "processed_data_1", "final_processed_data", new FractionalSelectivity(1.0));
        application.addTupleMapping("MASTER_MODULE", "processed_data_2", "final_processed_data", new FractionalSelectivity(1.0));
        application.addTupleMapping(THERMOSTAT_MODULE, "final_processed_data", "thermostat", new FractionalSelectivity(1.0));

// Define Execution Loops
        final AppLoop masterWorkerLoop = new AppLoop(new ArrayList<String>() {{
            add("temperature");
            add("MASTER_MODULE");
            add("WORKER_MODULE_1");
            add("WORKER_MODULE_2");
            add("MASTER_MODULE");
            add(THERMOSTAT_MODULE);
            add("thermostat");
        }});

        List<AppLoop> loops = new ArrayList<AppLoop>() {{
            add(masterWorkerLoop);
        }};

        application.setLoops(loops);


        return application;
    }
}