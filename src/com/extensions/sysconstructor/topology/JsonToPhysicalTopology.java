package com.extensions.sysconstructor.topology;

import com.extensions.customfog.FogDeviceFactory;
import com.extensions.sysconstructor.core.ApplicationPhysicalTopology;
import com.extensions.sysconstructor.nodered.NodeRedTranslator;
import com.extensions.utils.FilePaths;
import com.extensions.vdcreation.core.VirtualDevice;
import org.fog.application.Application;
import org.fog.entities.FogDevice;
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

public class JsonToPhysicalTopology {
    public static ApplicationPhysicalTopology createPhysicalTopology(int userId, String appId, File nodeRedApplicationJsonFile, List<VirtualDevice> virtualDevices) {
        ApplicationPhysicalTopology applicationPhysicalTopology = new ApplicationPhysicalTopology();
        try {
            // Generate the application topology from the node red application design
            NodeRedTranslator.nodeRedToInputJson(nodeRedApplicationJsonFile);

            // Parse the application topology json
            ApplicationTopologyParser applicationTopologyParser = new ApplicationTopologyParser(new File(FilePaths.APPLICATION_TOPOLOGY));

            // Extract all the topology nodes
            List<TopologyNode> topologyNodes = applicationTopologyParser.parseTopologyNodes("nodes");

            // Extract all the connections between nodes
            List<TopologyNodeConnection> topologyNodeConnections = applicationTopologyParser.parseTopologyConnections();

            System.out.println("Application Topology Parsed Successfully!");

            //System.out.println(topologyNodes.getFirst().toString()); // TODO COME BACK TO THIS

            // Create the cloud device at the top of the hierarchy
            //FogDevice cloud = FogDeviceFactory.createFogDevice("cloud", 50000, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25);

            // Cloud has no parent, it is the root of the hierarchy
            //cloud.setParentId(-1);



            // Create the edge nodes





        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) {
        createPhysicalTopology(
                0,
                null,
                new File("src/com/extensions/input/application/door-security-application.json"),
                null
        );
    }

    public static Application createApplicationDataMappings() {
        return null;
    }
}
