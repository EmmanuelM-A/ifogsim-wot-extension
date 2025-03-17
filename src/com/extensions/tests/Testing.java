package com.extensions.tests;

import com.extensions.custommetrics.metrics.MeanTupleExecutionDelay;
import com.extensions.simulation.Simulation;
import com.extensions.simulation.SimulationData;
import com.extensions.tests.helper.InputHandler;
import com.extensions.utils.presets.*;
import org.cloudbus.cloudsim.Log;

import java.io.IOException;
import java.util.Arrays;

/**
 * All testing done during my Testing & Evaluation stage happen in this folder.
 */
public class Testing {
    /**
     * Determines if the application will be cloud deployment or not.
     */
    private static final boolean CLOUD_DEPLOYED = true;

    //private static final InputHandler inputHandler = new InputHandler("industrial-equipment-maintenance-system.json", "src/com/extensions/tests/testing/industrialEquipmentMaintenanceSystem", true);
    //private static final InputHandler inputHandler = new InputHandler("remote-weather-station.json", "src/com/extensions/tests/testing/remoteWeatherStation", false);
    private static final InputHandler inputHandler = new InputHandler("smart-city-traffic-management.json", "src/com/extensions/tests/testing/SmartCityTrafficManagement", true);
    //private static final InputHandler inputHandler = new InputHandler("smart-home-temperature-control.json", "src/com/extensions/tests/testing/smartHomeTemperatureControl", false);
    //private static final InputHandler inputHandler = new InputHandler("smart-agriculture-greenhouse-monitoring.json", "src/com/extensions/tests/testing/smartAgricultureGreenhouseMonitoring", false);
    //private static final InputHandler inputHandler = new InputHandler("smart-hospital-management.json", "src/com/extensions/tests/testing/smartHospitalManagement", true);
    //private static final InputHandler inputHandler = new InputHandler("dcns-fog-application.json", "src/com/extensions/tests/testing/dcnsFog", true);

    /**
     * The preset used to configure the cloud node.
     */
    private static final CloudNodePreset CLOUD_NODE_PRESET = CloudNodePreset.DEFAULT;

    /**
     * The presets used to configure all edge nodes.
     */
    private static final EdgeNodePreset EDGE_NODE_PRESET = EdgeNodePreset.DEFAULT;

    /**
     * The presets used to configure the application.
     */
    private static final ApplicationPreset APPLICATION_PRESET = ApplicationPreset.DEFAULT;

    /**
     * The number of cloud users/components that interact with the cloud.
     */
    private static final int NUM_USERS = 1;

    /**
     * The presets used to configure any fog device.
     */
    private static final FogDevicePreset FOG_DEVICE_PRESET = FogDevicePreset.DEFAULT;

    /**
     * The presets used to configure a sensor.
     */
    private static final SensorPreset SENSOR_PRESET = SensorPreset.DEFAULT;

    /**
     * The presets used to configure an actuator.
     */
    private static final ActuatorPreset ACTUATOR_PRESET = ActuatorPreset.DEFAULT;

    /**
     * The main method associated to this class, which runs the program
     * @param args Any arguments for the application (NONE)
     */
    public static void main(String[] args) {
        try{
            // Step 1: Set up the application and provide all the required arguments
            Simulation.setupApplication(
                    CLOUD_DEPLOYED,
                    inputHandler.NODE_RED_APPLICATION_JSON,
                    inputHandler.THINGS_REPO,
                    inputHandler.VDS_CONFIG_FILE,
                    inputHandler.THINGS_QUANTITIES,
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
            Log.printLine("Simulation terminated!");
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
