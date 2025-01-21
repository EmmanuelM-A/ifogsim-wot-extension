package com.extensions.sysconstructor.topology;

import com.extensions.sysconstructor.core.ApplicationPhysicalTopology;
import com.extensions.utils.FilePaths;
import com.extensions.vdcreation.core.VirtualDevice;
import org.fog.utils.JsonToTopology;

import java.io.File;
import java.util.List;

public class JsonToPhysicalTopology extends JsonToTopology {
    public static ApplicationPhysicalTopology createPhysicalTopology(int userId, String appId, String applicationTopology, List<VirtualDevice> virtualDevices) {
        /*
        * Parse the application topology json
        * Get the VDs
        * Connect devices according to the topology JSON
        * INJECT, SUB-EVENTS = REPRESENTS DATA FLOW ONLY (IGNORE)
        * READ-PROP = SENSORS
        * INVOKE-ACTION, WRITE-PROP = ACTUATORS
        * READ-PROP search for
        * */

        try {
            // Parse the application topology json
            ApplicationTopologyParser applicationTopologyParser = new ApplicationTopologyParser(new File(applicationTopology));

            List<TopologyNode> topologyNodes = applicationTopologyParser.parseTopologyNodes("nodes");

            List<TopologyNodeConnection> topologyNodeConnections = applicationTopologyParser.parseTopologyConnections();

            System.out.println("Application Topology Parsed Successfully!");

            // Create the cloud node

            // Create the edge nodes





        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
