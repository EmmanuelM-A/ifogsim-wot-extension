package com.extensions.sysconstructor.core;

import com.extensions.sysconstructor.eventdriver.EventDrivenApplication;
import com.extensions.sysconstructor.nodered.NodeRedJSONParser;
import com.extensions.sysconstructor.topology.*;
import com.extensions.utils.Utility;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Tuple;

import java.util.*;

/*
 * Parse the application topology json
 * Get the VDs
 * Connect devices according to the topology JSON
 * INJECT, SUB-EVENTS = REPRESENTS DATA FLOW ONLY (IGNORE)
 * READ-PROP = SENSORS
 * INVOKE-ACTION, WRITE-PROP = ACTUATORS
 * READ-PROP search for
 * */

public class JsonToApplicationModel {
    private static ApplicationContext applicationContext;

    public static EventDrivenApplication createApplicationModel(String appId, int userId, ApplicationContext context) {
        // Set the global variable
        applicationContext = context;

        // Create the application instance
        EventDrivenApplication application = new EventDrivenApplication(appId, userId, applicationContext.applicationPreset);

        // Create and set the application modules
        setApplicationModules(application);

        System.out.println("Created AppModules:");
        for(AppModule appModule : application.getModules()) {
            System.out.println(appModule.getName());
        }

        // Set the app edges between modules
        setApplicationEdges(application);

        // Set the tuple mappings for modules
        setApplicationTupleMappings(application);

        // Set the app loops
        setApplicationLoops(application);

        Utility.printAppLoops(applicationContext.appLoops);

        // Set the application events
        //setApplicationEvents(application);


        // Create modules:
        // - DefaultModule module represents an imaginary module that handles some undefined processing or data transmission to other modules.
        //
        // - client module that represents user input or user-based tuple transmission
        //
        // - Modules for any read-prop, write-prop or invoke-action node given the sub-flow does not start with a sub-event node
        //
        // Add app edges
        // - Modules connected based on node-red connections, so if a read-prop node is connected (directly or indirectly) to a write-prop, their
        // module counterparts will be connected as such.
        //      - The tuple type is the name of the property of the read-prop so the Property: lockState would be TUPLE_LOCK_STATE
        //      - Direction: determineDirection(src, dst)
        //      - edgeType: props as sensors and actions as actuators, everything else is a module
        //      - keep track of module connections and module inputs in a D.S (ModuleConnections)
        //
        // Define tuple mappings:
        // - use ModuleConnection class
        //
        //
        // Define app loops
        // - createAppLoops()
        //
        //
        // Define events => setApplicationEvents() // Will handle all the event setting up and configurations to allow for event transmission and processing
        // - Extend fogDevices to handle events
        // - The fog node emits a tuple type of the event-node to all other fog nodes which will process it and act accordingly
        // - Sensors and actuators (stored in VD) will process incoming event_tuples and emit tuples of their own type, so the invoke-action
        // node: activateAlarm() will emit a tuple type of TUPLE_activateAlarm to some endpoint, after receiving a tuple of type EVENT_TUPLE_tamperAlert
        // - Define event tuple flows transmission Map<Event_tuple, List<Nodes> components (like sensors or actuators)> and a response would the node
        // emitting a tuple to some destination.
        // - extend controller or application
        //
        // */

        System.out.println("Application Model formed!");


        return application;
    }

