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
                // - It belongs to a sub flow that starts with an inject node
                // - It belongs to a sub flow that has no event or inject node
                if (subFlowStartTypes.getOrDefault(subFlowId, "event").equals("inject") ||
                        subFlowStartTypes.getOrDefault(subFlowId, "event").equals("none")) {

                    if(!nodeIds.contains(node.id())) {
                        String moduleName = node.uniqueAttribute() + "_" + node.id(); // Append ID to ensure uniqueness
                        application.addAppModule(moduleName, 10);

                        // Store the app modules created <ID, MODULE>
                        applicationContext.appModulesCreated.put(node.id(), application.getModuleByName(moduleName));

                        nodeIds.add(node.id());
                    } else {
                        System.out.println("Skipping node: " + node.uniqueAttribute() + " (node already added)");
                    }
                } else {
                    System.out.println("Skipping node: " + node.uniqueAttribute() + " (belongs to event-driven sub flow)");
                }
            }
        }
    }
    private static void setApplicationEdges(EventDrivenApplication application) {
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

                            // Create an app edge from src to dst
                            addAppEdge(application, srcModule.getName(), dstModule.getName(), src);

                            // Record the connection
                            applicationContext.appEdges.put(srcModule, dstModule);

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


    private static void addAppEdge(EventDrivenApplication application, String srcModuleName, String dstModuleName, TopologyNode node) {
        String tupleType = determineTupleType(node);
        int edgeDirection = determineDirection(node);
        int edgeType = determineEdgeType(node);

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





    /*private static void setApplicationEdges(EventDrivenApplication application) {
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
    }*/

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


    /*private static void setApplicationLoops(EventDrivenApplication application) {
        List<AppLoop> loops = new ArrayList<>();
        Map<String, List<String>> graph = new HashMap<>();

        // Step 1: Build a directed graph from application edges
        for (TopologyNode srcNode : applicationContext.topologyNodes) {
            for (TopologyNode dstNode : applicationContext.topologyNodes) {
                TopologyNodeConnectionStatus connectionStatus = applicationContext.nodeConnectionChecker.areNodesConnected(srcNode.id(), dstNode.id(), "id");
                if (connectionStatus.isThereAConnection()) {
                    String srcModuleName = applicationContext.nodeModules.get(srcNode.uniqueAttribute()).getModule().getName();
                    String dstModuleName = applicationContext.nodeModules.get(dstNode.uniqueAttribute()).getModule().getName();

                    String srcModuleName = nodeToModuleName.get(srcNode.uniqueAttribute());
                    String dstModuleName = nodeToModuleName.get(dstNode.uniqueAttribute());

                    if (srcModuleName == null || dstModuleName == null) {
                        System.out.println("Warning: Missing module for node " +
                                (srcModuleName == null ? srcNode.uniqueAttribute() : dstNode.uniqueAttribute()));
                        continue; // Skip if any module is missing
                    }

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
    }*/

    private static void setApplicationLoops(EventDrivenApplication application) {
        List<AppLoop> loops = new ArrayList<>();
        Map<String, List<String>> graph = new HashMap<>();

        // Ensure all node modules are properly tracked
        Map<String, String> nodeToModuleName = new HashMap<>();
        for (Map.Entry<String, NodeModule> entry : applicationContext.nodeModules.entrySet()) {
            nodeToModuleName.put(entry.getKey(), entry.getValue().getModule().getName());
        }

        // Step 1: Build a directed graph from application edges
        for (TopologyNode srcNode : applicationContext.topologyNodes) {
            for (TopologyNode dstNode : applicationContext.topologyNodes) {
                TopologyNodeConnectionStatus connectionStatus =
                        applicationContext.nodeConnectionChecker.areNodesConnected(srcNode.id(), dstNode.id(), "id");

                if (connectionStatus.isThereAConnection()) {
                    // Get module names safely
                    String srcModuleName = nodeToModuleName.get(srcNode.uniqueAttribute());
                    String dstModuleName = nodeToModuleName.get(dstNode.uniqueAttribute());

                    if (srcModuleName == null || dstModuleName == null) {
                        System.out.println("Warning: Missing module for node " +
                                (srcModuleName == null ? srcNode.uniqueAttribute() : dstNode.uniqueAttribute()));
                        continue; // Skip if any module is missing
                    }

                    //System.out.println("Module " + );

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
