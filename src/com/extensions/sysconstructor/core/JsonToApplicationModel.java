package com.extensions.sysconstructor.core;

import com.extensions.customfog.CustomFogDevice;
import com.extensions.sysconstructor.eventdriver.EventDrivenApplication;
import com.extensions.sysconstructor.eventdriver.EventManager;
import com.extensions.sysconstructor.nodered.NodeRedJSONParser;
import com.extensions.sysconstructor.topology.*;
import com.extensions.utils.FogEntityPrefixes;
import com.extensions.utils.Utility;
import com.extensions.vdcreation.core.VirtualDevice;
import org.apache.commons.math3.util.Pair;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.application.selectivity.SelectivityModel;
import org.fog.entities.Tuple;

import java.util.*;

// TODO - COMPLETE THE INJECT NODE DATA FLOW
// TODO - FINALISE CLASSES AND ADD COMMENTS TO CODE

public class JsonToApplicationModel {
    /**
     * Contains all the information needed to run the application
     */
    private static ApplicationContext applicationContext;

    /**
     * A count used to keep track of all processing modules created
     */
    private static int processingModuleCount = 0;

    /**
     * A count used to keep track of all event processing modules created
     */
    private static int eventProcessingModuleCount = 0;

    public static EventDrivenApplication createApplicationModel(String appId, int userId, ApplicationContext context) {
        // Set the global variable
        applicationContext = context;

        // Create the application instance
        EventDrivenApplication application = new EventDrivenApplication(appId, userId, applicationContext.applicationPreset);

        // Set appLoops - IMPORTANT DO NOT CHANGE
        application.setLoops(applicationContext.appLoops);

        // Group sub flows into inject flows, data flows and event flows
        boolean success = groupTopologyNodeTrees(applicationContext.topologyNodeTrees);

        if(!success) return null;

        // Create the application model for all inject flows
        /*for(TopologyNodeTree injectFlow : applicationContext.injectFlows) {
            createInjectFlow(application, injectFlow);
        }*/

        // Create the application model for all data flows
        for(TopologyNodeTree dataFlow : applicationContext.dataFlows) {
            createDataFlow(application, dataFlow);
        }

        // Set all the event flows for the application model
        /*for(TopologyNodeTree eventFlow : applicationContext.eventFlows) {
            createEventFlow(application, eventFlow);
        }*/

        // Print app modules
        System.out.println("AppModules:");
        System.out.println("--------------------------------------");
        for(AppModule appModule : application.getModules()) {
            System.out.println(appModule.getName());
        }
        System.out.println();

        // Print app edges
        System.out.println("AppEdges:");
        System.out.println("---------------------------------------");
        for(AppEdge appEdge : application.getEdges()) {
            System.out.println(appEdge);
        }
        System.out.println();

        // print tuple mappings
        System.out.println("Tuple Mappings:");
        System.out.println("---------------------------------------");
        for (AppModule appModule : application.getModules()) {
            for (Map.Entry<Pair<String, String>, SelectivityModel> entry : appModule.getSelectivityMap().entrySet()) {
                Pair<String, String> key = entry.getKey();
                SelectivityModel value = entry.getValue();

                System.out.println("Module name: " + appModule.getName() +
                        " | Input Tuple Type: " + key.getKey() +
                        " | Output Tuple Type: " + key.getValue());
            }
        }
        System.out.println();

        System.out.println("Size of appLoops: " + applicationContext.appLoops.size());

        // print app loop
        System.out.println("AppLoops:");
        System.out.println("----------------------------------------");
        //Utility.printAppLoops(new ArrayList<>(applicationContext.appLoops));
        System.out.println();

        //setApplicationLoops(application, new ArrayList<>(applicationContext.appLoops));

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
        HashMap<String, TupleMapping> tupleMappings = new HashMap<>();

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
        for(Map.Entry<String, TupleMapping> tupleMapping : tupleMappings.entrySet()) {
            String moduleName = tupleMapping.getKey();
            TupleMapping mapping = tupleMapping.getValue();

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
        if(dataFlow.rootNode().type().equals(NodeRedJSONParser.TYPE_INJECT) || dataFlow.rootNode().type().equals(NodeRedJSONParser.TYPE_SUBSCRIBE_EVENT)) {
            System.out.println("Sub Flow " + dataFlow.rootNode().id() + " is not a data flow");
            return;
        }

        // Checks if the data flow's branches list is set
        if(dataFlow.branches() == null || dataFlow.branches().isEmpty()) {
            System.out.println("Sub Flow " + dataFlow.rootNode().id() + "'s branches list is not available!");
            return;
        }

        // Get the rootNode
        TopologyNode rootNode = dataFlow.rootNode();

        // Get branches
        List<List<TopologyNode>> branches = dataFlow.branches();

        // Check if the rootNode is a wot node, if so get its type
        if(!isWoTNode(rootNode)) {
            System.out.println("Sub Flow " + dataFlow.rootNode().id() + "'s root node is not a WoT node!");
            return;
        }

        // Used to keep track of all modules made and ensure no duplicate
        HashMap<String, TupleMapping> moduleMappings = new HashMap<>();

        // Helps keep track of all processingModules
        Map<String, String> existingProcessingModules = new HashMap<>();

        // Iterate through one branch at a time
        for(List<TopologyNode> branch : branches) {
            // Used to access nodes in a branch
            int ptr1 = 0, ptr2 = 1;

            // Helps keep track of data flow in and out of application model
            Set<String> appLoopRoute = new HashSet<>();

            while(ptr2 < branch.size()) {
                // Get the src node (Assuming it's a WoT node) and dst node (will be checked)
                TopologyNode src = branch.get(ptr1);

                TopologyNode dst = branch.get(ptr2);

                // Check if the dst node is a WoT node
                if(isWoTNode(dst)) {
                    // Check the connectivity between the src and dst nodes
                    TopologyNodeConnectionStatus connectionStatus = TopologyNodeConnectionChecker.areNodesConnected(src.id(), dst.id());

                    // I only care if there exists some connection between two nodes (whether its direct or indirect)
                    if(connectionStatus.isThereAConnection()) {
                        //if(connectionStatus.isDirectionConnection()) {} // You can change it to handle direct connections

                        // Get the property/action name of the src and dst node
                        String srcName = src.name() != null ? src.name() : src.uniqueAttribute();
                        String dstName = dst.name() != null ? dst.name() : dst.uniqueAttribute();

                        // Check if a processing module already exists for this src
                        String processingModule;

                        if (existingProcessingModules.containsKey(srcName)) {
                            processingModule = existingProcessingModules.get(srcName);
                        } else {
                            // Create a processing module (represents an intermediately processing unit for data)
                            processingModule = "PROCESSING_MODULE-" + processingModuleCount;
                            application.addAppModule(processingModule, applicationContext.applicationPreset.APP_MODULE_RAM);

                            // Store the new processing module for this src
                            existingProcessingModules.put(srcName, processingModule);

                            // Increase count only when a new module is created
                            processingModuleCount++;
                        }

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

                        // Ensure src module has output tuple type
                        if (!moduleMappings.containsKey(srcName)) {
                            moduleMappings.put(srcName, new TupleMapping());
                        }
                        moduleMappings.get(srcName).setOutputTupleType(tupleTypeOne);
                        //System.out.println("Tuple mapping made for " + srcName);

                        // Ensure processing module has both input and output tuple types
                        if (!moduleMappings.containsKey(processingModule)) {
                            moduleMappings.put(processingModule, new TupleMapping());
                        }

                        moduleMappings.get(processingModule).setInputTupleType(tupleTypeOne);
                        moduleMappings.get(processingModule).setOutputTupleType(tupleTypeTwo);
                        //System.out.println("Tuple mapping made for " + processingModule);

                        // Ensure dst module has input tuple type
                        if (!moduleMappings.containsKey(dstName)) {
                            moduleMappings.put(dstName, new TupleMapping());
                        }
                        moduleMappings.get(dstName).setInputTupleType(tupleTypeTwo);
                        //System.out.println("Tuple mapping made for " + dstName);

                        // Record the route of the data flow
                        appLoopRoute.add(srcName);
                        appLoopRoute.add(processingModule);
                        appLoopRoute.add(dstName);

                        // Add app loop to the application's app loop list
                        addLoopToAppLoops(application, new ArrayList<>(appLoopRoute));

                        // Move to the next position
                        ptr1 = ptr2;
                    }
                }
                ptr2++;
            }
        }

        // Set tuple mappings
        for(Map.Entry<String, TupleMapping> moduleMapping : moduleMappings.entrySet()) {
            String moduleName = moduleMapping.getKey();
            TupleMapping mapping = moduleMapping.getValue();

            // Only set tuple mapping if for that module, its input and output tuple types are both set
            if(mapping.getInputTupleType() != null && mapping.getOutputTupleType() != null) {
                System.out.println("Tuple mapping set for " + moduleName);
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
        // Ensure the root node is a valid event publisher
        if (!eventFlow.rootNode().type().equals(NodeRedJSONParser.TYPE_SUBSCRIBE_EVENT)) {
            System.out.println("Sub Flow " + eventFlow.rootNode().id() + " is NOT an event flow.");
            return;
        }

        // Extract event type
        String eventType = eventFlow.rootNode().uniqueAttribute();

        // Keep track of modules for tuple mappings
        HashMap<String, TupleMapping> moduleMappings = new HashMap<>();

        // Helps keep track of all processingModules
        Map<String, String> existingEventProcessingModules = new HashMap<>();

        for (List<TopologyNode> branch : eventFlow.branches()) {
            int ptr1 = 0, ptr2 = 1;

            while (ptr2 < branch.size()) {
                TopologyNode src = branch.get(ptr1);
                TopologyNode dst = branch.get(ptr2);

                if (isWoTNode(dst)) {
                    // Get the property/action name of the src and dst node
                    String srcName = src.name() != null ? src.name() : src.uniqueAttribute();
                    String dstName = dst.name() != null ? dst.name() : dst.uniqueAttribute();

                    // Create event tuple type
                    String eventTupleType = FogEntityPrefixes.EVENT_TUPLE_PREFIX + eventType;

                    // Before creating a new module, check if one already exists for the source
                    String eventProcessingModule;

                    if (existingEventProcessingModules.containsKey(srcName)) {
                        eventProcessingModule = existingEventProcessingModules.get(srcName);
                    } else {
                        eventProcessingModule = "EVENT_PROCESSING_MODULE-" + eventProcessingModuleCount;
                        application.addAppModule(eventProcessingModule, applicationContext.applicationPreset.APP_MODULE_RAM);

                        // Register the new module in the map
                        existingEventProcessingModules.put(srcName, eventProcessingModule);

                        // Increase count only when a new module is created
                        eventProcessingModuleCount++;
                    }

                    // Register event with EventManager
                    VirtualDevice vd = Utility.getVirtualDevice(applicationContext.things, applicationContext.selectedVirtualDevices, src);
                    if(vd != null) {
                        EventManager.getInstance().registerEvent(eventType, vd.getFogDevice());
                    }

                    // Connect event source to event processing module
                    addAppEdge(application, srcName, eventProcessingModule, eventTupleType, Tuple.UP, AppEdge.MODULE);

                    // Connect event processing module to destination module
                    addAppEdge(application, eventProcessingModule, dstName, eventTupleType, Tuple.DOWN, AppEdge.MODULE);

                    // Ensure proper tuple mappings
                    if (!moduleMappings.containsKey(eventProcessingModule)) {
                        moduleMappings.put(eventProcessingModule, new TupleMapping());
                    }
                    moduleMappings.get(eventProcessingModule).setInputTupleType(eventTupleType);
                    moduleMappings.get(eventProcessingModule).setOutputTupleType(eventTupleType);

                    ptr1 = ptr2;
                }
                ptr2++;
            }
        }

        // Set tuple mappings
        for (Map.Entry<String, TupleMapping> moduleMapping : moduleMappings.entrySet()) {
            application.addTupleMapping(
                    moduleMapping.getKey(),
                    moduleMapping.getValue().getInputTupleType(),
                    moduleMapping.getValue().getOutputTupleType(),
                    new FractionalSelectivity(1.0) // Adjust as needed
            );
        }
    }

    /**
     * Adds a new app loop to the app loop list in application.
     * @param application The application instance
     * @param loopToAdd The app loop to add
     */
    private static void addLoopToAppLoops(EventDrivenApplication application, List<String> loopToAdd) {
        AppLoop loop = new AppLoop(new ArrayList<>(loopToAdd));

        application.getLoops().add(loop);
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
