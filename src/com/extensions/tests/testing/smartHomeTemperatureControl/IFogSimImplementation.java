package com.extensions.tests.testing.smartHomeTemperatureControl;

import com.extensions.tests.testing.helper.Helper;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.*;
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

    private static final boolean CLOUD = false;

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

            CloudSim.startSimulation();
            CloudSim.stopSimulation();

            Log.printLine(appId + " finished!");

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happened");
        }
    }

    private static void createFogDevices(String appId, int userId) throws Exception {
        FogDevice cloud = Helper.createFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16 * 103, 16 * 83.25);
        FogDevice homeGateway = Helper.createFogDevice("home-gateway", 2000, 4000, 10000, 10000, 1, 0.0, 107.53, 83.44);

        cloud.setParentId(-1);
        homeGateway.setParentId(cloud.getId());

        fogDevices.add(cloud);
        fogDevices.add(homeGateway);

        Sensor temperatureSensor = new Sensor("TEMPERATURE", "TEMPERATURE", userId, appId, new DeterministicDistribution(5000));
        temperatureSensor.setGatewayDeviceId(homeGateway.getId());
        temperatureSensor.setLatency(1.0);
        sensors.add(temperatureSensor);

        Actuator thermostatActuator = new Actuator("THERMOSTAT", userId, appId, "THERMOSTAT");
        thermostatActuator.setGatewayDeviceId(homeGateway.getId());
        thermostatActuator.setLatency(1.0);
        actuators.add(thermostatActuator);
    }

    private static Application createApplication(String appId, int userId) {
        Application application = Application.createApplication(appId, userId);

        application.addAppModule(TEMPERATURE_PROCESSING_MODULE, 10);
        application.addAppModule(THERMOSTAT_MODULE, 10);

        application.addAppEdge("TEMPERATURE", TEMPERATURE_PROCESSING_MODULE, 1000, 100, "TEMPERATURE", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(TEMPERATURE_PROCESSING_MODULE, THERMOSTAT_MODULE, 1000, 100, "THERMOSTAT", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge(THERMOSTAT_MODULE, "THERMOSTAT", 1000, 100, "THERMOSTAT", Tuple.DOWN, AppEdge.ACTUATOR);

        application.addTupleMapping(TEMPERATURE_PROCESSING_MODULE, "TEMPERATURE", "THERMOSTAT", new FractionalSelectivity(1.0));
        application.addTupleMapping(THERMOSTAT_MODULE, "THERMOSTAT", "THERMOSTAT", new FractionalSelectivity(1.0));

        final AppLoop mainControlLoop = new AppLoop(new ArrayList<String>() {{
            add("TEMPERATURE");
            add(TEMPERATURE_PROCESSING_MODULE);
            add(THERMOSTAT_MODULE);
            add("THERMOSTAT");
        }});

        List<AppLoop> loops = new ArrayList<AppLoop>() {{
            add(mainControlLoop);
        }};

        application.setLoops(loops);

        return application;
    }
}