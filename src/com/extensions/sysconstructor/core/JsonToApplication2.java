package com.extensions.sysconstructor.core;

import com.extensions.customfog.FogDeviceFactory;
import com.extensions.sysconstructor.eventdriver.EventManager;
import com.extensions.sysconstructor.eventdriver.EventSensor;
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
import com.extensions.vdcreation.core.VD;
import com.extensions.vdcreation.core.VirtualDevice;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class JsonToApplication2 {
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
    private final List<TopologyNode> allTopologyNodes;

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
    private final List<VD> selectedVirtualDevices;

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

    // Keep tracks of the number of worker modules created
    private int workerModuleCount = 1;

    // Keep tracks of the number of worker modules created
    private int eventWorkerModuleCount = 1;

    public JsonToApplication2(
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
        this.allTopologyNodes = Utility.getAllNodesFromTopology(topologyNodeTrees);

        // Initialize the connection checker to validate topology node connections
        TopologyNodeConnectionChecker.initializeChecker(topologyNodeTrees);
    }

    //////////////////////////////////////////// APPLICATION MODEL CONSTRUCTION ////////////////////////////////////////////

    public Application createApplicationModel(String appId, int userId) {
        return mapAppTopologyToAppModel(appId, userId, topologyNodeTrees);
    }

    private Application mapAppTopologyToAppModel(String appId, int userId, List<TopologyNodeTree> topologyNodeTrees) {
        // Check if the sub-flow flow exists
        if(topologyNodeTrees == null) {
            System.out.println("Dataflow passed in is null!");
            return null;
        }

        // Create application
        Application application = Application.createApplication(appId, userId);

        // Create master module
        String MASTER_MODULE = "MasterModule";
        application.addAppModule(MASTER_MODULE, applicationPreset.APP_MODULE_RAM);

        // Keeps track of sensor-actuator pairs that have been made
        Set<VD> virtualDevicesUsedSoFar = new HashSet<>();

        // Keep track of the last used VD
        VD prevVD = null;

        for(TopologyNodeTree topologyNodeTree : topologyNodeTrees) {
            // Check if the sub flow is not null
            if(topologyNodeTree == null) continue;

            // If the sub flow is an inject sub-flow, skip (as only data flows and event flows are used)
            if(topologyNodeTree.rootNode().type().equals(NodeRedJSONParser.TYPE_INJECT)) continue;

            // If the sub-flow's branches list is not set, skip
            if(topologyNodeTree.branches() == null || topologyNodeTree.branches().isEmpty()) continue;

            // Get the rootNode
            TopologyNode rootNode = topologyNodeTree.rootNode();

            // Get branches
            List<List<TopologyNode>> branches = topologyNodeTree.branches();

            // Iterate through one branch at a time
            for(List<TopologyNode> branch : branches) {
                // Get the src and dst node
                TopologyNode src = branch.getFirst(); // Assumed to be sensor
                TopologyNode dst = branch.getLast(); // Assumed to actuator

                // Check if the src node is a sensor and if the dst node is an actuator
                if(isSensor(src) && isActuator(dst)) {
                    // Check the connectivity between the src and dst nodes
                    TopologyNodeConnectionStatus connectionStatus = TopologyNodeConnectionChecker.areNodesConnected(src.id(), dst.id());

                    // For this level of complexity, I only care if there exists some connection between two nodes (whether its direct or indirect)
                    if(connectionStatus.isThereAConnection()) {
                        //if(connectionStatus.isDirectionConnection()) {} // You can change it to handle direct connections

                        // Get the VD that holds the src node (sensor)
                        VD virtualDevice = getVDUsed(src, selectedVirtualDevices);

                        // Determines if the sub flow is an event flow or not
                        boolean isEventful = rootNode.type().equals(NodeRedJSONParser.TYPE_SUBSCRIBE_EVENT);

                        // Check if VD exists
                        if(virtualDevice != null) {
                            // Get the VD_SENSOR and VD_ACTUATOR, which both represent all sensors and actuator for that VD respectively
                            String VD_SENSOR = virtualDevice.getSensor().getName();
                            String VD_ACTUATOR = virtualDevice.getActuator().getName();

                            // Check if the VD has not been used before (means VD SENSOR-MODULE-ACTUATOR for that sub-flow has not been created yet)
                            if(!virtualDevicesUsedSoFar.contains(virtualDevice)) {
                                // Make an edge from VD_SENSOR to MASTER_MODULE carrying tuple types of VD_SENSOR
                                addAppEdge(application, VD_SENSOR, MASTER_MODULE, VD_SENSOR, Tuple.UP, AppEdge.SENSOR);

                                // Make an edge from MASTER_MODULE to VD_ACTUATOR, carrying tuple types of VD_ACTUATOR
                                addAppEdge(application, MASTER_MODULE, VD_ACTUATOR, VD_ACTUATOR, Tuple.DOWN, AppEdge.ACTUATOR);

                                // Record the VD
                                virtualDevicesUsedSoFar.add(virtualDevice);

                                /*
                                Once the VD_SENSOR -> MASTER_MODULE -> VD_ACTUATOR connection is set up for a given VD, all branches (from that
                                sub-flow) that use that VD (meaning the sensor is from that VD), will be converted into a worker module, so that
                                data flowing through the sub-flow is still incorporated into the application model. If a sub-flow branch has an existing
                                VD SENSOR-MODULE-ACTUATOR connection already, the sub-flow branch is just converted into another WORKER_MODULE-MASTER_MODULE
                                connection for the existing VD SENSOR-MODULE-ACTUATOR connection.
                                */

                                // Get the property/action name of the src and dst node which is used as the name for sensor and actuator
                                String srcName = src.uniqueAttribute();
                                String dstName = dst.uniqueAttribute();

                                // Convert the branch (sub-flow) into a WORKER_MODULE -> MASTER_MODULE connection
                                String workerModuleConnection = srcName + " --> " + "WM-" + workerModuleCount + " --> " + dstName;

                                connectWorkerModule(
                                        application,
                                        VD_SENSOR,
                                        VD_ACTUATOR,
                                        srcName,
                                        dstName,
                                        MASTER_MODULE,
                                        isEventful
                                );

                                System.out.println("New VD SENSOR-MODULE-ACTUATOR connection made for " + virtualDevice.getFogDevice().getName());
                                System.out.println("W-M connection: " + workerModuleConnection + " added for " + virtualDevice.getFogDevice().getName());
                            } else {
                                /*
                                VD SENSOR-MODULE-ACTUATOR connection already exists for the sub-flow and as such just create
                                a new WORKER_MODULE-MASTER_MODULE connection using sub-flow branch
                                 */

                                // Get the property/action name of the src and dst node which is used as the name for sensor and actuator
                                String srcName = src.uniqueAttribute();
                                String dstName = dst.uniqueAttribute();

                                // Convert the branch (sub-flow) into a WORKER_MODULE -> MASTER_MODULE connection
                                String workerModuleConnection = srcName + " --> " + "WM-" + workerModuleCount + " --> " + dstName;

                                connectWorkerModule(
                                        application,
                                        VD_SENSOR,
                                        VD_ACTUATOR,
                                        srcName,
                                        dstName,
                                        MASTER_MODULE,
                                        isEventful
                                );

                                System.out.println("W-M connection: " + workerModuleConnection + " added to existing " + virtualDevice.getFogDevice().getName() + " SENSOR-MODULE-ACTUATOR connection.");
                            }
                        }
                    }
                }
            }
        }
        return application;
    }

    private VD getVDUsed(TopologyNode node, List<VD> vds) {
        // Get the thing referenced by the node
        TopologyNode thingUsed = null;
        for(TopologyNode thing : things) {
            if(thing.id().equals(node.thing())) thingUsed = thing;
        }

        // Using the thing node's name find its corresponding VD
        if(thingUsed != null) {
            for(VD vd : vds) {
                if(vd.getFogDevice().getName().equals(thingUsed.name())) {
                    return vd;
                }
            }
        }

        return null;
    }

    private void connectWorkerModule(
            Application application,
            String VD_SENSOR,
            String VD_ACTUATOR,
            String subFlowSensor,
            String subFlowActuator,
            String MASTER_MODULE,
            boolean isEventful
    ) {
        ////////// App Modules //////////

        String WORKER_MODULE_K;

        if(isEventful) {
            WORKER_MODULE_K = "EventWorkerModule-" + eventWorkerModuleCount;
            eventWorkerModuleCount++;
        } else {
            WORKER_MODULE_K = "WorkerModule-" + workerModuleCount;
            workerModuleCount++;
        }

        application.addAppModule(WORKER_MODULE_K, applicationPreset.APP_MODULE_RAM);

        ////////// App Edges //////////

        // From MASTER_MODULE to WORKER_MODULE_K carrying tuple types of customSensor (from sub flow)
        addAppEdge(application, MASTER_MODULE, WORKER_MODULE_K, subFlowSensor, Tuple.UP, AppEdge.MODULE);

        // From WORKER_MODULE_K to MASTER_MODULE carrying tuple types of actuatorAction (from sub flow)
        addAppEdge(application, WORKER_MODULE_K, MASTER_MODULE, subFlowActuator, Tuple.DOWN, AppEdge.MODULE);

        ////////// Tuple Mappings //////////

        // For every incoming tuple of type VD_SENSOR into the MASTER_MODULE, 1.0 tuples of type subFlowSensor are emitted
        application.addTupleMapping(MASTER_MODULE, VD_SENSOR, subFlowSensor, new FractionalSelectivity(1.0));

        // For every incoming tuple of type subFlowSensor into the WORKER_MODULE_K, 1.0 tuples of type subFlowActuator are emitted
        application.addTupleMapping(WORKER_MODULE_K, subFlowSensor, subFlowActuator, new FractionalSelectivity(1.0));

        // For every incoming tuple of type subFlowActuator into the MASTER_MODULE, 1.0 tuples of type VD_ACTUATOR are emitted
        application.addTupleMapping(MASTER_MODULE, subFlowActuator, VD_ACTUATOR, new FractionalSelectivity(1.0));

        ////////// App Loop //////////

        // Create the app loop which defines the flow of tuples from component to component
        final AppLoop loop = new AppLoop(new ArrayList<>(){{add(VD_SENSOR);add(MASTER_MODULE);/*add(subFlowSensor);*/add(WORKER_MODULE_K);/*add(subFlowActuator);*/add(MASTER_MODULE);add(VD_ACTUATOR);}});

        // Add the created app loop to the appLoops list
        application.getLoops().add(loop);
    }

    private boolean isActuator(TopologyNode node) {
        if(!isWoTNode(node)) return false;

        return node.type().equals(NodeRedJSONParser.TYPE_INVOKE_ACTION) || node.type().equals(NodeRedJSONParser.TYPE_WRITE_PROPERTY);
    }

    private boolean isSensor(TopologyNode node) {
        if(!isWoTNode(node)) return false;

        return node.type().equals(NodeRedJSONParser.TYPE_READ_PROPERTY) || node.type().equals(NodeRedJSONParser.TYPE_SUBSCRIBE_EVENT);
    }

    private static boolean isWoTNode(TopologyNode node) {
        return node.thing() != null && !node.thing().isEmpty();
    }


    private void addAppEdge(Application application, String srcName, String dstName, String tupleType, int edgeDirection, int edgeType) {
        application.addAppEdge(
                srcName,
                dstName,
                applicationPreset.APP_EDGE_TUPLE_CPU_LENGTH,  // Processing latency
                applicationPreset.APP_EDGE_TUPLE_NW_LENGTH,   // Transmission latency
                tupleType,
                edgeDirection,
                edgeType
        );
    }

    ////////////////////////////////////// APPLICATION PHYSICAL TOPOLOGY CONSTRUCTION //////////////////////////////////////

    public ApplicationPhysicalTopology createApplicationPhysicalTopology(List<VD> virtualDevices) {
        // Will store all fog devices used in the application
        List<FogDevice> fogDevices = new ArrayList<>();

        // Will store all edge nodes used in the application
        List<FogDevice> edgeNodes = new ArrayList<>();

        try {
            // Search for selected VDs based on things list
            List<VD> allSelectedVirtualDevices = getSelectedVirtualDevices(virtualDevices, things);

            if(allSelectedVirtualDevices == null) throw new Error("No virtual devices exist for things specified!");

            // Set the selected virtual devices
            selectedVirtualDevices.addAll(allSelectedVirtualDevices);

            // Lists to store used sensors and actuators
            List<Sensor> allSensorsUsedInApplication = new ArrayList<>();
            List<Actuator> allActuatorsUsedInApplication = new ArrayList<>();

            // Iterate once over VDs to collect sensors & actuators
            for (VD virtualDevice : selectedVirtualDevices) {
                allSensorsUsedInApplication.add(virtualDevice.getSensor());

                allActuatorsUsedInApplication.add(virtualDevice.getActuator());
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
            cloud.setLevel(1);

            fogDevices.add(cloud);

            if(!nodeTopics.isEmpty()) { // If the topics array is set
                // Assign VDs to edge nodes based on topics
                for(String topic : nodeTopics) {
                    // Create the edge node for that topic
                    FogDevice edgeNode = createEdgeNode(topic);

                    // Link the edge node to the cloud
                    edgeNode.setParentId(cloud.getId());
                    edgeNode.setUplinkLatency(applicationPreset.UPLINK_LATENCY_EDGE_TO_CLOUD);
                    edgeNode.setLevel(2);
                    fogDevices.add(edgeNode);
                    edgeNodes.add(edgeNode);

                    List<VD> virtualDevicesUsed = getVirtualDeviceWithMatchingTopic(allTopologyNodes, allSelectedVirtualDevices, topic);

                    assert virtualDevicesUsed != null;
                    for(VD virtualDevice : virtualDevicesUsed) {
                        FogDevice vdFogDevice = null;

                        if(virtualDevice != null) {
                            vdFogDevice = virtualDevice.getFogDevice();
                        }

                        if(vdFogDevice != null) {
                            vdFogDevice.setParentId(edgeNode.getId());
                            vdFogDevice.setUplinkLatency(applicationPreset.UPLINK_LATENCY_VD_TO_EDGE);
                            vdFogDevice.setLevel(3);
                            fogDevices.add(vdFogDevice);
                        }
                    }
                }
            } else { // If the topics array is not set, distribute nodes normally
                // Calculate the number of edge nodes needed
                int numberOfEdgeNodes = Math.max(1, calculateNoOfEdgeNodes(selectedVirtualDevices.size(), applicationPreset.MAX_VDS_FOR_ONE_EDE_NODE));

                // Create a list of edge nodes (each edge node is represented as a list of VDs)
                List<List<VD>> edgeNodeList = new ArrayList<>();

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
                    edgeNode.setLevel(2);
                    fogDevices.add(edgeNode);
                    edgeNodes.add(edgeNode);

                    // Connect the VDs to the edge node
                    for(VD virtualDevice : edgeNodeList.get(index)) {
                        FogDevice vdFogDevice = virtualDevice.getFogDevice();

                        vdFogDevice.setParentId(edgeNode.getId());

                        vdFogDevice.setUplinkLatency(applicationPreset.UPLINK_LATENCY_VD_TO_EDGE);

                        vdFogDevice.setLevel(3);

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
        List<String> includeTypes = new ArrayList<>(){{
            add(NodeRedJSONParser.TYPE_READ_PROPERTY);
            add(NodeRedJSONParser.TYPE_INVOKE_ACTION);
            add(NodeRedJSONParser.TYPE_WRITE_PROPERTY);
            add(NodeRedJSONParser.TYPE_SUBSCRIBE_EVENT);
        }};

        for(TopologyNode node : nodes) {
            if(includeTypes.contains(node.type())) attributeNames.add(node.uniqueAttribute());
        }

        return attributeNames;
    }

    private int calculateNoOfEdgeNodes(int numberOfVDs, int maxNoVDsForOneEdgeNode) {
        // CHANGE FORMULA AS YOU SEE FIT
        return (int)(numberOfVDs - maxNoVDsForOneEdgeNode) / 2;
    }

    private List<VD> getSelectedVirtualDevices(List<VD> virtualDevices, List<TopologyNode> things) {
        List<VD> selectedVirtualDevices = new ArrayList<>();

        for(VD virtualDevice : virtualDevices) {
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

    /**
     * Gets all virtual devices with the matching topic
     * @param nodes All nodes used in the application
     * @param allVirtualDevices All virtual devices used in the application
     * @param targetTopic The target topic to search for
     * @return A list of all virtual devices counterparts of the (referenced) things that contain the matching topic
     */
    private List<VD> getVirtualDeviceWithMatchingTopic(List<TopologyNode> nodes, List<VD> allVirtualDevices, String targetTopic) {
        if (targetTopic == null || targetTopic.isEmpty()) {
            System.out.println("Topic must not be empty!");
            return null;
        }

        // Get all nodes that have a matching topic attribute
        Set<TopologyNode> matchingNodesWithTopic = new HashSet<>();

        for(TopologyNode topologyNode : nodes) {
            if(topologyNode.topic() != null && topologyNode.topic().equals(targetTopic)) {
                matchingNodesWithTopic.add(topologyNode);
            }
        }

        // Get all things referenced (stores thing id) by the nodes with a matching topic
        Set<String> thingsRef = new HashSet<>();

        for(TopologyNode node : matchingNodesWithTopic) {
            if(node.thing() != null && !node.thing().isEmpty()) {
                thingsRef.add(node.thing());
            }
        }

        // Get the things (topology nodes) referenced
        Set<TopologyNode> thingNodes = new HashSet<>();
        for(TopologyNode thingNode : this.things) {
            if(thingsRef.contains(thingNode.id())) {
                thingNodes.add(thingNode);
            }
        }

        // Get the corresponding virtual devices of the things
        Set<VD> virtualDevices = new HashSet<>();

        // Get all things used for that topic (topic-node) and get its corresponding virtual device
        for(TopologyNode thing : thingNodes) {
            String formattedThingName = thing.name().replace(" ", "");
            virtualDevices.add(Utility.getVD(allVirtualDevices, formattedThingName));
        }

        return !virtualDevices.isEmpty() ? new ArrayList<>(virtualDevices) : null;
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

    public List<TopologyNode> getAllTopologyNodes() {
        return allTopologyNodes;
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

    public List<VD> getSelectedVirtualDevices() {
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

