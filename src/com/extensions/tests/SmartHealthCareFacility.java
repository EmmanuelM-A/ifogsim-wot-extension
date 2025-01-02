package com.extensions.tests;

import com.extensions.customfog.FogDeviceFactory;
import com.extensions.utils.Utility;
import com.extensions.utils.presets.ActuatorPreset;
import com.extensions.utils.presets.FogDevicePreset;
import com.extensions.utils.presets.SensorPreset;
import com.extensions.vdcreation.core.JsonFileProcessor;
import com.extensions.vdcreation.core.VirtualDevice;
import com.extensions.vdcreation.core.VirtualDeviceFactory;
import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.parsers.ThingDescriptionParser;
import com.extensions.vdcreation.parsers.VirtualDeviceConfigParser;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Represents a smart healthcare facility using IoT devices to monitor patients'
 * health, manage environmental conditions and ensure safety and security.
 * <p>
 * Hierarchy/ Component Setup:
 *                         | - Medication Dispensers
 *                         | - Environmental Sensors (Temperature, Humidity)
 *                         | - Air Quality Sensors
 *                         | - Smart Beds
 *                         | - Wearable Health Monitors (Heart Rate, Oxygen saturation)
 *                        /
 *                   GATEWAY (@Patient)
 *                 /
 *               FOG1 (Patient Monitoring Aggregator)
 *             /
 *          CLOUD
 *             \
 *               FOG2 (Facility Management Aggregator)
 *                 \
 *                   GATEWAY (@Facility)
 *                        \
 *                        | - Surveillance Cameras
 *                        | - RFID Tag Scanners
 *                        | - Smart Door Locks
 *                        | - Smart Lighting Systems
 */
public class SmartHealthCareFacility {
    /**
     * Represents all fog devices in the application including the fog devices of the virtual devices.
     */
    private static final List<FogDevice> fogDevices = new ArrayList<>();

    /**
     * Represents all sensors in the application including the sensors of the virtual devices.
     */
    private static final List<Sensor> sensors = new ArrayList<>();

    /**
     * Represents all actuators in the application including the actuators of the virtual devices.
     */
    private static final List<Actuator> actuators = new ArrayList<>();

    /**
     * Stores all {@link VirtualDevice} objects created.
     */
    private static final List<VirtualDevice> virtualDevices = new ArrayList<>();

    /**
     * Determines if the application deployment is cloud-based.
     */
    private static final boolean CLOUD = false;

    public static void main(String[] args) {
        Log.printLine("Starting Smart Healthcare Facility Application....");

        try {
            Log.disable();

            //////////////////////////////// INITIAL SETUP ////////////////////////////////

            // Specifies the number of users interacting with the cloud.
            int numUsers = 10; // SWITCH 100 LATER

            // Initializes a calendar object to track simulation time and events.
            Calendar calendar = Calendar.getInstance();

            // Determines whether to enable tracing of simulation events for debugging purposes.
            boolean trace_flag = false;

            // Initializes the CloudSim toolkit with the specified number of users, the calendar instance, and trace settings.
            CloudSim.init(numUsers, calendar, trace_flag);

            // Assigns a unique identifier to the application being simulated. This ID is used to manage the application's components and operations.
            String appId = "Smart-Healthcare-Facility";

            // Initializes a FogBroker, which manages application modules and coordinates communication between them in the simulation.
            FogBroker broker = new FogBroker("broker");

            //////////////////////////////// VIRTUAL DEVICE CREATION ////////////////////////////////

            // Extract the metadata from the TDs
            List<ThingDescription> thingDescriptions = JsonFileProcessor.processJsonFiles(
                    "src/com/extensions/input/things/healthcare",
                    new ThingDescriptionParser()
            );

            // Set up the VD factory to create VDs with the appropriate presets
            VirtualDeviceFactory virtualDeviceFactory = new VirtualDeviceFactory(broker.getId(), appId, FogDevicePreset.DEFAULT, SensorPreset.DEFAULT, ActuatorPreset.DEFAULT);
            VirtualDeviceConfigParser vdConfigParser = new VirtualDeviceConfigParser();

            // Create the virtual devices using the thing descriptions and factory method
            for(ThingDescription thingDescription : thingDescriptions) {
                VirtualDevice vd = virtualDeviceFactory.createVirtualDevice(
                        thingDescription,
                        null
                );
                // Validate VD HERE
                virtualDevices.add(vd);
            }

            //////////////////////////////// APPLICATION SETUP ////////////////////////////////

            // Create Temperature Monitoring application
            Application application = createApplication(appId, broker.getId());

            // Create the physical topology for the fog devices
            createPhysicalTopology();

            //////////////////////////////// SIMULATION ////////////////////////////////

        } catch (Exception e) {
            Log.printLine(e.getMessage());
        }
    }

