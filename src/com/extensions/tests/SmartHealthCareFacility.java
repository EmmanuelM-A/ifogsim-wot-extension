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
import com.extensions.vdcreation.validation.VirtualDeviceValidationManager;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.*;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.utils.TimeKeeper;

import javax.naming.ldap.Control;
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
                    "src/com/extensions/tests/input/things/SmartHealthcareApplication",
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
                VirtualDeviceValidationManager validationManager = new VirtualDeviceValidationManager(vd);

                validationManager.validateVirtualDevice();
                //

                virtualDevices.add(vd);
            }

            //////////////////////////////// APPLICATION SETUP ////////////////////////////////

            // Create Temperature Monitoring application
            Application application = createApplication(appId, broker.getId());

            // Create the physical topology for the fog devices
            createPhysicalTopology();

            Controller controller = null;

            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();

            // DO MODULE MAPPINGS & CREATE TD FOR EACH VD
            if(CLOUD) {

            }

            addAllSensorsAndActuators();

            controller = new Controller("master-controller", fogDevices, sensors, actuators);

            controller.submitApplication(application,
                    (CLOUD) ? (new ModulePlacementMapping(fogDevices, application, moduleMapping))
                            : (new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping))
            );

            //////////////////////////////// SIMULATION ////////////////////////////////

            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            CloudSim.startSimulation();

            CloudSim.stopSimulation();

        } catch (Exception e) {
            Log.printLine(e.getMessage());
        }
    }

    private static void addAllSensorsAndActuators() {
        for(VirtualDevice virtualDevice : virtualDevices) {
            if(!virtualDevice.getSensorProperties().isEmpty()) {
                sensors.addAll(virtualDevice.getSensorProperties());
            }

            if(!virtualDevice.getActuatorActions().isEmpty()) {
                actuators.addAll(virtualDevice.getActuatorActions());
            }
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

    private static Application createApplication(String appId, int userId) {
        // Creates an empty application model with the given app ID and user ID.
        Application application = Application.createApplication(appId, userId);

        // Define application components
        String CLOUD = "cloud";
        String PATIENT_GATEWAY = "patient_gateway";
        String FACILITY_GATEWAY = "facility_gateway";
        String DISPENSE_CONTROL = "medication_dispenser";
        String BED_CONTROL = "smart_bed_control";
        String LIGHTING_CONTROL = "lighting_control";
        String DOOR_LOCK_CONTROL = "door_lock_control";

        // Define modules
        String PATIENT_DATA_PROCESSOR = "patient_data_processor";
        String FACILITY_DATA_PROCESSOR = "facility_data_processor";

        // Define tuple types for data flow
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
        application.addAppEdge(ENV_SENSOR_DATA, PATIENT_GATEWAY, 500, 1000, ENV_SENSOR_DATA, Tuple.UP, AppEdge.SENSOR); // From Environmental Sensors to Patient Gateway
        application.addAppEdge(AQ_SENSOR_DATA, PATIENT_GATEWAY, 400, 900, AQ_SENSOR_DATA, Tuple.UP, AppEdge.SENSOR); // From Air Quality Sensors to Patient Gateway
        application.addAppEdge(WEARABLE_SENSOR_DATA, PATIENT_GATEWAY, 700, 1500, WEARABLE_SENSOR_DATA, Tuple.UP, AppEdge.SENSOR); // From Wearable Monitors to Patient Gateway
        application.addAppEdge(PATIENT_GATEWAY, PATIENT_DATA_PROCESSOR, 800, 2000, AGGREGATED_PATIENT_DATA, Tuple.UP, AppEdge.MODULE); // From Patient Gateway to Data Processor

        application.addAppEdge(PATIENT_DATA_PROCESSOR, MED_DISPENSER_DATA, 300, 500, DISPENSE_CONTROL, Tuple.DOWN, AppEdge.ACTUATOR); // To Medication Dispenser (Actuator)
        application.addAppEdge(PATIENT_DATA_PROCESSOR, SMART_BED_DATA, 1000, 2000, BED_CONTROL, Tuple.DOWN, AppEdge.ACTUATOR); // To Smart Bed (Actuator)

        // Facility-side devices
        application.addAppEdge(SURVEILLANCE_DATA, FACILITY_GATEWAY, 1000, 2000, SURVEILLANCE_DATA, Tuple.UP, AppEdge.SENSOR); // From Surveillance Cameras to Facility Gateway
        application.addAppEdge(RFID_DATA, FACILITY_GATEWAY, 500, 800, RFID_DATA, Tuple.UP, AppEdge.SENSOR); // From RFID Scanners to Facility Gateway
        application.addAppEdge(DOOR_LOCK_DATA, FACILITY_GATEWAY, 600, 1000, DOOR_LOCK_DATA, Tuple.UP, AppEdge.SENSOR); // From Smart Door Locks to Facility Gateway
        application.addAppEdge(LIGHTING_SYS_DATA, FACILITY_GATEWAY, 400, 700, LIGHTING_SYS_DATA, Tuple.UP, AppEdge.SENSOR); // From Lighting Systems (Sensor part) to Facility Gateway
        application.addAppEdge(FACILITY_GATEWAY, FACILITY_DATA_PROCESSOR, 900, 2000, AGGREGATED_FACILITY_DATA, Tuple.UP, AppEdge.MODULE); // From Facility Gateway to Data Processor

        application.addAppEdge(FACILITY_DATA_PROCESSOR, LIGHTING_SYS_DATA, 500, 800, LIGHTING_CONTROL, Tuple.DOWN, AppEdge.ACTUATOR); // To Lighting Systems (Actuator part)
        application.addAppEdge(FACILITY_DATA_PROCESSOR, DOOR_LOCK_DATA, 300, 600, DOOR_LOCK_CONTROL, Tuple.DOWN, AppEdge.ACTUATOR); // To Door Locks (Actuator)

        // Cloud connections
        application.addAppEdge(PATIENT_DATA_PROCESSOR, CLOUD, 5000, 500, AGGREGATED_PATIENT_DATA, Tuple.UP, AppEdge.MODULE);
        application.addAppEdge(FACILITY_DATA_PROCESSOR, CLOUD, 5000, 500, AGGREGATED_FACILITY_DATA, Tuple.UP, AppEdge.MODULE);

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

        /*
         *  Defining the application loops
         */
        List<List<String>> definedLoops = new ArrayList<>();
        definedLoops.add(new ArrayList<>(){{add(PATIENT_DATA_PROCESSOR); add(PATIENT_GATEWAY); add(DISPENSE_CONTROL);}});
        definedLoops.add(new ArrayList<>(){{add(PATIENT_DATA_PROCESSOR); add(PATIENT_GATEWAY); add(BED_CONTROL);}});
        definedLoops.add(new ArrayList<>(){{add(ENV_SENSOR_DATA); add(PATIENT_GATEWAY); add(PATIENT_DATA_PROCESSOR);}});
        definedLoops.add(new ArrayList<>(){{add(AQ_SENSOR_DATA); add(PATIENT_GATEWAY); add(PATIENT_DATA_PROCESSOR);}});
        definedLoops.add(new ArrayList<>(){{add(WEARABLE_SENSOR_DATA); add(PATIENT_GATEWAY); add(PATIENT_DATA_PROCESSOR);}});

        definedLoops.add(new ArrayList<>(){{add(SURVEILLANCE_DATA); add(FACILITY_GATEWAY); add(FACILITY_DATA_PROCESSOR);}});
        definedLoops.add(new ArrayList<>(){{add(RFID_DATA); add(FACILITY_GATEWAY); add(FACILITY_DATA_PROCESSOR);}});
        definedLoops.add(new ArrayList<>(){{add(FACILITY_DATA_PROCESSOR); add(FACILITY_GATEWAY); add(DOOR_LOCK_CONTROL);}});
        definedLoops.add(new ArrayList<>(){{add(LIGHTING_SYS_DATA); add(FACILITY_GATEWAY); add(FACILITY_DATA_PROCESSOR);}});
        definedLoops.add(new ArrayList<>(){{add(FACILITY_DATA_PROCESSOR); add(FACILITY_GATEWAY); add(LIGHTING_CONTROL);}});

        List<AppLoop> loops = generateAppLoops(definedLoops);

        application.setLoops(loops);

        return application;
    }

    private static List<AppLoop> generateAppLoops(List<List<String>> loops) {
        List<AppLoop> appLoops = new ArrayList<>();

        for(List<String> loop : loops) {
            AppLoop appLoop = new AppLoop(loop);

            appLoops.add(appLoop);
        }

        return appLoops;
    }
}
