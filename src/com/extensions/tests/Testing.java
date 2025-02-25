package com.extensions.tests;

import com.extensions.customfog.FogDeviceFactory;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.*;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.utils.Config;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.NetworkUsageMonitor;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.utils.distribution.DeterministicDistribution;

import java.util.*;

public class Testing {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Fog Simulation...");

        Log.disable();

        // Step 1: Initialize CloudSim
        int numUser = 1;
        Calendar calendar = Calendar.getInstance();
        boolean traceFlag = false;
        CloudSim.init(numUser, calendar, traceFlag);

        String appId = "TestApp";

        // Step 2: Create Fog Broker
        FogBroker broker = new FogBroker("broker");

        // Step 3: Create Fog Devices (Hierarchical: Sensor -> FogDevice -> Proxy -> Cloud)
        FogDevice cloud = FogDeviceFactory.createFogDevice("cloud", 10000, 40000, 10000, 10000, 1, 0.1, 16 * 103, 16 * 83.25);
        FogDevice proxy = FogDeviceFactory.createFogDevice("proxy", 4000, 8000, 10000, 270, 1, 0.1, 107.339, 83.4333);
        FogDevice edgeDevice = FogDeviceFactory.createFogDevice("fog-device", 2000, 4000, 10000, 107, 1, 0.1, 107.339, 83.4333);

        cloud.setParentId(-1); // Cloud is the top-level device
        proxy.setParentId(cloud.getId());
        edgeDevice.setParentId(proxy.getId());

        List<FogDevice> fogDevices = new ArrayList<>(Arrays.asList(cloud, proxy, edgeDevice));

        // Step 4: Create Sensors & Actuators
        Sensor sensor = new Sensor("Sensor", "Sensor", broker.getId(), appId, new DeterministicDistribution(5));
        Actuator actuator = new Actuator("Actuator", broker.getId(), appId, "Actuator");

        // Step 5: Create the Application
        Application application = createApplication(appId, broker.getId());

        sensor.setApp(application);
        actuator.setApp(application);

        List<Sensor> sensors = new ArrayList<>(Collections.singletonList(sensor));
        List<Actuator> actuators = new ArrayList<>(Collections.singletonList(actuator));

        // Step 6: Set Up Controller and Module Placement
        Controller controller = new Controller("controller", fogDevices, sensors, actuators);
        controller.submitApplication(application, 0, new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, ModuleMapping.createModuleMapping()));

        // Step 7: Start Simulation
        CloudSim.startSimulation();
        CloudSim.stopSimulation();
    }

    // Your provided application model setup
    private static Application createApplication(String appId, int brokerId) {
        Application application = Application.createApplication(appId, brokerId);
        application.addAppModule("MasterModule", 10);
        application.addAppModule("WorkerModule-1", 10);
        application.addAppModule("WorkerModule-2", 10);
        application.addAppModule("WorkerModule-3", 10);

        application.addAppEdge("Sensor", "MasterModule", 3000, 500, "Sensor", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("MasterModule", "WorkerModule-1", 100, 1000, "Task-1", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("MasterModule", "WorkerModule-2", 100, 1000, "Task-2", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("MasterModule", "WorkerModule-3", 100, 1000, "Task-3", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("WorkerModule-1", "MasterModule", 20, 50, "Response-1", Tuple.DOWN, AppEdge.MODULE);
        application.addAppEdge("WorkerModule-2", "MasterModule", 20, 50, "Response-2", Tuple.DOWN, AppEdge.MODULE);
        application.addAppEdge("WorkerModule-3", "MasterModule", 20, 50, "Response-3", Tuple.DOWN, AppEdge.MODULE);
        application.addAppEdge("MasterModule", "Actuator", 100, 50, "OutputData", Tuple.DOWN, AppEdge.ACTUATOR);

        application.addTupleMapping("MasterModule", "Sensor", "Task-1", new FractionalSelectivity(0.3));
        application.addTupleMapping("MasterModule", "Sensor", "Task-2", new FractionalSelectivity(0.3));
        application.addTupleMapping("MasterModule", "Sensor", "Task-3", new FractionalSelectivity(0.3));
        application.addTupleMapping("WorkerModule-1", "Task-1", "Response-1", new FractionalSelectivity(1.0));
        application.addTupleMapping("WorkerModule-2", "Task-2", "Response-2", new FractionalSelectivity(1.0));
        application.addTupleMapping("WorkerModule-3", "Task-3", "Response-3", new FractionalSelectivity(1.0));
        application.addTupleMapping("MasterModule", "Response-1", "OutputData", new FractionalSelectivity(0.3));
        application.addTupleMapping("MasterModule", "Response-2", "OutputData", new FractionalSelectivity(0.3));
        application.addTupleMapping("MasterModule", "Response-3", "OutputData", new FractionalSelectivity(0.3));

        final AppLoop loop1 = new AppLoop(Arrays.asList("Sensor", "MasterModule", "WorkerModule-1", "MasterModule", "Actuator"));
        final AppLoop loop2 = new AppLoop(Arrays.asList("Sensor", "MasterModule", "WorkerModule-2", "MasterModule", "Actuator"));
        final AppLoop loop3 = new AppLoop(Arrays.asList("Sensor", "MasterModule", "WorkerModule-3", "MasterModule", "Actuator"));

        List<AppLoop> loops = new ArrayList<>(Arrays.asList(loop1, loop2, loop3));
        application.setLoops(loops);

        return application;
    }
}
