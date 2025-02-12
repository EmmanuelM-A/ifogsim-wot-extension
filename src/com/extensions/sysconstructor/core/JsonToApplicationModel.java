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

// TODO - COMPLETE THE INJECT NODE DATA FLOW
// TODO - FINALISE CLASSES AND ADD COMMENTS TO CODE

public class JsonToApplicationModel {
    private static ApplicationContext applicationContext;

    public static EventDrivenApplication createApplicationModel(String appId, int userId, ApplicationContext context) {
        // Set the global variable
        applicationContext = context;

        // Create the application instance
        EventDrivenApplication application = new EventDrivenApplication(appId, userId, applicationContext.applicationPreset);

        // Group sub flows into inject flows, data flows and event flows
        boolean success = groupTopologyNodeTrees(applicationContext.topologyNodeTrees);

        if(!success) return null;

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

        // Set application loops
        setApplicationLoops(application);

        System.out.println("Application Model formed!");

        return application;
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

    /**
     * Creates the necessary app modules, app edges, tuple mappings for app modules and app loops to simulate user data
     * entering an application.
     * @param application The application instance
     * @param injectFlow The sub flow that represents user injected data (user-based data)
     */
    private static void createInjectFlow(EventDrivenApplication application, TopologyNodeTree injectFlow) {
        // Check if the inject flow actually exists
        if(injectFlow == null) {
            return;
        }

        // Make sure the inject flow is actually an inject  flow. If not return
        if(!injectFlow.rootNode().type().equals(NodeRedJSONParser.TYPE_INJECT)) {
            return;
        }

        // Checks if the inject flow's branches list is set
        if(injectFlow.branches() == null || injectFlow.branches().isEmpty()) {
            return;
        }

        int clientModuleCount = 0;

        // Get root node
        TopologyNode rootNode = injectFlow.rootNode();

        // Get branches
        List<List<TopologyNode>> branches = injectFlow.branches();

        // Used to keep track of all modules made and ensure no duplicate
        HashMap<String, ModuleMapping> moduleTupleMappings = new HashMap<>();

        // Create the client module
        String clientModule = "CLIENT_MODULE";
        application.addAppModule(clientModule, applicationContext.applicationPreset.APP_MODULE_RAM);

        // Iterate through one branch at a time
        for(List<TopologyNode> branch : branches) {
            // Helps keep track of data flow in and out of application model
            List<String> appLoopRoute = new ArrayList<>();

            for(int index = 0; index < branch.size(); index++) {
                // Get node at index
                TopologyNode node = branch.get(index);

                // Check if the node is a WoT node
                if(isWoTNode(node)) {
                    TopologyNodeConnectionStatus connectionStatus = TopologyNodeConnectionChecker.areNodesConnected(rootNode.id(), node.id());

                    // Only care if there exists some connection whether its direct or indirect
                    if(connectionStatus.isThereAConnection()) {
                        // Create the src
                        String srcName = node.uniqueAttribute();

                        // Create the app edge from src ----> client
                        String tupleTypeOne = determineTupleType(node);
                        int edgeDirection1 = determineDirection(node);
                        int edgeType1 = determineEdgeType(node);
                        addAppEdge(application, srcName, clientModule, tupleTypeOne, edgeDirection1, edgeType1);
                    }
                }
            }
        }

        // TODO CONSIDER ITS IMPORTANCE - IS IT ACTUALLY NEEDED - MUST I MAP USER DATA FLOW IN THE APPLICATION

        // Set tuple mappings
        for(Map.Entry<String, ModuleMapping> moduleMapping : moduleTupleMappings.entrySet()) {
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

    /**
     * Creates the necessary app modules, app edges, tuple mappings for app modules and app loops for a given
     * data flow sub flow.
     * @param application The application instance
     * @param dataFlow The sub flow which is a data flow
     */
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
                            // HANDLE DIRECT CONNECTION HERE
                        }

                        //else {
                            // HANDLE INDIRECT CONNECTION HERE

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

                            // Move to the next position
                            ptr1 = ptr2;
                            ptr2++;


                        //}
                    }
                } else {
                    // Move to the node
                    ptr2++;
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
        // APPLICATION MODEL THE EXACT SAME AS THE createDataFlow()
        // Tuple types start with EVENT_TUPLE_....
        // Processing modules are EVENT_PROCESSING_MODULE-....

        // EXTEND FOG DEVICES, SENSORS, ACTUATORS AND IFOGSIM SIMULATION CLASSES TO HANDLE TUPLE TYPES OF EVENT
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
                            //addAppEdge(application, srcModule.getName(), dstModule.getName(), src, dst);
                            //addAppEdge(application, dfm, dstModule.getName(), src, dst);

                            /*
                            * If src is directly connected to dst => edge) src ---> dst
                            * If there exists some nodes between src and dst (not directly connected) => edge) src ---> Pr --->  dst
                            * record all appEdges
                            * record all tuple flows through modules
                            */

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

    private static void setApplicationLoops(EventDrivenApplication application) {
        // Generate the loops
        List<AppLoop> appLoops = new ArrayList<>();

        for(List<String> loop : applicationContext.appLoops) {
            AppLoop appLoop = new AppLoop(loop);

            appLoops.add(appLoop);
        }

        // Assign loops to the application
        application.setLoops(appLoops);
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