    private static void createPhysicalTopology() {
        // Create the cloud device at the top of the hierarchy
        FogDevice cloud = FogDeviceFactory.createFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25);

        // Cloud has no parent, it is the root of the hierarchy
        cloud.setParentId(-1);

        fogDevices.add(cloud);

        // Create the centralized fog devices (patient-fog and facility-fog)
        FogDevice patientFog = FogDeviceFactory.createFogDevice("patient-fog", FogDevicePreset.DEFAULT);
        if(patientFog != null) {
            patientFog.setParentId(cloud.getId());
            patientFog.setUplinkLatency(100);
            fogDevices.add(patientFog);
        }

        FogDevice facilityFog = FogDeviceFactory.createFogDevice("facility-fog", FogDevicePreset.DEFAULT);
        if(facilityFog != null) {
            facilityFog.setParentId(cloud.getId());
            facilityFog.setUplinkLatency(100);
            fogDevices.add(facilityFog);
        }

        // Create the patient fog gateway
        FogDevice patientGateway = FogDeviceFactory.createFogDevice("patient-gateway", FogDevicePreset.DEFAULT);
        if(patientGateway != null) {
            if(patientFog != null) patientGateway.setParentId(patientFog.getId());
            patientGateway.setUplinkLatency(50);
            fogDevices.add(patientGateway);
        }

        // Create the facility fog gateway
        FogDevice facilityGateway = FogDeviceFactory.createFogDevice("patient-gateway", FogDevicePreset.DEFAULT);
        if(facilityGateway != null) {
            if(patientFog != null) facilityGateway.setParentId(patientFog.getId());
            facilityGateway.setUplinkLatency(50);
            fogDevices.add(facilityGateway);
        }

        // Create and connect the virtual devices for the patient gateway
        String[] patientVDNames = new String[]{
                "MedicationDispenser", "EnvironmentalSensor", "AirQualitySensor", "SmartBed", "SmartBed",
                "SmartBed", "SmartBed", "WearableHealthMonitor", "WearableHealthMonitor", "WearableHealthMonitor",
                "WearableHealthMonitor"
        };
        setupVDConnections(patientVDNames, patientGateway);

