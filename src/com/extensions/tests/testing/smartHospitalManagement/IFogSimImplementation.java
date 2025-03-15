package com.extensions.tests.testing.smartHospitalManagement;

import com.extensions.tests.helper.Helper;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.Application;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacement;
import org.fog.placement.ModulePlacementMapping;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class IFogSimImplementation {
    private static final List<FogDevice> fogDevices = new ArrayList<>();
    private static final List<Sensor> sensors = new ArrayList<>();
    private static final List<Actuator> actuators = new ArrayList<>();

    private static final boolean CLOUD = true;

    private static final String appId = "Smart Hospital Management Application";

    // App module names
    private static final String PATIENT_MONITOR_MODULE = "patientMonitoringModule";
    private static final String WARD_MANAGEMENT_MODULE = "wardManagementModule";
    private static final String INVENTORY_MODULE = "inventoryManagementModule";
    private static final String SECURITY_MODULE = "securityManagementModule";
    private static final String DATA_AGGREGATION_MODULE = "dataAggregationModule";

    public static void main(String[] args) {
        Log.printLine("Starting " + appId + "...");
        try {
            // Initial Setup
            Log.disable();
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events

            CloudSim.init(num_user, calendar, trace_flag);

            FogBroker broker = new FogBroker("broker");

            Application application = createApplication(appId, broker.getId());
            application.setUserId(broker.getId());

            createFogDevices(appId, broker.getId());

            // Module mapping
            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();

            // Map cloud modules
            for (FogDevice device : fogDevices) {
                if (device.getName().equals("cloud")) {
                    moduleMapping.addModuleToDevice(DATA_AGGREGATION_MODULE, device.getName());
                }
            }

            // Map edge modules
            for (FogDevice device : fogDevices) {
                if (device.getName().equals("icu-monitoring")) {
                    moduleMapping.addModuleToDevice(PATIENT_MONITOR_MODULE, device.getName());
                } else if (device.getName().equals("ward-monitoring")) {
                    moduleMapping.addModuleToDevice(WARD_MANAGEMENT_MODULE, device.getName());
                } else if (device.getName().equals("inventory-managment")) {
                    moduleMapping.addModuleToDevice(INVENTORY_MODULE, device.getName());
                } else if (device.getName().equals("hospital-security")) {
                    moduleMapping.addModuleToDevice(SECURITY_MODULE, device.getName());
                }
            }

            // Create module placement
            ModulePlacement modulePlacement = new ModulePlacementMapping(fogDevices, application, moduleMapping);

            // Start simulation
            CloudSim.startSimulation();
            CloudSim.stopSimulation();

            Log.printLine(appId + " finished!");

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happened");
        }
    }

    private static void createFogDevices(String appId, int userId) {
        // Create cloud node
        FogDevice cloud = Helper.createCloudNode("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25, fogDevices);

        // CREATE EDGE NODES & CONNECT END DEVICES TO EACH
        // 1. ICU Monitoring Edge Node
        FogDevice edgeIcuMonitoring = Helper.createEdgeNode("icu-monitoring", 2000, 4000, 10000, 10000, 1, 0.0, 107.53, 83.44, cloud.getId(), fogDevices);
        Helper.createEndDevices("PatientMonitoringDevice", 1000, 2000, 10000, 10000, 2, 0.0, 87.53, 82.44, 20, appId, userId, edgeIcuMonitoring.getId(), fogDevices, sensors, actuators);
        Helper.createEndDevices("EmergencyAlertSystem", 1000, 1000, 10000, 10000, 2, 0.0, 82.53, 75.44, 1, appId, userId, edgeIcuMonitoring.getId(), fogDevices, sensors, actuators);

        // 2. Ward Monitoring Edge Node
        FogDevice edgeWardMonitoring = Helper.createEdgeNode("ward-monitoring", 2000, 4000, 10000, 10000, 1, 0.0, 107.53, 83.44, cloud.getId(), fogDevices);
        Helper.createEndDevices("SmartBed", 800, 1000, 10000, 10000, 2, 0.0, 82.53, 75.44, 10, appId, userId, edgeWardMonitoring.getId(), fogDevices, sensors, actuators);
        Helper.createEndDevices("HVACControlSystem", 1000, 1000, 10000, 10000, 2, 0.0, 87.53, 82.44, 2, appId, userId, edgeWardMonitoring.getId(), fogDevices, sensors, actuators);
        Helper.createEndDevices("SmartDisplay", 800, 1000, 10000, 10000, 2, 0.0, 82.53, 75.44, 1, appId, userId, edgeWardMonitoring.getId(), fogDevices, sensors, actuators);
        Helper.createEndDevices("SmartLight", 500, 500, 10000, 10000, 2, 0.0, 77.53, 70.44, 10, appId, userId, edgeWardMonitoring.getId(), fogDevices, sensors, actuators);

        // 3. Inventory Management Edge Node
        FogDevice edgeInventoryManagement = Helper.createEdgeNode("inventory-managment", 2000, 4000, 10000, 10000, 1, 0.0, 107.53, 83.44, cloud.getId(), fogDevices);
        Helper.createEndDevices("RFIDInventorySystem", 1000, 1000, 10000, 10000, 2, 0.0, 87.53, 82.44, 5, appId, userId, edgeInventoryManagement.getId(), fogDevices, sensors, actuators);
        Helper.createEndDevices("SmartDisplay", 800, 1000, 10000, 10000, 2, 0.0, 82.53, 75.44, 1, appId, userId, edgeInventoryManagement.getId(), fogDevices, sensors, actuators);

        // 4. Hospital Security Edge Node
        FogDevice edgeHospitalSecurity = Helper.createEdgeNode("hospital-security", 2000, 4000, 10000, 10000, 1, 0.0, 107.53, 83.44, cloud.getId(), fogDevices);
        Helper.createEndDevices("SecurityCameraSystem", 1200, 2000, 10000, 10000, 2, 0.0, 92.53, 85.44, 5, appId, userId, edgeHospitalSecurity.getId(), fogDevices, sensors, actuators);
        Helper.createEndDevices("SmartCamera", 1000, 1500, 10000, 10000, 2, 0.0, 87.53, 82.44, 10, appId, userId, edgeHospitalSecurity.getId(), fogDevices, sensors, actuators);
        Helper.createEndDevices("Alarm", 800, 1000, 10000, 10000, 2, 0.0, 82.53, 75.44, 10, appId, userId, edgeHospitalSecurity.getId(), fogDevices, sensors, actuators);
        Helper.createEndDevices("SmartDisplay", 800, 1000, 10000, 10000, 2, 0.0, 82.53, 75.44, 1, appId, userId, edgeHospitalSecurity.getId(), fogDevices, sensors, actuators);
    }

    private static Application createApplication(String appId, int userId) {
        // Create application
        Application application = Application.createApplication(appId, userId);

        // Add app modules (processing elements of the application)
        application.addAppModule(PATIENT_MONITOR_MODULE, 100);
        application.addAppModule(WARD_MANAGEMENT_MODULE, 100);
        application.addAppModule(INVENTORY_MODULE, 100);
        application.addAppModule(SECURITY_MODULE, 100);
        application.addAppModule(DATA_AGGREGATION_MODULE, 200);

        // Add application edges (data flow between modules and sensors/actuators)
        // ICU Monitoring edges
        application.addAppEdge("PatientMonitoringDevice_SENSOR", PATIENT_MONITOR_MODULE, 1000, 500, "PATIENT_DATA", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(PATIENT_MONITOR_MODULE, "ACTUATOR", 1000, 500, "PATIENT_ALERT", Tuple.DOWN, AppEdge.ACTUATOR);
        application.addAppEdge(PATIENT_MONITOR_MODULE, DATA_AGGREGATION_MODULE, 1000, 500, "PATIENT_ANALYTICS", Tuple.UP, AppEdge.MODULE);

        // Emergency Alert edges
        application.addAppEdge("EmergencyAlertSystem_SENSOR", PATIENT_MONITOR_MODULE, 1000, 500, "EMERGENCY_ALERT", Tuple.UP, AppEdge.SENSOR);

        // Ward Management edges
        application.addAppEdge("SmartBed_SENSOR", WARD_MANAGEMENT_MODULE, 1000, 500, "BED_DATA", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("HVACControlSystem_SENSOR", WARD_MANAGEMENT_MODULE, 1000, 500, "HVAC_DATA", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(WARD_MANAGEMENT_MODULE, "SmartDisplay_ACTUATOR", 1000, 500, "WARD_DISPLAY", Tuple.DOWN, AppEdge.ACTUATOR);
        application.addAppEdge(WARD_MANAGEMENT_MODULE, "SmartLight_ACTUATOR", 1000, 500, "LIGHT_CONTROL", Tuple.DOWN, AppEdge.ACTUATOR);
        application.addAppEdge(WARD_MANAGEMENT_MODULE, DATA_AGGREGATION_MODULE, 1000, 500, "WARD_ANALYTICS", Tuple.UP, AppEdge.MODULE);

        // Inventory Management edges
        application.addAppEdge("RFIDInventorySystem_SENSOR", INVENTORY_MODULE, 1000, 500, "INVENTORY_DATA", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(INVENTORY_MODULE, "SmartDisplay_ACTUATOR", 1000, 500, "INVENTORY_DISPLAY", Tuple.DOWN, AppEdge.ACTUATOR);
        application.addAppEdge(INVENTORY_MODULE, DATA_AGGREGATION_MODULE, 1000, 500, "INVENTORY_ANALYTICS", Tuple.UP, AppEdge.MODULE);

        // Security Management edges
        application.addAppEdge("SecurityCameraSystem_SENSOR", SECURITY_MODULE, 1000, 500, "SECURITY_CAM_DATA", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("SmartCamera_SENSOR", SECURITY_MODULE, 1000, 500, "CAMERA_DATA", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(SECURITY_MODULE, "Alarm_ACTUATOR", 1000, 500, "ALARM_CONTROL", Tuple.DOWN, AppEdge.ACTUATOR);
        application.addAppEdge(SECURITY_MODULE, "SmartDisplay_ACTUATOR", 1000, 500, "SECURITY_DISPLAY", Tuple.DOWN, AppEdge.ACTUATOR);
        application.addAppEdge(SECURITY_MODULE, DATA_AGGREGATION_MODULE, 1000, 500, "SECURITY_ANALYTICS", Tuple.UP, AppEdge.MODULE);

        // Add selectivity of applications (probability of producing output tuples)
        application.addTupleMapping(PATIENT_MONITOR_MODULE, "PATIENT_DATA", "PATIENT_ALERT", new FractionalSelectivity(0.3));
        application.addTupleMapping(PATIENT_MONITOR_MODULE, "PATIENT_DATA", "PATIENT_ANALYTICS", new FractionalSelectivity(1.0));
        application.addTupleMapping(PATIENT_MONITOR_MODULE, "EMERGENCY_ALERT", "PATIENT_ALERT", new FractionalSelectivity(1.0));

        application.addTupleMapping(WARD_MANAGEMENT_MODULE, "BED_DATA", "WARD_DISPLAY", new FractionalSelectivity(1.0));
        application.addTupleMapping(WARD_MANAGEMENT_MODULE, "BED_DATA", "WARD_ANALYTICS", new FractionalSelectivity(1.0));
        application.addTupleMapping(WARD_MANAGEMENT_MODULE, "HVAC_DATA", "LIGHT_CONTROL", new FractionalSelectivity(0.5));

        application.addTupleMapping(INVENTORY_MODULE, "INVENTORY_DATA", "INVENTORY_DISPLAY", new FractionalSelectivity(1.0));
        application.addTupleMapping(INVENTORY_MODULE, "INVENTORY_DATA", "INVENTORY_ANALYTICS", new FractionalSelectivity(1.0));

        application.addTupleMapping(SECURITY_MODULE, "SECURITY_CAM_DATA", "ALARM_CONTROL", new FractionalSelectivity(0.1));
        application.addTupleMapping(SECURITY_MODULE, "CAMERA_DATA", "SECURITY_DISPLAY", new FractionalSelectivity(1.0));
        application.addTupleMapping(SECURITY_MODULE, "SECURITY_CAM_DATA", "SECURITY_ANALYTICS", new FractionalSelectivity(1.0));

        // Define application loops (important for latency calculation)
        final AppLoop patientMonitoringLoop = new AppLoop(new ArrayList<String>() {{
            add("PatientMonitoringDevice_SENSOR");
            add(PATIENT_MONITOR_MODULE);
            add("ACTUATOR");
        }});

        final AppLoop wardManagementLoop = new AppLoop(new ArrayList<String>() {{
            add("SmartBed_SENSOR");
            add(WARD_MANAGEMENT_MODULE);
            add("SmartDisplay_ACTUATOR");
        }});

        final AppLoop inventoryLoop = new AppLoop(new ArrayList<String>() {{
            add("RFIDInventorySystem_SENSOR");
            add(INVENTORY_MODULE);
            add("SmartDisplay_ACTUATOR");
        }});

        final AppLoop securityLoop = new AppLoop(new ArrayList<String>() {{
            add("SecurityCameraSystem_SENSOR");
            add(SECURITY_MODULE);
            add("Alarm_ACTUATOR");
        }});

        List<AppLoop> loops = new ArrayList<AppLoop>() {{
            add(patientMonitoringLoop);
            add(wardManagementLoop);
            add(inventoryLoop);
            add(securityLoop);
        }};

        application.setLoops(loops);

        return application;
    }
}