    private static void setApplicationModules(EventDrivenApplication application) {
        // Default module
        application.addAppModule("default-module", 10);
        applicationContext.nodeModules.put("default-module", new NodeModule(application.getModuleByName("default-module")));

        // Client module
        application.addAppModule("client", 10);
        applicationContext.nodeModules.put("client", new NodeModule(application.getModuleByName("client")));

        // Define node types to process
        List<String> nodeTypesToSearchFor = List.of("read-property", "invoke-action");

        // Map to track the start types of each sub flow
        Map<String, String> subFlowStartTypes = new HashMap<>();

        // Process topology node trees (sub flows)
        for (TopologyNodeTree topologyNodeTree : applicationContext.topologyNodeTrees) {
            TopologyNode rootNode = topologyNodeTree.rootNode();
            List<List<TopologyNode>> branches = topologyNodeTree.branches();

            String subFlowId = rootNode.id(); // Use root node ID as sub flow identifier
            boolean hasEventStart = false;
            boolean hasInjectStart = false;

            List<TopologyNode> subFlowNodes = new ArrayList<>();

            // Collect all nodes in the sub flow
            for (List<TopologyNode> branch : branches) {
                subFlowNodes.addAll(branch);
            }

            // Determine sub flow type
            for (TopologyNode node : subFlowNodes) {
                if (node.type().equals(NodeRedJSONParser.TYPE_SUBSCRIBE_EVENT)) {
                    hasEventStart = true;
                    break;
                } else if (node.type().equals(NodeRedJSONParser.TYPE_INJECT)) {
                    hasInjectStart = true;
                }
            }

            if (hasEventStart) {
                subFlowStartTypes.put(subFlowId, "event");
            } else if (hasInjectStart) {
                subFlowStartTypes.put(subFlowId, "inject");
                applicationContext.dataFlows.add(topologyNodeTree); // Add to dataflow sub flows
            } else {
                subFlowStartTypes.put(subFlowId, "none");
                applicationContext.dataFlows.add(topologyNodeTree); // Add to dataflow sub flows
            }
        }

        List<TopologyNode> allNodes = Utility.getAllNodesFromTopology(applicationContext.topologyNodeTrees);

        // Used to keep track of nodes that will be converted to modules, to ensure not duplicate nodes are converted
        List<String> nodeIds = new ArrayList<>();

        // Process WoT nodes
        for (String nodeType : nodeTypesToSearchFor) {
            List<TopologyNode> nodes = Utility.getTopologyNodesByType(allNodes, nodeType);

            for (TopologyNode node : nodes) {
                String subFlowId = node.subFlowId(); // Get the sub flow the node belongs to

                // Accept if:
                // - It belongs to a sub flow that has no event or inject node
                if (subFlowStartTypes.getOrDefault(subFlowId, "event").equals("none")) {
                    if(!nodeIds.contains(node.id())) {
                        String moduleName = node.uniqueAttribute() + "_" + node.id(); // Append ID to ensure uniqueness
                        application.addAppModule(moduleName, 10);

                        // Store the app modules created <ID, MODULE>
                        applicationContext.appModulesCreated.put(node.id(), application.getModuleByName(moduleName));

                        nodeIds.add(node.id());
                    } else {
                        //System.out.println("Skipping node: " + node.uniqueAttribute() + " (node already added)");
                    }
                } else {
                    //System.out.println("Skipping node: " + node.uniqueAttribute() + " (not a data flow)");
                }
            }
        }
    }

    private static void setApplicationEdges(EventDrivenApplication application) {
        // Default module
        String dfm = application.getModuleByName("default-module").getName();

        List<String> route = new ArrayList<>();

        for (TopologyNodeTree dataFlow : applicationContext.dataFlows) {
            // Get root node
            TopologyNode rootNode = dataFlow.rootNode();

            // Get branches
            List<List<TopologyNode>> branches = dataFlow.branches();

            // If the data flow is NOT event-driven and does NOT start with an inject node
            if (!rootNode.type().equals(NodeRedJSONParser.TYPE_SUBSCRIBE_EVENT) && !rootNode.type().equals(NodeRedJSONParser.TYPE_INJECT)) {
                // Used to access the nodes in a branch
                int ptr1 = 0, ptr2 = 1;

                // Iterate through one branch at a time
                for(List<TopologyNode> branch : branches) {
                    while(ptr2 < branch.size()) {
                        // Get the src node (Assuming it's a WoT node)
                        TopologyNode src = branch.get(ptr1);

                        // Get the dst node, checking if it's a WoT node
                        if (isWoTNode(branch.get(ptr2))) {
                            TopologyNode dst = branch.get(ptr2);

                            // Retrieve module names, ensuring they exist
                            AppModule srcModule = applicationContext.appModulesCreated.get(src.id());
                            AppModule dstModule = applicationContext.appModulesCreated.get(dst.id());

                            if (srcModule == null || dstModule == null) {
                                System.out.println("Skipping edge: Missing module for Src: " + src.id() + ", Dst: " + dst.id());
                                ptr2++;  // Continue checking next nodes
                                continue;
                            }

                            // Create an app edge from src ---> dfm ---> dst
                            addAppEdge(application, srcModule.getName(), dfm, src, dst);
                            addAppEdge(application, dfm, dstModule.getName(), src, dst);

                            // Record the connection
                            applicationContext.appEdges.put(src.id(), dst.id());

                            // Set the route of the data flow (assuming some processing occurs at a dfm before heading to dst)
                            if(applicationContext.appLoops.isEmpty()) {
                                // First time, start with an empty list
                                route.add(srcModule.getName());
                                route.add(dfm);
                                route.add(dstModule.getName());
                                applicationContext.appLoops.add(route);
                            } else {
                                // Subsequent times, extend the LAST route in appLoops
                                List<String> lastRoute = applicationContext.appLoops.getLast(); // Get the last route

                                List<String> newRoute = new ArrayList<>(lastRoute); // Create a *copy*

                                newRoute.removeLast(); // Remove the last element

                                newRoute.add(srcModule.getName());
                                newRoute.add(dfm);
                                newRoute.add(dstModule.getName());

                                //newRoute.add(dstModule.getName()); // Add the new destination module

                                applicationContext.appLoops.add(newRoute); // Add the extended route
                            }

                            // Move ptr1 to the next valid node
                            ptr1 = ptr2;
                            ptr2++;
                        } else {
                            ptr2++;
                        }
                    }
                }
            }
        }
    }

    private static boolean isWoTNode(TopologyNode node) {
        return node.thing() != null && !node.thing().isEmpty();
    }