        // Create and connect the virtual devices for the fog gateway
        String[] facilityVDNames = new String[]{
                "SurveillanceCamera", "RFIDTagScanner", "SmartDoorLock", "SmartLightingSystem"
        };
        setupVDConnections(facilityVDNames, facilityGateway);
    }

    private static void setupVDConnections(String[] vdNames, FogDevice gateway) {
        for(String vdName : vdNames) {
            VirtualDevice virtualDevice = Utility.getVirtualDevice(virtualDevices, vdName);
            if(virtualDevice != null) {
                FogDevice VDFogDevice = virtualDevice.getFogDevice();
                if(gateway != null) VDFogDevice.setParentId(gateway.getId());
                VDFogDevice.setUplinkLatency(10);
            }
        }
    }

    /*private static Application createApplication(String appId, int userId) {
        // Creates an empty application model with the given app ID and user ID.
        Application application = Application.createApplication(appId, userId);

        // Define IoT devices' actuators and sensors
        String MED_DISPENSER = "medication_dispenser";

        // Define module names
        String CLOUD_MOD = "cloud_module";

        String PATIENT_FOG_MOD = "patient_fog_module";
        String PATIENT_GATEWAY_MOD = "patient_gateway";
        String MED_DISPENSER_MOD = "medication_dispenser_module";
        String ENV_SENSOR_MOD = "env_sensor_module";
        String AQ_SENSOR_MOD = "air_quality_sensor_module";
        String SMART_BED_MOD = "smart_bed_module";
        String WEARABLE_MONITOR_MOD = "wearable_monitor_module";

        String FACILITY_FOG_MOD = "facility_fog_module";
        String FACILITY_GATEWAY_MOD = "facility_gateway";
        String SURVEILLANCE_MOD = "surveillance_module";
        String RFID_SCANNER_MOD = "rfid_scanner_module";
        String SMART_DOOR_LOCK_MOD = "smart_door_lock_module";
        String SMART_LIGHTS_MOD = "smart_lighting_module";

        String MED_DATA_PROCESSOR = "medication_processor";

        // Add application modules (vertices)
        application.addAppModule(CLOUD_MOD, 50);
        application.addAppModule(PATIENT_FOG_MOD, 30);
        application.addAppModule(PATIENT_GATEWAY_MOD, 20);
        application.addAppModule(MED_DISPENSER_MOD, 10);
        application.addAppModule(ENV_SENSOR_MOD, 10);
        application.addAppModule(AQ_SENSOR_MOD, 10);
        application.addAppModule(SMART_BED_MOD, 15);
        application.addAppModule(WEARABLE_MONITOR_MOD, 15);
        application.addAppModule(FACILITY_FOG_MOD, 30);
        application.addAppModule(FACILITY_GATEWAY_MOD, 20);
        application.addAppModule(SURVEILLANCE_MOD, 10);
        application.addAppModule(RFID_SCANNER_MOD, 10);
        application.addAppModule(SMART_DOOR_LOCK_MOD, 10);
        application.addAppModule(SMART_LIGHTS_MOD, 10);

        // Define tuple types for data flow ****NOTE - THE TUPLE TYPE OF THE EDGE NEEDS TO MATCH THE TUPLE TYPE OF THE CORRESPONDING SENSOR
        String MEDICATION_DATA = "medication_data";
        String ENV_DATA = "environmental_data";
        String AIR_QUALITY_DATA = "air_quality_data";
        String BED_DATA = "smart_bed_data";
        String WEARABLE_DATA = "wearable_data";

        String GATEWAY_TO_FOG = "gateway_to_fog_data";
        String FOG_TO_CLOUD = "fog_to_cloud_data";
        String CLOUD_TO_FOG = "cloud_to_fog_data";
        String FOG_TO_GATEWAY = "fog_to_gateway_data";

        String SURVEILLANCE_DATA = "surveillance_data";
        String RFID_DATA = "rfid_data";
        String DOOR_LOCK_DATA = "door_lock_data";
        String LIGHTING_DATA = "lighting_data";

        // Add application edges (directed data flow)

        // Data sources (sensors) to data modules
        application.addAppEdge(PATIENT_FOG_MOD, MED_DISPENSER_MOD, 100, 500, MEDICATION_DATA, Tuple.UP, AppEdge.SENSOR);

        // Adding edge from MED_DATA_PROCESSOR to MED_DISPENSER (actuator) carrying tuples of type MEDICATION_DATA
        application.addAppEdge(MED_DISPENSER_MOD, MED_DISPENSER, 100, 50, 100, MEDICATION_DATA, Tuple.DOWN, AppEdge.ACTUATOR);

        // Adding edge from ENV_DATA (sensor) to ENV_SENSOR_MOD carrying tuples of type ENV_DATA
        application.addAppEdge(ENV_DATA, ENV_SENSOR_MOD, 100, 500, ENV_DATA, Tuple.UP, AppEdge.SENSOR);

        // Adding edge from AIR_QUALITY_DATA (sensor) to AQ_SENSOR_MOD carrying tuples of type AIR_QUALITY_DATA
        application.addAppEdge(AIR_QUALITY_DATA, AQ_SENSOR_MOD, 100, 500, AIR_QUALITY_DATA, Tuple.UP, AppEdge.SENSOR);

        //application.addAppEdge(BED_DATA, SMART_BED_MOD, 100, 500, BED_DATA, Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(WEARABLE_DATA, WEARABLE_MONITOR_MOD, 100, 500, WEARABLE_DATA, Tuple.UP, AppEdge.SENSOR);

        // Data modules to gateway modules
        application.addAppEdge(ENV_SENSOR_MOD, PATIENT_GATEWAY_MOD, 500, 1000, ENV_DATA, Tuple.UP, AppEdge.MODULE);
        application.addAppEdge();

        application.addAppEdge(medicationDispenserModule, patientGatewayModule, 500, 1000, GATEWAY_TO_FOG, Tuple.UP, AppEdge.MODULE);
        application.addAppEdge(envSensorModule, patientGatewayModule, 500, 1000, GATEWAY_TO_FOG, Tuple.UP, AppEdge.MODULE);
        application.addAppEdge(patientGatewayModule, patientFogModule, 1000, 2000, FOG_TO_CLOUD, Tuple.UP, AppEdge.MODULE);
        //application.addAppEdge(patientFogModule, cloudModule, 2000, 4000, CLOUD_TO_FOG, Tuple.UP, AppEdge.MODULE);

        //application.addAppEdge(cloudModule, facilityFogModule, 2000, 4000, CLOUD_TO_FOG, Tuple.DOWN, AppEdge.MODULE);
        application.addAppEdge(facilityFogModule, facilityGatewayModule, 1000, 2000, FOG_TO_GATEWAY, Tuple.DOWN, AppEdge.MODULE);

        application.addAppEdge(SURVEILLANCE_DATA, surveillanceModule, 100, 500, SURVEILLANCE_DATA, Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(RFID_DATA, rfidScannerModule, 100, 500, RFID_DATA, Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(DOOR_LOCK_DATA, smartDoorLockModule, 100, 500, DOOR_LOCK_DATA, Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(LIGHTING_DATA, smartLightingModule, 100, 500, LIGHTING_DATA, Tuple.UP, AppEdge.SENSOR);

        // Define tuple mappings (input-output relationships)
        application.addTupleMapping(medicationDispenserModule, MEDICATION_DATA, GATEWAY_TO_FOG, new FractionalSelectivity(1.0));
        application.addTupleMapping(envSensorModule, ENV_DATA, GATEWAY_TO_FOG, new FractionalSelectivity(1.0));
        application.addTupleMapping(patientGatewayModule, GATEWAY_TO_FOG, FOG_TO_CLOUD, new FractionalSelectivity(1.0));
        application.addTupleMapping(patientFogModule, FOG_TO_CLOUD, CLOUD_TO_FOG, new FractionalSelectivity(1.0));

        application.addTupleMapping(cloudModule, CLOUD_TO_FOG, FOG_TO_GATEWAY, new FractionalSelectivity(1.0));
        application.addTupleMapping(facilityFogModule, FOG_TO_GATEWAY, SURVEILLANCE_DATA, new FractionalSelectivity(1.0));

        // Define application loops for latency monitoring
        List<AppLoop> loops = getAppLoops(MEDICATION_DATA, SURVEILLANCE_DATA);

        application.setLoops(loops);

        return application;

        return null;
    }*/

    private static Application createApplication(String appId, int userId) {
        // Creates an empty application model with the given app ID and user ID.
        Application application = Application.createApplication(appId, userId);

        // Define application components
        String CLOUD = "cloud";

        // Define modules
        String PATIENT_DATA_PROCESSOR = "patient_data_processor";
        String FACILITY_DATA_PROCESSOR = "facility_data_processor";

        // Define tuple types for data flow ****NOTE - THE TUPLE TYPE OF THE EDGE NEEDS TO MATCH THE TUPLE TYPE OF THE CORRESPONDING SENSOR
        String MED_DISPENSER_DATA = "medication_data";
        String ENV_SENSOR_DATA = "environmental_data";
        String AQ_SENSOR_DATA = "air_quality_data";
        String SMART_BED_DATA = "smart_bed_data";
        String WEARABLE_SENSOR_DATA = "wearable_data";

        String SURVEILLANCE_DATA = "surveillance_data";
        String RFID_DATA = "rfid_data";
        String DOOR_LOCK_DATA = "door_lock_data";
        String LIGHTING_SYS_DATA = "lighting_system_data";

        String AGGREGATED_FACILITY_DATA = "aggregated_facility_data";
        String AGGREGATED_PATIENT_DATA = "aggregated_patient_data";

        /*
         * Adding modules (vertices) to the application model (directed graph)
         */
        application.addAppModule(PATIENT_DATA_PROCESSOR, 20); // Processes patient data at the patient fog node
        application.addAppModule(FACILITY_DATA_PROCESSOR, 20); // Processes facility-related data at the facility fog node

        /*
         * Connecting the application modules (vertices) in the application model (directed graph) with edges
         */
        // Patient-side devices
        application.addAppEdge(PATIENT_DATA_PROCESSOR, MED_DISPENSER_DATA, 1000, 100, "DISPENSER_COMMAND", Tuple.DOWN, AppEdge.ACTUATOR);
        application.addAppEdge(ENV_SENSOR_DATA, PATIENT_DATA_PROCESSOR, 1000, 2000, ENV_SENSOR_DATA, Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(AQ_SENSOR_DATA, PATIENT_DATA_PROCESSOR, 1000, 2000, AQ_SENSOR_DATA, Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(PATIENT_DATA_PROCESSOR, SMART_BED_DATA, 1000, 2000, "SMART_BED_COMMAND", Tuple.DOWN, AppEdge.ACTUATOR);
        application.addAppEdge(WEARABLE_SENSOR_DATA, PATIENT_DATA_PROCESSOR, 1000, 2000, WEARABLE_SENSOR_DATA, Tuple.UP, AppEdge.SENSOR);

        // Facility-side devices
        application.addAppEdge(SURVEILLANCE_DATA, FACILITY_DATA_PROCESSOR, 1000, 2000, SURVEILLANCE_DATA, Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(RFID_DATA, FACILITY_DATA_PROCESSOR, 1000, 2000, RFID_DATA, Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(FACILITY_DATA_PROCESSOR, DOOR_LOCK_DATA, 500, 100, "LOCK_COMMAND", Tuple.DOWN, AppEdge.ACTUATOR);
        application.addAppEdge(FACILITY_DATA_PROCESSOR, LIGHTING_SYS_DATA, 1000, 100, "LIGHT_COMMAND", Tuple.DOWN, AppEdge.ACTUATOR);
        application.addAppEdge(LIGHTING_SYS_DATA, FACILITY_DATA_PROCESSOR, 1000, 2000, LIGHTING_SYS_DATA, Tuple.UP, AppEdge.SENSOR);

        // Cloud connections
        application.addAppEdge(PATIENT_DATA_PROCESSOR, CLOUD, 5000, 500, AGGREGATED_PATIENT_DATA, Tuple.UP, AppEdge.MODULE);
        application.addAppEdge(FACILITY_DATA_PROCESSOR, CLOUD, 5000, 500, AGGREGATED_FACILITY_DATA, Tuple.UP, AppEdge.MODULE);

        // Cloud-to-fog interactions
        application.addAppEdge(CLOUD, PATIENT_DATA_PROCESSOR, 2000, 500, "CLOUD_PATIENT_COMMAND", Tuple.DOWN, AppEdge.MODULE);
        application.addAppEdge(CLOUD, FACILITY_DATA_PROCESSOR, 2000, 500, "CLOUD_FACILITY_COMMAND", Tuple.DOWN, AppEdge.MODULE);

        /*
         * Defining the input-output relationships (represented by selectivity) of the application modules
         */
        application.addTupleMapping(PATIENT_DATA_PROCESSOR, ENV_SENSOR_DATA, AGGREGATED_PATIENT_DATA, new FractionalSelectivity(2.0));
        application.addTupleMapping(PATIENT_DATA_PROCESSOR, AQ_SENSOR_DATA, AGGREGATED_PATIENT_DATA, new FractionalSelectivity(1.0));
        application.addTupleMapping(PATIENT_DATA_PROCESSOR, WEARABLE_SENSOR_DATA, AGGREGATED_PATIENT_DATA, new FractionalSelectivity(3.5));

        application.addTupleMapping(FACILITY_DATA_PROCESSOR, SURVEILLANCE_DATA, AGGREGATED_FACILITY_DATA, new FractionalSelectivity(2.4));
        application.addTupleMapping(FACILITY_DATA_PROCESSOR, RFID_DATA, AGGREGATED_FACILITY_DATA, new FractionalSelectivity(1.0));
        application.addTupleMapping(FACILITY_DATA_PROCESSOR, DOOR_LOCK_DATA, AGGREGATED_FACILITY_DATA, new FractionalSelectivity(1.0));
        application.addTupleMapping(FACILITY_DATA_PROCESSOR, LIGHTING_SYS_DATA, AGGREGATED_FACILITY_DATA, new FractionalSelectivity(3.2));


        return null;
    }

    private static List<AppLoop> getAppLoops(String MEDICATION_DATA, String SURVEILLANCE_DATA) {
        final AppLoop patientLoop = new AppLoop(new ArrayList<String>() {{
            add(MEDICATION_DATA);
            add(medicationDispenserModule);
            add(patientGatewayModule);
            add(patientFogModule);
            add(cloudModule);
        }});

        final AppLoop facilityLoop = new AppLoop(new ArrayList<String>() {{
            add(SURVEILLANCE_DATA);
            add(surveillanceModule);
            add(facilityGatewayModule);
            add(facilityFogModule);
            add(cloudModule);
        }});

        List<AppLoop> loops = new ArrayList<AppLoop>() {{
            add(patientLoop);
            add(facilityLoop);
        }};
        return loops;
    }
}
