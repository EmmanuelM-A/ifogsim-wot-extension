package com.extensions;

import com.extensions.sysconstructor.core.ApplicationTopologyParser;
import com.extensions.utils.FilePaths;
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
import org.fog.entities.FogBroker;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public final class App {
    public static void main(String[] args) {
        // Determines if the application deployment is cloud-based.
        boolean CLOUD = false;

        List<VirtualDevice> virtualDevices = new ArrayList<>();

        Log.printLine("Starting Simulation...");

        try {
            ApplicationTopologyParser applicationTopologyParser = new ApplicationTopologyParser(new File(FilePaths.APPLICATION_TOPOLOGY));

            Log.disable();

            //////////////////////////////// INITIAL SETUP ////////////////////////////////

            // Specifies the number of users interacting with the cloud.
            int numUsers = 1;

            // Initializes a calendar object to track simulation time and events.
            Calendar calendar = Calendar.getInstance();

            // Determines whether to enable tracing of simulation events for debugging purposes.
            boolean traceFlag = false;

            // Initializes the CloudSim toolkit with the specified number of users, the calendar instance, and trace settings.
            CloudSim.init(numUsers, calendar, traceFlag);

            // Assigns a unique identifier to the application being simulated. This ID is used to manage the application's components and operations.
            String applicationTitle = applicationTopologyParser.parseApplicationTitle();
            String appId = applicationTitle != null ? applicationTitle: "Default";

            // Initializes a FogBroker, which manages application modules and coordinates communication between them in the simulation.
            FogBroker broker = new FogBroker("broker");

            //////////////////////////////// VIRTUAL DEVICE CREATION ////////////////////////////////

            // Extract the metadata from the TDs
            List<ThingDescription> thingDescriptions = JsonFileProcessor.processJsonFiles(
                    FilePaths.JSON_THINGS_REPO, // SET THINGS REPO HERE
                    new ThingDescriptionParser()
            );

            // Set up the VD factory to create VDs with the appropriate presets
            VirtualDeviceFactory virtualDeviceFactory = new VirtualDeviceFactory(broker.getId(), appId, FogDevicePreset.DEFAULT, SensorPreset.DEFAULT, ActuatorPreset.DEFAULT);
            VirtualDeviceConfigParser vdConfigParser = new VirtualDeviceConfigParser();

            // Create the virtual devices using the thing descriptions and factory method
            for(ThingDescription thingDescription : thingDescriptions) {
                VirtualDevice vd = virtualDeviceFactory.createVirtualDevice(
                        thingDescription,
                        vdConfigParser.process(new File(FilePaths.VD_CONFIG_FILE)) // SET VD'S CONFIG FILE HERE
                );
                // Validate VD HERE
                virtualDevices.add(vd);
            }

            //////////////////////////////// APPLICATION SETUP ////////////////////////////////



            //////////////////////////////// SIMULATION ////////////////////////////////
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