    private static void addAppEdge(EventDrivenApplication application, String srcModuleName, String dstModuleName, TopologyNode srcNode, TopologyNode dstNode) {
        String tupleType = null;
        int edgeDirection = 0;
        int edgeType = 0;

        if(srcModuleName.equals("default-module")) { // dfm ---> dst
            tupleType = determineTupleType(srcNode); // Same as src
            edgeDirection = determineDirection(dstNode); //
            edgeType = determineEdgeType(dstNode);
        } else {
            tupleType = determineTupleType(srcNode);
            edgeDirection = determineDirection(srcNode);
            edgeType = determineEdgeType(srcNode);
        }

        application.addAppEdge(
                srcModuleName,
                dstModuleName,
                applicationContext.applicationPreset.APP_EDGE_TUPLE_CPU_LENGTH,  // Processing latency
                applicationContext.applicationPreset.APP_EDGE_TUPLE_NW_LENGTH,   // Transmission latency
                tupleType,
                edgeDirection,
                edgeType
        );

        System.out.println("Connected: " + srcModuleName + " --> " + dstModuleName);
    }

    // TODO - Connect dfm to modules when there existing a node between src and dst. If src node is right next to dst node, no dfm needed

    private static void setApplicationTupleMappings(EventDrivenApplication application) {
        Map<String, Map<String, TupleMapping>> tupleMappings = new HashMap<>();

        List<TopologyNode> allNodes = Utility.getAllNodesFromTopology(applicationContext.topologyNodeTrees);

        for (Map.Entry<String, String> edgeEntry : applicationContext.appEdges.entrySet()) {
            String srcNodeId = edgeEntry.getKey();
            String dstNodeId = edgeEntry.getValue();

            AppModule srcModule = applicationContext.appModulesCreated.get(srcNodeId);
            AppModule dstModule = applicationContext.appModulesCreated.get(dstNodeId);

            if (srcModule != null && dstModule != null) {
                TopologyNode srcNode = Utility.findTopologyNodeBy("id", srcNodeId, allNodes);  // Find node by ID
                TopologyNode dstNode = Utility.findTopologyNodeBy("id", dstNodeId, allNodes);

                if (srcNode != null && dstNode != null) {
                    String srcTupleType = determineTupleType(srcNode);
                    String dstTupleType = determineTupleType(dstNode);

                    if (srcTupleType != null && dstTupleType != null) {
                        if (!tupleMappings.containsKey(srcTupleType)) {
                            tupleMappings.put(srcTupleType, new HashMap<>());
                        }
                        TupleMapping mapping = new TupleMapping(srcModule.getName(), srcTupleType, dstTupleType, new FractionalSelectivity(1.0)); // Create mapping here
                        tupleMappings.get(srcTupleType).put(dstTupleType, mapping);

                        // Print the mapping as it's created:
                        System.out.println("Tuple Mapping Created: " + mapping); // Or a more formatted output

                        application.addTupleMapping(mapping.module(), mapping.inputTupleType(), mapping.outputTupleType(), mapping.selectivity()); // Add to application immediately
                    }
                }
            }
        }
    }

    private static void setApplicationLoops(EventDrivenApplication application) {
        // Generate the loops
        List<AppLoop> appLoops = generateAppLoops(applicationContext.appLoops);

        // Assign loops to the application
        application.setLoops(appLoops);
    }

    private static void setApplicationEvents(EventDrivenApplication application) {

    }

    private static List<AppLoop> generateAppLoops(List<List<String>> loops) {
        List<AppLoop> appLoops = new ArrayList<>();

        for(List<String> loop : loops) {
            AppLoop appLoop = new AppLoop(loop);

            appLoops.add(appLoop);
        }

        return appLoops;
    }

    private static String determineTupleType(TopologyNode node) {
        if (node.type().equals("read-property")) {
            return node.uniqueAttribute();  // Use property name as tuple
        } else if (node.type().equals("invoke-action") || node.type().equals("write-property")) {
            return node.uniqueAttribute();  // Action-related tuple
        } else {
            return node.uniqueAttribute();  // Generic processing tuple
        }
    }

    private static int determineDirection(TopologyNode node) {
        // Assume upward tuple flow for sensors and downward for actuators
        if (node.type().equals(NodeRedJSONParser.TYPE_READ_PROPERTY)) {
            return Tuple.UP;
        } else if (node.type().equals(NodeRedJSONParser.TYPE_INVOKE_ACTION) || node.type().equals(NodeRedJSONParser.TYPE_WRITE_PROPERTY)) {
            return Tuple.DOWN;
        } else {
            return Tuple.UP;  // Default for other module connections
        }
    }

    private static int determineEdgeType(TopologyNode node) {
        if (node.type().equals(NodeRedJSONParser.TYPE_READ_PROPERTY)) {
            return AppEdge.SENSOR;
        } else if (node.type().equals(NodeRedJSONParser.TYPE_INVOKE_ACTION) || node.type().equals(NodeRedJSONParser.TYPE_WRITE_PROPERTY)) {
            return AppEdge.ACTUATOR;
        } else {
            return AppEdge.MODULE;
        }
    }

}
