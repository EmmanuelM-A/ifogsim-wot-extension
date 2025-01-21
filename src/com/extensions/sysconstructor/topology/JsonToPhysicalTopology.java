package com.extensions.sysconstructor.topology;

import com.extensions.sysconstructor.core.ApplicationPhysicalTopology;
import com.extensions.sysconstructor.nodered.NodeRedTranslator;
import com.extensions.utils.FilePaths;
import com.extensions.vdcreation.core.VirtualDevice;
import org.fog.application.Application;
import org.fog.utils.JsonToTopology;

import java.io.File;
import java.util.List;

/*
 * Parse the application topology json
 * Get the VDs
 * Connect devices according to the topology JSON
 * INJECT, SUB-EVENTS = REPRESENTS DATA FLOW ONLY (IGNORE)
 * READ-PROP = SENSORS
 * INVOKE-ACTION, WRITE-PROP = ACTUATORS
 * READ-PROP search for
 * */

public class JsonToPhysicalTopology extends JsonToTopology {
    public static ApplicationPhysicalTopology createPhysicalTopology(int userId, String appId, File nodeRedApplicationJsonFile, List<VirtualDevice> virtualDevices) {
        try {
            // Generate the application topology from the node red application design
            NodeRedTranslator.nodeRedToInputJson(nodeRedApplicationJsonFile);

            // Parse the application topology json
            ApplicationTopologyParser applicationTopologyParser = new ApplicationTopologyParser(new File(FilePaths.APPLICATION_TOPOLOGY));

            // Extract application details (Title)
            //ApplicationDetails applicationDetails;

            // Extract all the topology nodes
            List<TopologyNode> topologyNodes = applicationTopologyParser.parseTopologyNodes("nodes");

            // Extract all the connections between nodes
            List<TopologyNodeConnection> topologyNodeConnections = applicationTopologyParser.parseTopologyConnections();

            System.out.println("Application Topology Parsed Successfully!");

            System.out.println(topologyNodes.getFirst().toString());

            // Create the cloud node

            // Create the edge nodes





        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) {
        //createPhysicalTopology(null, null, FilePaths.NODE_RED_APPLICATION_JSON)
    }

    public static Application createApplicationDataMappings() {
        return null;
    }
}
