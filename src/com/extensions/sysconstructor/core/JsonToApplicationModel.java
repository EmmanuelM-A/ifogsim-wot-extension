package com.extensions.sysconstructor.core;

import com.extensions.sysconstructor.eventdriver.EventDrivenApplication;
import com.extensions.sysconstructor.nodered.NodeRedJSONParser;
import com.extensions.sysconstructor.topology.*;
import com.extensions.utils.Utility;
import org.apache.commons.math3.util.Pair;
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

// TODO - COMPLETE THE INJECT NODE DATA FLOW
// TODO - CLEANUP CODE AND REMOVE REDUNDANT CODE
// TODO - FINALISE CLASSES AND ADD COMMENTS TO CODE

public class JsonToApplicationModel {
    private static ApplicationContext applicationContext;

    public static EventDrivenApplication createApplicationModel(String appId, int userId, ApplicationContext context) {
        // Set the global variable
        applicationContext = context;

        // Create the application instance
        EventDrivenApplication application = new EventDrivenApplication(appId, userId, applicationContext.applicationPreset);

        // Group sub flows into inject flows, data flows and event flows
        groupTopologyNodeTrees(applicationContext.topologyNodeTrees);

        // Create the application model for all inject flows
        for(TopologyNodeTree injectFlow : applicationContext.injectFlows) {
            createInjectFlow(application, injectFlow);
        }

        // Create the application model for all data flows
        for(TopologyNodeTree dataFlow : applicationContext.dataFlows) {
            createDataFlow(application, dataFlow);
        }

        // Set all the event flows for the application model
        for(TopologyNodeTree eventFlow : applicationContext.eventFlows) {
            createEventFlow(application, eventFlow);
        }

        // Create and set the application modules
        /*setApplicationModules(application);

        System.out.println("Created AppModules:");
        for(AppModule appModule : application.getModules()) {
            System.out.println(appModule.getName());
        }

        // Set the app edges between modules
        //setApplicationEdges(application);

        // Set the tuple mappings for modules
        setApplicationTupleMappings(application);

        // Set the app loops
        setApplicationLoops(application);

        Utility.printAppLoops(applicationContext.appLoops);

        // Set the application events
        //setApplicationEvents(application);*/


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
        //applicationContext.nodeModules.put("default-module", new NodeModule(application.getModuleByName("default-module")));

        // Client module
        application.addAppModule("client", 10);
        //applicationContext.nodeModules.put("client", new NodeModule(application.getModuleByName("client")));

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
                applicationContext.eventFlows.add(topologyNodeTree); // Keep track of sub flows that are event-driven
            } else if (hasInjectStart) {
                subFlowStartTypes.put(subFlowId, "inject");
                applicationContext.injectFlows.add(topologyNodeTree); // Keep track of sub flows that are inject-stimulated
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

                // If the node belongs to a data flow sub flow (sub flow id equals none)
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
                }
            }
        }
    }

    private static boolean groupTopologyNodeTrees(List<TopologyNodeTree> topologyNodeTrees) {
        // Process topology node trees (sub flows)
        for (TopologyNodeTree topologyNodeTree : topologyNodeTrees) {
            TopologyNode rootNode = topologyNodeTree.rootNode();

            if(rootNode == null) {
                System.out.println("In groupTopologyNodeTrees(), a root node has not been set!");
                return false;
            }

            if (rootNode.type().equals(NodeRedJSONParser.TYPE_SUBSCRIBE_EVENT)) {
                applicationContext.eventFlows.add(topologyNodeTree); // Keep track of sub flows that are event-driven
            } else if (rootNode.type().equals(NodeRedJSONParser.TYPE_INJECT)) {
                applicationContext.injectFlows.add(topologyNodeTree); // Keep track of sub flows that are inject-stimulated
            } else {
                applicationContext.dataFlows.add(topologyNodeTree); // Add to dataflow sub flows
            }
        }

        return true;
    }

    private static void createInjectFlow(EventDrivenApplication application, TopologyNodeTree injectFlow) {

    }

    private static void createDataFlow(EventDrivenApplication application, TopologyNodeTree dataFlow) {
        // Check if the data flow exists
        if(dataFlow == null) {
            return;
        }

        // Make sure the data flow is actually a data flow. If not return
        if(!dataFlow.rootNode().type().equals("inject") && !dataFlow.rootNode().type().equals("event")) {
            return;
        }

        // Checks if the data flow's branches list is set
        if(dataFlow.branches() == null || dataFlow.branches().isEmpty()) {
            return;
        }

        // Get the rootNode
        TopologyNode rootNode = dataFlow.rootNode();

        // Get branches
        List<List<TopologyNode>> branches = dataFlow.branches();

        // Check if the rootNode is a wot node, if so get its type
        if(!isWoTNode(rootNode)) {
            return;
        }

        // A count used to keep track of processing modules created
        int processingModuleCount = 0;

        // Used to keep track of all modules made and ensure no duplicate
        HashMap<String, ModuleMapping> moduleMappings = new HashMap<>();

        // Iterate through one branch at a time
        for(List<TopologyNode> branch : branches) {
            // Used to access nodes in a branch
            int ptr1 = 0, ptr2 = 1;

            // Helps keep track of data flow in and out of application model
            List<String> appLoopRoute = new ArrayList<>();

            while(ptr2 < branch.size()) {
                // Get the src node (Assuming it's a WoT node) and dst node (will be checked)
                TopologyNode src = branch.get(ptr1);

                TopologyNode dst = branch.get(ptr2);

                // Check if the dst node is a WoT node
                if(isWoTNode(dst)) {
                    // Check the connectivity between the src and dst nodes
                    TopologyNodeConnectionStatus connectionStatus = TopologyNodeConnectionChecker.areNodesConnected(src.id(), dst.id());

                    if(connectionStatus.isThereAConnection()) {
                        if(connectionStatus.isDirectionConnection()) {
                            // Handle direct connections here
                        }

                        //else {
                            // Handle indirect connection here

                            // Create a processing module (represents an intermediately processing unit for data)
                            String processingModule = "processingModule-" + processingModuleCount;
                            application.addAppModule(processingModule, applicationContext.applicationPreset.APP_MODULE_RAM);

                            // Get the property/action name of the src and dst node
                            String srcName = src.uniqueAttribute();
                            String dstName = dst.uniqueAttribute();

                            // Create the app edges from src ----> PrM ----> dst
                            // Src ----> PrM
                            String tupleTypeOne = determineTupleType(src);
                            int edgeDirection1 = determineDirection(src);
                            int edgeType1 = determineEdgeType(src);
                            addAppEdge(application, srcName, processingModule, tupleTypeOne, edgeDirection1, edgeType1);

                            // PrM ----> dst
                            String tupleTypeTwo = determineTupleType(dst);
                            int edgeDirection2 = determineDirection(dst);
                            int edgeType2 = determineEdgeType(dst);
                            addAppEdge(application, processingModule, dstName, tupleTypeTwo, edgeDirection2, edgeType2);

                            // Record tuple mappings between modules
                            if(moduleMappings.containsKey(srcName)) {
                                ModuleMapping mapping = moduleMappings.get(srcName);

                                if(mapping.getOutputTupleType() == null) mapping.setOutputTupleType(tupleTypeOne);

                            } else {
                                moduleMappings.put(srcName, new ModuleMapping());
                            }

                            if(moduleMappings.containsKey(dstName)) {
                                ModuleMapping mapping = moduleMappings.get(dstName);

                                if(mapping.getInputTupleType() == null) mapping.setInputTupleType(tupleTypeTwo);

                            } else {
                                moduleMappings.put(dstName, new ModuleMapping());
                            }

                            if(moduleMappings.containsKey(processingModule)) {
                                ModuleMapping mapping = moduleMappings.get(processingModule);

                                mapping.setInputTupleType(tupleTypeOne);
                                mapping.setOutputTupleType(tupleTypeTwo);

                            } else {
                                moduleMappings.put(processingModule, new ModuleMapping());
                            }

                            // Record the route of the data flow
                            if(appLoopRoute.isEmpty()) {
                                // First time, start with an empty list
                                appLoopRoute.add(srcName);
                                appLoopRoute.add(processingModule);
                                appLoopRoute.add(dstName);
                            } else {
                                // Subsequent times, extend the appLoopRoute
                                appLoopRoute.add(processingModule);
                                appLoopRoute.add(dstName);
                            }

                            applicationContext.appLoops.add(appLoopRoute);

                            processingModuleCount++;


                        //}
                    }

                }

            }
        }

        // Set tuple mappings
        for(Map.Entry<String, ModuleMapping> moduleMapping : moduleMappings.entrySet()) {
            String moduleName = moduleMapping.getKey();
            ModuleMapping mapping = moduleMapping.getValue();

            // Only set tuple mapping if for that module, its input and output tuple types are both set
            if(mapping.getInputTupleType() != null && mapping.getOutputTupleType() != null) {
                application.addTupleMapping(
                        moduleName,
                        mapping.getInputTupleType(),
                        mapping.getOutputTupleType(),
                        new FractionalSelectivity(1.0) // CHANGE AT YOUR DISCRETION
                        );
            }
        }
    }

    private static void createEventFlow(EventDrivenApplication application, TopologyNodeTree eventFlow) {

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
                            addAppEdge(application, srcModule.getName(), dstModule.getName(), src, dst);
                            //addAppEdge(application, dfm, dstModule.getName(), src, dst);

                            /*
                            * If src is directly connected to dst => edge) src ---> dst
                            * If there exists some nodes between src and dst (not directly connected) => edge) src ---> Pr --->  dst
                            * record all appEdges
                            * record all tuple flows through modules
                            */

                            // TODO - Connect dfm to modules when there existing a node between src and dst. If src node is right next to dst node, no dfm needed

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

                                //newRoute.removeLast(); // Remove the last element

                                //newRoute.add(srcModule.getName());
                                //newRoute.add(dfm);
                                //newRoute.add(dstModule.getName());

                                // TODO - FIX THIS / OUTPUT INCORRECT

                                newRoute.add(dstModule.getName()); // Add the new destination module

                                applicationContext.appLoops.add(newRoute); // Add the extended route
                            }

                            // Move ptr1 to the next valid node
                            ptr1 = ptr2;
                            ptr2++;
                        } else {
                            ptr2++;
                        }
                    }

                    // Empty route list for new branch
                    route.clear();
                }
            }
        }
    }

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

    private static boolean isWoTNode(TopologyNode node) {
        return node.thing() != null && !node.thing().isEmpty();
    }


    private static void addAppEdge(EventDrivenApplication application, String srcModuleName, String dstModuleName, String tupleType, int edgeDirection, int edgeType) {
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

    private static String determineTupleType(TopologyNode node) {
        return node.uniqueAttribute();
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
