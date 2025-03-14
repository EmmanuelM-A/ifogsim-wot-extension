package com.extensions.sysconstructor.core;

import com.extensions.vdcreation.core.VirtualDevice;
import org.fog.application.Application;

import java.util.List;

/**
 * Responsible constructing the iFogSim application.
 */
public interface ApplicationConstructor {
    /**
     * Creates the application model for the application.
     * @param appId The application ID.
     * @param userId The user ID associated with the application.
     *
     * @return The constructed application model from the Node-RED application.
     */
    Application createApplicationModel(String appId, int userId) throws Exception;

    /**
     * Constructs the application's physical topology given the list of virtual devices used in the application.
     * @param virtualDevices The list of all virtual devices used in the application.
     * @return The psychical topology instance which details all sensors, actuators and fog devices used.
     */
    ApplicationPhysicalTopology createApplicationPhysicalTopology(List<VirtualDevice> virtualDevices);
}
