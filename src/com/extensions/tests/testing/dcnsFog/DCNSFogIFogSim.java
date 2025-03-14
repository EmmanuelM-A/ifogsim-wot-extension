package com.extensions.tests.testing.dcnsFog;

import com.extensions.customfog.FogDeviceFactory;
import com.extensions.utils.presets.ActuatorPreset;
import com.extensions.utils.presets.ApplicationPreset;
import com.extensions.utils.presets.EdgeNodePreset;
import com.extensions.utils.presets.SensorPreset;
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
import org.fog.entities.*;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Simulation setup for case study 2 - Intelligent Surveillance
 * @author Harshit Gupta
 *
 */
public class DCNSFogIFogSim {
    static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
    static List<Sensor> sensors = new ArrayList<Sensor>();
    static List<Actuator> actuators = new ArrayList<Actuator>();
    static int numOfAreas = 1;
    static int numOfCamerasPerArea = 4;

    private static boolean CLOUD = true;

    public static void main(String[] args) {

        Log.printLine("Starting DCNS...");

        try {
            Log.disable();
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events

            CloudSim.init(num_user, calendar, trace_flag);

            String appId = "dcns"; // identifier of the application

            FogBroker broker = new FogBroker("broker");

            Application application = createApplication(appId, broker.getId(), ApplicationPreset.DEFAULT);
            application.setUserId(broker.getId());

            createFogDevices(broker.getId(), appId);

            Controller controller = null;

            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping
            for(FogDevice device : fogDevices){
                if(device.getName().startsWith("m")){ // names of all Smart Cameras start with 'm'
                    moduleMapping.addModuleToDevice("sub_processing_module", device.getName());  // fixing 1 instance of the Motion Detector module to each Smart Camera
                }
            }
            moduleMapping.addModuleToDevice("sub_processing_module", "cloud"); // fixing instances of User Interface module in the Cloud
            if(CLOUD){
                // if the mode of deployment is cloud-based
                moduleMapping.addModuleToDevice("sub_processing_module", "cloud"); // placing all instances of Object Detector module in the Cloud
                moduleMapping.addModuleToDevice("sub_processing_module", "cloud"); // placing all instances of Object Tracker module in the Cloud
            }

            controller = new Controller("master-controller", fogDevices, sensors,
                    actuators);

            controller.submitApplication(application,
                    (CLOUD)?(new ModulePlacementMapping(fogDevices, application, moduleMapping))
                            :(new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping)));

            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            CloudSim.startSimulation();

            CloudSim.stopSimulation();

            Log.printLine("VRGame finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }

    private static void createFogDevices(int userId, String appId) {
        // Create the cloud node
        FogDevice cloud = FogDeviceFactory.createFogDevice("cloud", 44800, 40000, 100, 1000, 2, 0.01, 16*103, 16*83.25);
        cloud.setLevel(0);
        cloud.setParentId(-1);
        fogDevices.add(cloud);

        // Connect the edge node (router) to cloud
        for(int i=0;i<numOfAreas;i++){
            FogDevice router = addArea(i+"", userId, appId, cloud.getId());
            fogDevices.add(router);
        }
    }

    private static FogDevice addArea(String id, int userId, String appId, int parentId){
        // Create the edge node (router)
        FogDevice router = FogDeviceFactory.createFogDevice("d-"+id, 2800, 4000, 10000, 10000, 2, 0.0, 107.339, 83.4333);
        router.setLevel(1);
        router.setParentId(parentId);
        fogDevices.add(router);

        // Connect all cameras to this router
        for(int i=0;i<numOfCamerasPerArea;i++){
            String mobileId = id+"-"+i;
            FogDevice camera = addCamera(mobileId, userId, appId, router.getId());
            fogDevices.add(camera);
        }

        return router;
    }

    private static FogDevice addCamera(String id, int userId, String appId, int parentId){
        // Create the fog device that represents the camera
        FogDevice camera = FogDeviceFactory.createFogDevice("m-"+id, 500, 1000, 10000, 10000, 2, 0, 87.53, 82.44);
        camera.setLevel(2);
        camera.setParentId(parentId);

        // Create the camera's sensor
        Sensor sensor = new Sensor("s-"+id, "CAMERA", userId, appId, new DeterministicDistribution(5)); // inter-transmission time of camera (sensor) follows a deterministic distribution
        sensor.setGatewayDeviceId(camera.getId());
        sensor.setLatency(SensorPreset.DEFAULT.LATENCY);
        sensors.add(sensor);

        // Create the camera's actuator
        Actuator ptz = new Actuator("ptz-"+id, userId, appId, "PTZ_CONTROL");
        ptz.setGatewayDeviceId(camera.getId());
        ptz.setLatency(ActuatorPreset.DEFAULT.LATENCY);
        actuators.add(ptz);

        return camera;
    }

    private static Application createApplication(String appId, int userId, ApplicationPreset applicationPreset){
        Application application = Application.createApplication(appId, userId);
        /*
         * Adding modules (vertices) to the application model (directed graph)
         */
        application.addAppModule("object_detector", 10);
        application.addAppModule("motion_detector", 10);
        application.addAppModule("object_tracker", 10);
        application.addAppModule("user_interface", 10);

        // App modules
        String MASTER_MODULE = "main_processing_module";
        String WORKER_MODULE = "sub_processing_module";

        application.addAppModule(MASTER_MODULE, applicationPreset.APP_MODULE_RAM);
        application.addAppModule(WORKER_MODULE, applicationPreset.APP_MODULE_RAM);

        // App edges
        application.addAppEdge("CAMERA", MASTER_MODULE, applicationPreset.APP_EDGE_TUPLE_CPU_LENGTH, applicationPreset.APP_EDGE_TUPLE_NW_LENGTH, "CAMERA", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(MASTER_MODULE, WORKER_MODULE, applicationPreset.APP_EDGE_TUPLE_CPU_LENGTH, applicationPreset.APP_EDGE_TUPLE_NW_LENGTH, "RAW_DATA", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge(WORKER_MODULE, MASTER_MODULE, applicationPreset.APP_EDGE_TUPLE_CPU_LENGTH, applicationPreset.APP_EDGE_TUPLE_NW_LENGTH, "PROCESSED_DATA", Tuple.DOWN, AppEdge.MODULE);
        application.addAppEdge(MASTER_MODULE, "PTZ_CONTROL", 100, applicationPreset.APP_EDGE_TUPLE_CPU_LENGTH, applicationPreset.APP_EDGE_TUPLE_NW_LENGTH, "PTZ_PARAMS", Tuple.DOWN, AppEdge.ACTUATOR);

        // Tuple mappings
        application.addTupleMapping(MASTER_MODULE, "CAMERA", "RAW_DATA", new FractionalSelectivity(1.0));
        application.addTupleMapping(WORKER_MODULE, "RAW_DATA", "PROCESSED_DATA", new FractionalSelectivity(1.0));
        application.addTupleMapping(MASTER_MODULE, "PROCESSED_DATA", "PTZ_PARAMS", new FractionalSelectivity(1.0));

        // App Loop
        final AppLoop loop1 = new AppLoop(new ArrayList<>(){{add("CAMERA");add(MASTER_MODULE);add(WORKER_MODULE);add(MASTER_MODULE);add("PTZ_PARAMS");}});
        List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);}};

        application.setLoops(loops);
        return application;
    }
}
