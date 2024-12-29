package com.extensions.tests;

import com.extensions.customfog.FogDeviceFactory;
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
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Represents a smart healthcare facility using IoT devices to monitor patients'
 * health, manage environmental conditions and ensure safety.
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
            int numUsers = 10;

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
            createPhysicalTopology(virtualDevices);

            //////////////////////////////// SIMULATION ////////////////////////////////

        } catch (Exception e) {
            Log.printLine(e.getMessage());
        }
    }

    private static void createPhysicalTopology(List<VirtualDevice> virtualDevices) {
        // Create the cloud device at the top of the hierarchy
        FogDevice cloud = FogDeviceFactory.createFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25);

        // Cloud has no parent, it is the root of the hierarchy
        cloud.setParentId(-1);

        // Create the centralized fog devices
        //FogDevice patientFog = FogDeviceFactory.createFogDevice()

        // Add the cloud and proxy devices to the list of fog devices
        fogDevices.add(cloud);
    }

    private static Application createApplication(String appId, int userId) {
        // Creates an empty application model with the given app ID and user ID.
        Application application = Application.createApplication(appId, userId);

        /*
         * Adding modules (vertices) to the application model where each module represents
         * a processing or functional unit in the application.
         */
        application.addAppModule("patient-monitor-aggregator", 10);
        //application.addAppModule("");


        return null;
    }
}
