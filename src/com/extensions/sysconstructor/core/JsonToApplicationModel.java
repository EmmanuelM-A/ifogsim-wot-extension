package com.extensions.sysconstructor.core;

import com.extensions.App;
import com.extensions.sysconstructor.eventdriver.EventDrivenApplication;
import com.extensions.sysconstructor.nodered.NodeRedJSONParser;
import com.extensions.sysconstructor.topology.*;
import com.extensions.utils.Utility;
import jdk.jshell.execution.Util;
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

        for(AppModule appModule : application.getModules()) {
            System.out.println(appModule.getName());
        }

        // Set the app edges between modules
        setApplicationEdges(application);

        // Set the tuple mappings for modules
        setApplicationTupleMappings(application);

        // Set the app loops
        setApplicationLoops(application);

        // Set the application events
        setApplicationEvents(application);


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

    /*private static void setApplicationModules(EventDrivenApplication application) {
        // Default module represents generic computations
        application.addAppModule("default-module", 10);
        applicationContext.nodeModules.put("default-module", new NodeModule(application.getModuleByName("default-module")));

        // Client module represents user-related computations
        application.addAppModule("client", 10);
        applicationContext.nodeModules.put("client", new NodeModule(application.getModuleByName("client")));

        List<String> nodeTypesToSearchFor = new ArrayList<>() {{
            add("read-property");
            add("invoke-action");
        }};

        // Retrieve inject nodes separately
        List<TopologyNode> injectNodes = Utility.getTopologyNodesByType(applicationContext.topologyNodes, NodeRedJSONParser.TYPE_INJECT);

        // Iterate through all WoT node types
        for (String nodeType : nodeTypesToSearchFor) {
            List<TopologyNode> nodes = Utility.getTopologyNodesByType(applicationContext.topologyNodes, nodeType);

            for (TopologyNode node : nodes) {
                boolean isConnectedToEvent = applicationContext.nodeConnectionChecker.areNodesConnected(
                        NodeRedJSONParser.TYPE_SUBSCRIBE_EVENT, node.type(), "type"
                ).isThereAConnection();

                boolean isConnectedToInject = false;
                for (TopologyNode injectNode : injectNodes) {
                    if (applicationContext.nodeConnectionChecker.areNodesConnected(
                            injectNode.id(), node.id(), "id"
                    ).isThereAConnection()) {
                        isConnectedToInject = true;
                        break;
                    }
                }

                // If node is NOT connected to an event subscription but starts a flow
                if (!isConnectedToEvent || isConnectedToInject) {
                    String moduleName = node.uniqueAttribute().replaceAll("\\s+", "_"); // Ensure valid module names
                    AppModule appModule = application.addAppModule(moduleName);
                    NodeModule nodeModule = new NodeModule(appModule);

                    System.out.println("Adding node module: " + moduleName);

                    applicationContext.nodeModules.put(moduleName, nodeModule);
                } else {
                    System.out.println("Skipping node: " + node.uniqueAttribute() + " (connected to event)");
                }
            }
        }
    }*/

    private static void setApplicationModules(EventDrivenApplication application) {
        // Default modules
        application.addAppModule("default-module", 10);
        applicationContext.nodeModules.put("default-module", new NodeModule(application.getModuleByName("default-module")));
        application.addAppModule("client", 10);
        applicationContext.nodeModules.put("client", new NodeModule(application.getModuleByName("client")));

        // Define node types to process
        List<String> nodeTypesToSearchFor = List.of("read-property", "invoke-action");

        // Get all subflows
        Map<String, List<TopologyNode>> subflows = Utility.getAllSubflows(applicationContext.topologyNodes);

        // Determine how each subflow starts
        Map<String, String> subflowStartTypes = new HashMap<>(); // subflowId -> "event" or "inject" or "none"

        for (Map.Entry<String, List<TopologyNode>> entry : subflows.entrySet()) {
            String subflowId = entry.getKey();
            List<TopologyNode> subflowNodes = entry.getValue();

            boolean hasEventStart = false;
            boolean hasInjectStart = false;

            for (TopologyNode node : subflowNodes) {
                if (node.type().equals(NodeRedJSONParser.TYPE_SUBSCRIBE_EVENT)) {
                    hasEventStart = true;
                    break;
                } else if (node.type().equals(NodeRedJSONParser.TYPE_INJECT)) {
                    hasInjectStart = true;
                }
            }

            if (hasEventStart) {
                subflowStartTypes.put(subflowId, "event");
            } else if (hasInjectStart) {
                subflowStartTypes.put(subflowId, "inject");
            } else {
                subflowStartTypes.put(subflowId, "none");
            }
        }

        // Process WoT nodes
        for (String nodeType : nodeTypesToSearchFor) {
            List<TopologyNode> nodes = Utility.getTopologyNodesByType(applicationContext.topologyNodes, nodeType);

            for (TopologyNode node : nodes) {
                String subflowId = node.getSubflowId(); // Get the subflow the node belongs to

                // Accept if:
                // - It belongs to a subflow that starts with an inject node
                // - It belongs to a subflow that has no event or inject node
                if (subflowStartTypes.getOrDefault(subflowId, "event").equals("inject") ||
                        subflowStartTypes.getOrDefault(subflowId, "event").equals("none")) {

                    String moduleName = node.uniqueAttribute().replaceAll("\\s+", "_"); // Ensure valid names
                    AppModule appModule = application.addAppModule(moduleName);
                    NodeModule nodeModule = new NodeModule(appModule);

                    System.out.println("Adding node module: " + moduleName);

                    applicationContext.nodeModules.put(moduleName, nodeModule);
                } else {
                    System.out.println("Skipping node: " + node.uniqueAttribute() + " (belongs to event-driven subflow)");
                }
            }
        }
    }



    private static void setApplicationEdges(EventDrivenApplication application) {
        for (TopologyNode srcNode : applicationContext.topologyNodes) {
            for (TopologyNode dstNode : applicationContext.topologyNodes) {
                TopologyNodeConnectionStatus connectionStatus = applicationContext.nodeConnectionChecker.areNodesConnected(srcNode.id(), dstNode.id(), "id");

                if (connectionStatus.isThereAConnection()) {
                    // Retrieve the module names from nodeModules
                    NodeModule srcModule = applicationContext.nodeModules.get(srcNode.uniqueAttribute());
                    NodeModule dstModule = applicationContext.nodeModules.get(dstNode.uniqueAttribute());

                    if (srcModule != null && dstModule != null) {
                        String tupleType = determineTupleType(srcNode, dstNode);
                        int edgeDirection = determineDirection(srcNode, dstNode);
                        int edgeType = determineEdgeType(srcNode, dstNode);

                        // Set node module variables
                        srcModule.setNextModule(dstModule);
                        srcModule.setOutputTupleType(tupleType);
                        dstModule.setInputTupleType(tupleType);

                        // Add tuple to multi-output modules
                        if (srcModule instanceof MultiOutputNodeModule multiModule) {
                            multiModule.addOutputTuple(tupleType);
                        }

                        application.addAppEdge(
                                srcModule.getModule().getName(),
                                dstModule.getModule().getName(),
                                applicationContext.applicationPreset.APP_EDGE_TUPLE_CPU_LENGTH,  // Processing latency (adjust as needed)
                                applicationContext.applicationPreset.APP_EDGE_TUPLE_NW_LENGTH,    // Transmission latency (adjust as needed)
                                tupleType,
                                edgeDirection,
                                edgeType
                        );
                    }
                }
            }
        }
    }

    private static void setApplicationTupleMappings(EventDrivenApplication application) {
        for (Map.Entry<String, NodeModule> entry : applicationContext.nodeModules.entrySet()) {
            NodeModule module = entry.getValue();

            String moduleName = module.getModule().getName();
            String inputTuple = module.getInputTupleType();
            String outputTuple = module.getOutputTupleType();

            // Ensure valid mappings exist
            if (inputTuple != null && outputTuple != null) {
                application.addTupleMapping(moduleName, inputTuple, outputTuple, new FractionalSelectivity(1.0));
            }

            // Handle multiple output tuples
            if (module instanceof MultiOutputNodeModule multiModule) {  // A subclass supporting multiple outputs
                for (String extraOutput : multiModule.getAdditionalOutputTuples()) {
                    application.addTupleMapping(moduleName, inputTuple, extraOutput, new FractionalSelectivity(1.0));  // Adjust selectivity as needed
                }
            }
        }
    }


    private static void setApplicationLoops(EventDrivenApplication application) {
        List<AppLoop> loops = new ArrayList<>();
        Map<String, List<String>> graph = new HashMap<>();

        // Step 1: Build a directed graph from application edges
        for (TopologyNode srcNode : applicationContext.topologyNodes) {
            for (TopologyNode dstNode : applicationContext.topologyNodes) {
                TopologyNodeConnectionStatus connectionStatus = applicationContext.nodeConnectionChecker.areNodesConnected(srcNode.id(), dstNode.id(), "id");
                if (connectionStatus.isThereAConnection()) {
                    String srcModuleName = applicationContext.nodeModules.get(srcNode.uniqueAttribute()).getModule().getName();
                    String dstModuleName = applicationContext.nodeModules.get(dstNode.uniqueAttribute()).getModule().getName();

                    graph.putIfAbsent(srcModuleName, new ArrayList<>());
                    graph.get(srcModuleName).add(dstModuleName);
                }
            }
        }

        // Step 2: Find loops in the graph using DFS
        Set<String> visited = new HashSet<>();
        Stack<String> pathStack = new Stack<>();
        Set<List<String>> detectedLoops = new HashSet<>();

        for (String node : graph.keySet()) {
            findLoopsDFS(node, graph, visited, pathStack, detectedLoops);
        }

        // Step 3: Convert detected loops into AppLoop objects
        for (List<String> loopPath : detectedLoops) {
            loops.add(new AppLoop(new ArrayList<>(loopPath)));
        }

        // Step 4: Assign loops to the application
        application.setLoops(loops);
    }


    private static void setApplicationEvents(EventDrivenApplication application) {

    }

    /**
     * DFS-based loop detection
     */
    private static void findLoopsDFS(String node, Map<String, List<String>> graph, Set<String> visited, Stack<String> pathStack, Set<List<String>> detectedLoops) {
        if (pathStack.contains(node)) {
            // Loop detected: extract loop sequence
            int loopStartIndex = pathStack.indexOf(node);
            List<String> loopPath = new ArrayList<>(pathStack.subList(loopStartIndex, pathStack.size()));
            loopPath.add(node); // Complete the cycle
            detectedLoops.add(loopPath);
            return;
        }

        if (visited.contains(node)) return;

        visited.add(node);
        pathStack.push(node);

        if (graph.containsKey(node)) {
            for (String neighbor : graph.get(node)) {
                findLoopsDFS(neighbor, graph, visited, pathStack, detectedLoops);
            }
        }

        pathStack.pop();
    }

    private static String determineTupleType(TopologyNode src, TopologyNode dst) {
        if (src.type().equals("read-property")) {
            return src.uniqueAttribute();  // Use property name as tuple
        } else if (dst.type().equals("invoke-action") || dst.type().equals("write-property")) {
            return dst.uniqueAttribute();  // Action-related tuple
        } else {
            return src.uniqueAttribute();  // Generic processing tuple
        }
    }


    private static int determineDirection(TopologyNode src, TopologyNode dst) {
        // Assume upward tuple flow for sensors and downward for actuators
        if (src.type().equals(NodeRedJSONParser.TYPE_READ_PROPERTY)) {
            return Tuple.UP;
        } else if (dst.type().equals(NodeRedJSONParser.TYPE_INVOKE_ACTION) || dst.type().equals(NodeRedJSONParser.TYPE_WRITE_PROPERTY)) {
            return Tuple.DOWN;
        } else {
            return Tuple.UP;  // Default for other module connections
        }
    }

    private static int determineEdgeType(TopologyNode src, TopologyNode dst) {
        if (src.type().equals(NodeRedJSONParser.TYPE_READ_PROPERTY)) {
            return AppEdge.SENSOR;
        } else if (dst.type().equals(NodeRedJSONParser.TYPE_INVOKE_ACTION) || dst.type().equals(NodeRedJSONParser.TYPE_WRITE_PROPERTY)) {
            return AppEdge.ACTUATOR;
        } else {
            return AppEdge.MODULE;
        }
    }
}
