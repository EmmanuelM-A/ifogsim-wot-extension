package com.extensions;

import com.extensions.custommetrics.metrics.MeanTupleExecutionDelay;
import com.extensions.simulation.Simulation;
import com.extensions.utils.presets.*;
import org.cloudbus.cloudsim.Log;

import java.io.IOException;

/**
 * This is the main class used to run the program. All steps have been detailed below on how to run it.
 */
public final class App {
    /**
     * Determines if the application will be cloud deployment or not.
     */
    private static final boolean CLOUD_DEPLOYED = true; // FOR NOW THE CLOUD DEPLOYMENT IS ONLY POSSIBLE

    /**
     * The location of the node red application json file.
     */
    private static final String NODE_RED_APPLICATION_JSON = "";

    /**
     * The location of the folder that contains the things used in the application.
     */
    private static final String THINGS_REPO = "";

    /**
     * The location of virtual device configuration file.
     */
    private static final String VDS_CONFIG_FILE = "";

    /**
     * The location of thing quantities file.
     */
    private static final String THING_QUANTITIES_FILE = "";

    /**
     * The preset used to configure the cloud node.
     */
    private static final CloudNodePreset CLOUD_NODE_PRESET = null;

    /**
     * The presets used to configure all edge nodes.
     */
    private static final EdgeNodePreset EDGE_NODE_PRESET = null;

    /**
     * The presets used to configure the application.
     */
    private static final ApplicationPreset APPLICATION_PRESET = null;

    /**
     * The number of cloud users that interact with the cloud.
     */
    private static final int NUM_USERS = 1;

    /**
     * The presets used to configure any fog device.
     */
    private static final FogDevicePreset FOG_DEVICE_PRESET = null;

    /**
     * The presets used to configure a sensor.
     */
    private static final SensorPreset SENSOR_PRESET = null;

    /**
     * The presets used to configure an actuator.
     */
    private static final ActuatorPreset ACTUATOR_PRESET = null;

    /**
     * The main method associated to this class, which runs the program
     * @param args Any arguments for the application (NONE)
     */
    public static void main(String[] args) {
        try{
            // Step 1: Set up the application and provide all the required arguments
            Simulation.setupApplication(
                    CLOUD_DEPLOYED,
                    NODE_RED_APPLICATION_JSON,
                    THINGS_REPO,
                    VDS_CONFIG_FILE,
                    THING_QUANTITIES_FILE,
                    CLOUD_NODE_PRESET,
                    EDGE_NODE_PRESET,
                    APPLICATION_PRESET,
                    NUM_USERS,
                    FOG_DEVICE_PRESET,
                    SENSOR_PRESET,
                    ACTUATOR_PRESET
            );

            // Step 2: Register any custom performance metrics, you have implemented
            Simulation.registerCustomMetric(new MeanTupleExecutionDelay()); // Example implementation provided in custom metrics folder

            // Step 3: Run the simulation
            Simulation.run();

        } catch(IOException e) {
            //System.out.println(e.getMessage());
            Log.printLine(e.getLocalizedMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
