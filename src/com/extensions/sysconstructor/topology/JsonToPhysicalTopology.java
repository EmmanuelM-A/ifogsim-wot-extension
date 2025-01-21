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
        * INJECT - REPRESENTS DATA FLOW ONLY
        * READ-PROP =
        * INVOKE-ACTION, WRITE-PROP = ACTUATORS
        * */

        try {
            // Parse the application topology json
            ApplicationTopologyParser applicationTopologyParser = new ApplicationTopologyParser(new File(applicationTopology));

            List<TopologyNode> topologyNodes = applicationTopologyParser.parseTopologyNodes("nodes");

            List<TopologyNodeConnection> topologyNodeConnections = applicationTopologyParser.parseTopologyConnections();

            System.out.println("Application Topology Parsed Successfully!");


        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
