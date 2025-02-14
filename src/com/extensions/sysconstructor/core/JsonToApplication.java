package com.extensions.sysconstructor.core;

import com.extensions.customfog.FogDeviceFactory;
import com.extensions.sysconstructor.nodered.NodeRedJSONParser;
import com.extensions.sysconstructor.nodered.NodeRedTranslator;
import com.extensions.sysconstructor.topology.TopologyNode;
import com.extensions.sysconstructor.topology.TopologyNodeConnectionChecker;
import com.extensions.sysconstructor.topology.TopologyNodeConnectionStatus;
import com.extensions.sysconstructor.topology.TopologyNodeTree;
import com.extensions.utils.FilePaths;
import com.extensions.utils.Utility;
import com.extensions.utils.presets.ApplicationPreset;
import com.extensions.utils.presets.CloudNodePreset;
import com.extensions.utils.presets.EdgeNodePreset;
import com.extensions.vdcreation.core.VirtualDevice;
import org.apache.commons.math3.util.Pair;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.application.selectivity.SelectivityModel;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class JsonToApplication {
    /**
     * Presets for defining the characteristics of cloud nodes in the application
     */
    private final CloudNodePreset cloudNodePreset;

    /**
     * Presets for defining the characteristics of all edge nodes in the application
     */
    private final EdgeNodePreset edgeNodePreset;

    /**
     * Presets for defining the characteristics of all cloud nodes in the application
     */
    private final ApplicationPreset applicationPreset;

    /**
     * The list of all WoT thing nodes parsed from the application topology
     */
    private final List<TopologyNode> things;

    /**
     * The list of all topics parsed from the application topology. Used to group devices with edge nodes.
     */
    private final List<String> nodeTopics;

    /**
     * A list of all nodes used in the application.
     */
    private final List<TopologyNode> topologyNodes;

    /**
     * A list of all sub flows in a tree-like data structure
     */
    private final List<TopologyNodeTree> topologyNodeTrees;

    /**
     * A list of sub flow trees that represent data flows within an application
     */
    private final List<TopologyNodeTree> dataFlows;

    /**
     * Sub flow trees that represent (user) inject stimulated data flows.
     */
    private final List<TopologyNodeTree> injectFlows;

    /**
     * Sub flow trees that represent event flows within an application.
     */
    private final List<TopologyNodeTree> eventFlows;

    /**
     * A list of loops present in the application topology
     */
    private final List<AppLoop> appLoops;

    /**
     * The parser responsible for extracting information from application topology file
     */
    private final ApplicationTopologyParser applicationTopologyParser;

    /**
     * A list of all virtual devices used in the application
     */
    private final List<VirtualDevice> selectedVirtualDevices;

    /**
     * A count used to keep track of all processing modules created
     */
    private int processingModuleCount;

    /**
     * A count used to keep track of all event processing modules created
     */
    private int eventProcessingModuleCount;

    /**
     * Used to keep track of all modules made and ensure no duplicate
     */
    private HashMap<String, TupleMapping> moduleTupleMappings;

    /**
     * Helps keep track of all processingModules.
     */
    private Map<String, String> existingProcessingModules;

    public JsonToApplication(
            CloudNodePreset cloudNodePreset,
            EdgeNodePreset edgeNodePreset,
            ApplicationPreset applicationPreset,
            File nodeRedApplicationJsonFile
    ) throws IOException {
        this.cloudNodePreset = cloudNodePreset;
        this.edgeNodePreset = edgeNodePreset;
        this.applicationPreset = applicationPreset;

        // Initialize variables, data structures and storages
        this.dataFlows = new ArrayList<>();
        this.eventFlows = new ArrayList<>();
        this.injectFlows = new ArrayList<>();
        this.appLoops = new ArrayList<>();
        this.selectedVirtualDevices = new ArrayList<>();
        this.moduleTupleMappings = new HashMap<>();
        this.existingProcessingModules = new HashMap<>();
        this.processingModuleCount = 0;
        this.eventProcessingModuleCount = 0;

        // Convert the Node-RED application description into a structured input format
        NodeRedTranslator.nodeRedToInputJson(nodeRedApplicationJsonFile);

        // Initialize the topology parser using the generated application topology file
        this.applicationTopologyParser = new ApplicationTopologyParser(new File(FilePaths.APPLICATION_TOPOLOGY));

        // Parse and construct topology node trees for hierarchical organization
        this.topologyNodeTrees = applicationTopologyParser.parseTopologyNodeTrees();

        // Group sub flows into inject flows, data flows and event flows
        groupTopologyNodeTrees(topologyNodeTrees);

        // Extract IoT device nodes (things) from the parsed topology
        this.things = applicationTopologyParser.parseTopologyNodes("things");

        // Extract the list of communication topics used in the application
        this.nodeTopics = applicationTopologyParser.parseTopologyNodeTopics();

        // Get all the topology nodes from the sub flows list
        this.topologyNodes = Utility.getAllNodesFromTopology(topologyNodeTrees);

        // Initialize the connection checker to validate topology node connections
        TopologyNodeConnectionChecker.initializeChecker(topologyNodeTrees);
    }

    //////////////////////////////////////////// APPLICATION MODEL CONSTRUCTION ////////////////////////////////////////////

    public Application createApplicationModel(String appId, int userId) {
        // Create the application instance
        Application application = new Application(appId, userId);

        System.out.println(application.getLoops() != null ? "Application loop list set!" : "Application loop list not set!");

        // Create the application model for all inject flows
        for(TopologyNodeTree injectFlow : injectFlows) {
            setupInjectFlowInApplication(application, injectFlow);
        }

        // Create the application model for all data flows
        for(TopologyNodeTree dataFlow : dataFlows) {
            setupDataFlowInApplication(application, dataFlow);
        }

        // Set all the event flows for the application model
        for(TopologyNodeTree eventFlow : eventFlows) {
            setupEventFlowInApplication(application, eventFlow);
        }

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

        // print app loop
        System.out.println("AppLoops:");
        System.out.println("----------------------------------------");
        for(AppLoop appLoop : application.getLoops()) {
            System.out.println(Utility.formAppLoop(appLoop));
        }
        System.out.println();

        System.out.println("Application Model formed!");

        return application;
    }

    public void setupInjectFlowInApplication(Application application, TopologyNodeTree injectFlow) {}

    /**
     * Creates the necessary app modules, app edges, tuple mappings (for app modules) and app loops based on the provided
     * data flow for the application.
     *
     * @param application The application instance.
     * @param dataFlow The sub flow that represents a data flow
     */
    private void setupDataFlowInApplication(Application application, TopologyNodeTree dataFlow) {
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

        // Helps keep track of data flow in and out of application model
        Set<String> appLoopRoute = new HashSet<>();

        // Iterate through one branch at a time
        for(List<TopologyNode> branch : branches) {
            // Used to access nodes in a branch
            int ptr1 = 0, ptr2 = 1;

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
                            application.addAppModule(processingModule, applicationPreset.APP_MODULE_RAM);

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
                        if (!moduleTupleMappings.containsKey(srcName)) {
                            moduleTupleMappings.put(srcName, new TupleMapping());
                        }
                        moduleTupleMappings.get(srcName).setOutputTupleType(tupleTypeOne);

                        // Ensure processing module has both input and output tuple types
                        if (!moduleTupleMappings.containsKey(processingModule)) {
                            moduleTupleMappings.put(processingModule, new TupleMapping());
                        }
                        moduleTupleMappings.get(processingModule).setInputTupleType(tupleTypeOne);
                        moduleTupleMappings.get(processingModule).setOutputTupleType(tupleTypeTwo);

                        // Ensure dst module has input tuple type
                        if (!moduleTupleMappings.containsKey(dstName)) {
                            moduleTupleMappings.put(dstName, new TupleMapping());
                        }
                        moduleTupleMappings.get(dstName).setInputTupleType(tupleTypeTwo);

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
        for(Map.Entry<String, TupleMapping> moduleMapping : moduleTupleMappings.entrySet()) {
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

    public void setupEventFlowInApplication(Application application, TopologyNodeTree eventFlow) {}

    /**
     * Adds a new app loop to the app loop list in application.
     * @param application The application instance
     * @param loopToAdd The app loop to add
     */
    private static void addLoopToAppLoops(Application application, List<String> loopToAdd) {
        AppLoop loop = new AppLoop(loopToAdd);

        if(application.getLoops() != null) {
            application.getLoops().add(loop);
            System.out.println("Application loop set: " + loop.getModules());
        } else {
            System.out.println("Application loops are null!");
        }
    }

    private static boolean isWoTNode(TopologyNode node) {
        return node.thing() != null && !node.thing().isEmpty();
    }


    private void addAppEdge(Application application, String srcModuleName, String dstModuleName, String tupleType, int edgeDirection, int edgeType) {
        application.addAppEdge(
                srcModuleName,
                dstModuleName,
                applicationPreset.APP_EDGE_TUPLE_CPU_LENGTH,  // Processing latency
                applicationPreset.APP_EDGE_TUPLE_NW_LENGTH,   // Transmission latency
                tupleType,
                edgeDirection,
                edgeType
        );

        System.out.println("Connected: " + srcModuleName + " --> " + dstModuleName);
    }

    private String determineTupleType(TopologyNode node) {
        return node.uniqueAttribute();
    }

    private int determineDirection(TopologyNode node) {
        // Assume upward tuple flow for sensors and downward for actuators
        if (node.type().equals(NodeRedJSONParser.TYPE_READ_PROPERTY)) {
            return Tuple.UP;
        } else if (node.type().equals(NodeRedJSONParser.TYPE_INVOKE_ACTION) || node.type().equals(NodeRedJSONParser.TYPE_WRITE_PROPERTY)) {
            return Tuple.DOWN;
        } else {
            return Tuple.UP;  // Default for other module connections
        }
    }

    private int determineEdgeType(TopologyNode node) {
        if (node.type().equals(NodeRedJSONParser.TYPE_READ_PROPERTY)) {
            return AppEdge.SENSOR;
        } else if (node.type().equals(NodeRedJSONParser.TYPE_INVOKE_ACTION) || node.type().equals(NodeRedJSONParser.TYPE_WRITE_PROPERTY)) {
            return AppEdge.ACTUATOR;
        } else {
            return AppEdge.MODULE;
        }
    }

    ////////////////////////////////////// APPLICATION PHYSICAL TOPOLOGY CONSTRUCTION //////////////////////////////////////

    public ApplicationPhysicalTopology createApplicationPhysicalTopology(List<VirtualDevice> virtualDevices) {
        // Will store all fog devices used in the application
        List<FogDevice> fogDevices = new ArrayList<>();

        // Will store all edge nodes used in the application
        List<FogDevice> edgeNodes = new ArrayList<>();

        try {
            // Search for selected VDs based on things list
            List<VirtualDevice> allSelectedVirtualDevices = getSelectedVirtualDevices(virtualDevices, things);

            if(allSelectedVirtualDevices == null) throw new Error("No virtual devices exist for things specified!");

            // Set the selected virtual devices
            selectedVirtualDevices.addAll(allSelectedVirtualDevices);

            // Get the names of all sensors and actuators used in the application
            Set<String> sensorsAndActuatorsUsed = new HashSet<>(getAllSensorsAndActuatorsUsed(topologyNodes));

            // Lists to store used sensors and actuators
            List<Sensor> allSensorsUsedInApplication = new ArrayList<>();
            List<Actuator> allActuatorsUsedInApplication = new ArrayList<>();

            // Iterate once over VDs to collect sensors & actuators
            for (VirtualDevice virtualDevice : selectedVirtualDevices) {
                for (Sensor sensor : virtualDevice.getSensorProperties()) {
                    if (sensorsAndActuatorsUsed.contains(sensor.getName())) {
                        allSensorsUsedInApplication.add(sensor);
                    }
                }
                for (Actuator actuator : virtualDevice.getActuatorActions()) {
                    if (sensorsAndActuatorsUsed.contains(actuator.getName())) {
                        allActuatorsUsedInApplication.add(actuator);
                    }
                }
            }

            // Create cloud node/device at the top of the hierarchy
            FogDevice cloud = FogDeviceFactory.createFogDevice(
                    "cloud",
                    cloudNodePreset.MIPS,
                    cloudNodePreset.RAM,
                    cloudNodePreset.UPLINK_BW,
                    cloudNodePreset.DOWN_LINK_BW,
                    cloudNodePreset.LATENCY,
                    cloudNodePreset.RATE_PER_MIPS,
                    cloudNodePreset.BUSY_POWER,
                    cloudNodePreset.IDLE_POWER
            );

            // Cloud has no parent, it is the root of the hierarchy
            cloud.setParentId(-1);

            fogDevices.add(cloud);

            if(!nodeTopics.isEmpty()) { // If the topics array is set
                // Assign VDs to edge nodes based on topics
                for(String topic : nodeTopics) {
                    // Create the edge node for that topic
                    FogDevice edgeNode = createEdgeNode(topic);

                    // Link the edge node to the cloud
                    edgeNode.setParentId(cloud.getId());
                    edgeNode.setUplinkLatency(applicationPreset.UPLINK_LATENCY_EDGE_TO_CLOUD);
                    fogDevices.add(edgeNode);
                    edgeNodes.add(edgeNode);

                    // Connect all VDs to this edge node based on the topic
                    for(VirtualDevice virtualDevice : selectedVirtualDevices) {
                        VirtualDevice vd = getVirtualDeviceWithMatchingTopic(topologyNodes, things, virtualDevice, topic);

                        FogDevice vdFogDevice = null;

                        if(vd != null) vdFogDevice = vd.getFogDevice();

                        if(vdFogDevice != null) {
                            vdFogDevice.setParentId(edgeNode.getId());
                            vdFogDevice.setUplinkLatency(applicationPreset.UPLINK_LATENCY_VD_TO_EDGE);
                            fogDevices.add(vdFogDevice);
                        }
                    }
                }
            } else { // If the topics array is not set, distribute nodes normally
                // Calculate the number of edge nodes needed
                int numberOfEdgeNodes = Math.max(1, calculateNoOfEdgeNodes(selectedVirtualDevices.size(), applicationPreset.MAX_VDS_FOR_ONE_EDE_NODE));

                // Create a list of edge nodes (each edge node is represented as a list of VDs)
                List<List<VirtualDevice>> edgeNodeList = new ArrayList<>();

                // Distribute virtual devices among edge nodes
                for (int i = 0; i < numberOfEdgeNodes; i++) {
                    edgeNodeList.add(new ArrayList<>());
                }

                for (int i = 0; i < selectedVirtualDevices.size(); i++) {
                    // Assign each virtual device to an edge node in a round-robin manner
                    edgeNodeList.get(i % numberOfEdgeNodes).add(selectedVirtualDevices.get(i));
                }

                // Connect the VDs to the edge nodes
                for(int index = 0; index < edgeNodeList.size(); index++) {
                    // Create the edge node
                    FogDevice edgeNode = createEdgeNode(String.valueOf(index));

                    // Link the edge node to the cloud
                    edgeNode.setParentId(cloud.getId());
                    edgeNode.setUplinkLatency(applicationPreset.UPLINK_LATENCY_EDGE_TO_CLOUD);
                    fogDevices.add(edgeNode);
                    edgeNodes.add(edgeNode);

                    // Connect the VDs to the edge node
                    for(VirtualDevice virtualDevice : edgeNodeList.get(index)) {
                        FogDevice vdFogDevice = virtualDevice.getFogDevice();

                        vdFogDevice.setParentId(edgeNode.getId());

                        vdFogDevice.setUplinkLatency(applicationPreset.UPLINK_LATENCY_VD_TO_EDGE);

                        fogDevices.add(vdFogDevice);
                    }
                }
            }

            // Create the physical topology instance and set its variables
            ApplicationPhysicalTopology applicationPhysicalTopology = new ApplicationPhysicalTopology();

            applicationPhysicalTopology.setFogDevices(fogDevices);
            applicationPhysicalTopology.setSensors(allSensorsUsedInApplication);
            applicationPhysicalTopology.setActuators(allActuatorsUsedInApplication);
            applicationPhysicalTopology.setEdgeNodes(edgeNodes);

            System.out.println("Application's physical topology formed!");

            return applicationPhysicalTopology;
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private List<String> getAllSensorsAndActuatorsUsed(List<TopologyNode> nodes) {
        List<String> attributeNames = new ArrayList<>();
        List<String> includeTypes = new ArrayList<>(){{add("read-property"); add("invoke-action"); add("write-property");}};

        for(TopologyNode node : nodes) {
            if(includeTypes.contains(node.type())) attributeNames.add(node.uniqueAttribute());
        }

        return attributeNames;
    }

    private int calculateNoOfEdgeNodes(int numberOfVDs, int maxNoVDsForOneEdgeNode) {
        // CHANGE FORMULA AS YOU SEE FIT
        return (int)(numberOfVDs - maxNoVDsForOneEdgeNode) / 2;
    }

    private List<VirtualDevice> getSelectedVirtualDevices(List<VirtualDevice> virtualDevices, List<TopologyNode> things) {
        List<VirtualDevice> selectedVirtualDevices = new ArrayList<>();

        for(VirtualDevice virtualDevice : virtualDevices) {
            String name = virtualDevice.getFogDevice().getName();

            for(TopologyNode thing : things) {
                String thingName = thing.name();

                if(name.equals(thingName)) selectedVirtualDevices.add(virtualDevice);
            }
        }

        return selectedVirtualDevices.isEmpty() ? null : selectedVirtualDevices;
    }

    private FogDevice createEdgeNode(String identifier) {
        return FogDeviceFactory.createFogDevice(
                "edgeNode-" + identifier,
                edgeNodePreset.MIPS,
                edgeNodePreset.RAM,
                edgeNodePreset.UPLINK_BW,
                edgeNodePreset.DOWN_LINK_BW,
                edgeNodePreset.LATENCY,
                edgeNodePreset.RATE_PER_MIPS,
                edgeNodePreset.BUSY_POWER,
                edgeNodePreset.IDLE_POWER
        );
    }

    private VirtualDevice getVirtualDeviceWithMatchingTopic(List<TopologyNode> nodes, List<TopologyNode> things, VirtualDevice virtualDevice, String targetTopic) {
        if (targetTopic == null || targetTopic.isEmpty()) {
            System.out.println("Topic must not be empty!");
            return null;
        }

        // Map thing ID → Thing Node
        Map<String, TopologyNode> thingNodeMap = new HashMap<>();
        for (TopologyNode thingNode : things) {
            thingNodeMap.put(thingNode.id(), thingNode);
        }

        // Set of valid node types
        Set<String> validNodeTypes = Set.of(NodeRedJSONParser.TYPE_READ_PROPERTY, NodeRedJSONParser.TYPE_INVOKE_ACTION);

        // Iterate through nodes
        for (TopologyNode node : nodes) {
            if (node.thing() != null && !node.thing().isEmpty() &&
                    node.topic() != null && node.topic().equals(targetTopic) &&
                    validNodeTypes.contains(node.uniqueAttribute())) {

                TopologyNode thingNode = thingNodeMap.get(node.thing());

                if (thingNode != null && virtualDevice.getFogDevice().getName().equals(thingNode.name())) {
                    return virtualDevice;  // Match found
                }
            }
        }

        return null;
    }

    /////////////////////////////////////////////////// HELPER FUNCTIONS ///////////////////////////////////////////////////

    /**
     * Organises the topology node trees into their irrespective sub flow groups: event sub flow, data sub flow and inject sub flow.
     * @param topologyNodeTrees The list of all topology node trees (sub flows) extracted from the application topology.
     */
    private void groupTopologyNodeTrees(List<TopologyNodeTree> topologyNodeTrees) {
        for (TopologyNodeTree topologyNodeTree : topologyNodeTrees) {
            TopologyNode rootNode = topologyNodeTree.rootNode();

            if(rootNode == null) {
                continue;
            }

            if (rootNode.type().equals(NodeRedJSONParser.TYPE_SUBSCRIBE_EVENT)) {
                eventFlows.add(topologyNodeTree);
            } else if (rootNode.type().equals(NodeRedJSONParser.TYPE_INJECT)) {
                injectFlows.add(topologyNodeTree);
            } else {
                dataFlows.add(topologyNodeTree);
            }
        }
    }

    ///////////////////////////////////////////////// GETTERS AND SETTERS //////////////////////////////////////////////////


    public List<TopologyNode> getThings() {
        return things;
    }

    public List<String> getNodeTopics() {
        return nodeTopics;
    }

    public List<TopologyNode> getTopologyNodes() {
        return topologyNodes;
    }

    public List<TopologyNodeTree> getTopologyNodeTrees() {
        return topologyNodeTrees;
    }

    public List<TopologyNodeTree> getDataFlows() {
        return dataFlows;
    }

    public List<TopologyNodeTree> getInjectFlows() {
        return injectFlows;
    }

    public List<TopologyNodeTree> getEventFlows() {
        return eventFlows;
    }

    public ApplicationTopologyParser getApplicationTopologyParser() {
        return applicationTopologyParser;
    }

    public List<VirtualDevice> getSelectedVirtualDevices() {
        return selectedVirtualDevices;
    }

    public int getProcessingModuleCount() {
        return processingModuleCount;
    }

    public int getEventProcessingModuleCount() {
        return eventProcessingModuleCount;
    }

    public HashMap<String, TupleMapping> getModuleTupleMappings() {
        return moduleTupleMappings;
    }

    public Map<String, String> getExistingProcessingModules() {
        return existingProcessingModules;
    }
}
