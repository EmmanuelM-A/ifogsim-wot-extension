package com.extensions.sysconstructor.core;

import com.extensions.customfog.CustomFogDevice;
import com.extensions.customfog.FogDeviceFactory;
import com.extensions.sysconstructor.eventdriver.EventManager;
import com.extensions.sysconstructor.eventdriver.EventSensor;
import com.extensions.sysconstructor.nodered.NodeRedJSONParser;
import com.extensions.sysconstructor.nodered.NodeRedTranslator;
import com.extensions.sysconstructor.topology.TopologyNode;
import com.extensions.sysconstructor.topology.TopologyNodeConnectionChecker;
import com.extensions.sysconstructor.topology.TopologyNodeConnectionStatus;
import com.extensions.sysconstructor.topology.TopologyNodeTree;
import com.extensions.utils.Utility;
import com.extensions.utils.presets.ApplicationPreset;
import com.extensions.utils.presets.CloudNodePreset;
import com.extensions.utils.presets.EdgeNodePreset;
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

/**
 * This class is responsible for construction the application's 1) Physical Topology and its 2) Application Model
 */
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
     * The parser responsible for extracting information from application topology file.
     */
    private final ApplicationTopologyParser applicationTopologyParser;

    /**
     * The parser responsible for extracting the data from the VD quantities file.
     */
    private final ThingQuantityParser thingQuantityParser;

    /**
     * A list of all virtual devices used in the application
     */
    private final List<VirtualDevice> selectedVirtualDevices;

    /**
     * A count used to keep track of all processing modules created
     */
    private final int processingModuleCount;

    /**
     * A count used to keep track of all event processing modules created
     */
    private final int eventProcessingModuleCount;

    /**
     * Keeps track of the number of worker modules created.
     */
    private int workerModuleCount = 1;

    /**
     * A list of all sensor used in the application's physical topology (ONLY USED IN THE APPLICATION MODEL).
     */
    private List<Sensor> allSensors;

    /**
     * Initializes all variables and components used to construct the application.
     * @param cloudNodePreset The preset configurations used to configure a cloud node.
     * @param edgeNodePreset The preset configurations used to configure the edge nodes.
     * @param applicationPreset The preset configurations used to configure the application and application components.
     * @param nodeRedApplicationJsonFile The JSON file that contains the Node-RED application design.
     * @param thingQuantityParser The parser used to extract and transform the data from the VD quantities file.
     * @throws IOException Throws an en exception if an error occurs or during processing or whilst reading or parsing the file
     */
    public JsonToApplication(
            CloudNodePreset cloudNodePreset,
            EdgeNodePreset edgeNodePreset,
            ApplicationPreset applicationPreset,
            File nodeRedApplicationJsonFile,
            ThingQuantityParser thingQuantityParser
    ) throws IOException {
        this.cloudNodePreset = cloudNodePreset;
        this.edgeNodePreset = edgeNodePreset;
        this.applicationPreset = applicationPreset;
        this.thingQuantityParser = thingQuantityParser;

        // Initialize variables and data structures
        this.dataFlows = new ArrayList<>();
        this.eventFlows = new ArrayList<>();
        this.injectFlows = new ArrayList<>();
        this.selectedVirtualDevices = new ArrayList<>();
        this.processingModuleCount = 0;
        this.eventProcessingModuleCount = 0;
        this.allSensors = null;

        // Convert the Node-RED application description into a structured input format
        NodeRedTranslator.nodeRedToInputJson(nodeRedApplicationJsonFile);

        // Initialize the topology parser using the generated application topology file
        this.applicationTopologyParser = new ApplicationTopologyParser(new File(NodeRedTranslator.APPLICATION_TOPOLOGY));

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

    // TODO - LOOK INTO WHY TUPLE EXECUTION DELAY & APP LOOP DELAY DON'T DISPLAY WHEN CLOUD = FALSE

    //////////////////////////////////////////// APPLICATION MODEL CONSTRUCTION ////////////////////////////////////////////

    /**
     * Creates the application model for the application.
     * @param appId The application ID.
     * @param userId The user ID associated with the application.
     *
     * @return The constructed application model from the Node-RED application.
     */
    public Application createApplicationModel(String appId, int userId) {
        Application application = mapAppTopologyToAppModel(appId, userId);

        addEventFlowToApplication(application);

        System.out.println("Application's Application Model Constructed Successfully!");

        return application;
    }

    /**
     * Maps the Node-RED application design to an iFogSim acceptable application model.
     * @param appId The application ID.
     * @param userId The user ID associated with the application.
     *
     * @return The constructed application model.
     */
    private Application mapAppTopologyToAppModel(String appId, int userId) {
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
        Set<VirtualDevice> virtualDevicesUsedSoFar = new HashSet<>();

        for(TopologyNodeTree topologyNodeTree : dataFlows) {
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

                        // Get the property/action name of the src and dst node which is used as the name for sensor and actuator
                        String srcName = src.uniqueAttribute();
                        String dstName = dst.uniqueAttribute();

                        String WORKER_MODULE_K = "WorkerModule-" + workerModuleCount;
                        workerModuleCount++;

                        //if(topologyNodeTree.branches().size() == 1) {
                            // Processing Module
                            /*application.addAppModule(WORKER_MODULE_K, applicationPreset.APP_MODULE_RAM);

                            // Data Flow Edges
                            addAppEdge(application, srcName, WORKER_MODULE_K, srcName, Tuple.UP, AppEdge.SENSOR);
                            addAppEdge(application, WORKER_MODULE_K, dstName, dstName, Tuple.DOWN, AppEdge.ACTUATOR);

                            // Tuple Mappings
                            application.addTupleMapping(WORKER_MODULE_K, srcName, dstName, new FractionalSelectivity(1.0));

                            // Define Application Loops
                            final AppLoop loop = new AppLoop(Arrays.asList(srcName, WORKER_MODULE_K, dstName));

                            application.setLoops(new ArrayList<>(){{add(loop);}});

                            break;*/
                        //} else if(branches.size() > 1) {
                            VirtualDevice virtualDevice = getVDUsed(src, selectedVirtualDevices);

                            // Check if VD exists
                            if(virtualDevice != null) {
                                // Get the VD_SENSOR and VD_ACTUATOR, which both represent all sensors and actuator for that VD respectively
                                String VD_SENSOR = virtualDevice.getSensor().getName();
                                String VD_ACTUATOR = virtualDevice.getActuator().getName();

                                // Check if the VD has not been used before (means VD SENSOR-MODULE-ACTUATOR for that sub-flow has not been created yet)
                                if(!virtualDevicesUsedSoFar.contains(virtualDevice)) {
                                    /*
                                        Once the VD_SENSOR -> MASTER_MODULE -> VD_ACTUATOR connection is set up for a given VD, all branches (from that
                                        sub-flow) that use that VD (meaning the sensor is from that VD), will be converted into a worker module, so that
                                        data flowing through the sub-flow is still incorporated into the application model. If a sub-flow branch has an existing
                                        VD SENSOR-MODULE-ACTUATOR connection already, the sub-flow branch is just converted into another WORKER_MODULE-MASTER_MODULE
                                        connection for the existing VD SENSOR-MODULE-ACTUATOR connection.
                                    */

                                    // Make an edge from VD_SENSOR to MASTER_MODULE carrying tuple types of VD_SENSOR
                                    addAppEdge(application, VD_SENSOR, MASTER_MODULE, VD_SENSOR, Tuple.UP, AppEdge.SENSOR);

                                    // Make an edge from MASTER_MODULE to VD_ACTUATOR, carrying tuple types of VD_ACTUATOR
                                    addAppEdge(application, MASTER_MODULE, VD_ACTUATOR, VD_ACTUATOR, Tuple.DOWN, AppEdge.ACTUATOR);

                                    // Record the VD
                                    virtualDevicesUsedSoFar.add(virtualDevice);

                                    //System.out.println("New VD SENSOR-MODULE-ACTUATOR connection made for " + virtualDevice.getFogDevice().getName());
                                }

                                /*
                                    VD SENSOR-MODULE-ACTUATOR connection already exists for the sub-flow and as such just create
                                    a new WORKER_MODULE-MASTER_MODULE connection using sub-flow branch
                                 */
                                String workerModuleConnection = connectWorkerModule(
                                        application,
                                        VD_SENSOR,
                                        VD_ACTUATOR,
                                        srcName,
                                        dstName,
                                        MASTER_MODULE,
                                        WORKER_MODULE_K
                                );
                                //System.out.println("WORKER-MODULE connection: " + workerModuleConnection + " added for " + virtualDevice.getFogDevice().getName());
                            }
                        //}
                    }
                }
            }
        }

        return application;
    }

    /**
     * Maps the user-defined event flows (from the Node-RED application design) into iFogSim acceptable application model
     * components which capture the data flow that occurs when the event is triggered.
     * @param application The application to add these event flows to.
     */
    private void addEventFlowToApplication(Application application) {
        for(TopologyNodeTree topologyNodeTree : eventFlows) {
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

            // Get first branch (Assuming there's always going to be at least one branch)
            List<TopologyNode> branch = branches.getFirst();

            // Get the src and dst node
            TopologyNode src = branch.getFirst(); // Assumed to be sensor
            TopologyNode dst = branch.getLast(); // Assumed to be actuator

            //System.out.println("BEFORE: " + src.uniqueAttribute() + " ---> " + dst.uniqueAttribute());

            // Check if the src node is a sensor and if the dst node is an actuator
            if(isSensor(src) && isActuator(dst)) {
                // Check the connectivity between the src and dst nodes
                TopologyNodeConnectionStatus connectionStatus = TopologyNodeConnectionChecker.areNodesConnected(src.id(), dst.id());

                // For this level of complexity, I only care if there exists some connection between two nodes (whether its direct or indirect)
                if(connectionStatus.isThereAConnection()) {
                    //if(connectionStatus.isDirectionConnection()) {} // You can change it to handle direct connections

                    // Get the property/action name of the src and dst node which is used as the name for sensor and actuator
                    String srcName = src.uniqueAttribute();
                    String dstName = dst.uniqueAttribute();

                    if(branches.size() == 1) {
                        recordEventFlow(application, srcName, dstName);

                        //System.out.println("AFTER: " + srcName + " ---> " + dstName);
                    } else if(branches.size() > 1) {
                        // Get the VD that holds the src node (sensor)
                        VirtualDevice virtualDevice = getVDUsed(src, selectedVirtualDevices);

                        if(virtualDevice != null) {
                            // Get the VD_ACTUATOR, which represents all actuators for that VD respectively
                            String VD_ACTUATOR = virtualDevice.getActuator().getName();

                            recordEventFlow(application, srcName, VD_ACTUATOR);

                            //System.out.println("AFTER: " + srcName + " ---> " + VD_ACTUATOR);
                        }
                    }
                }
            }
        }

        // Trigger all events
        for(EventSensor eventSensor : EventManager.getInstance().getEventSensors()) {
            //System.out.println(eventSensor + " TRIGGERED!");
            EventManager.getInstance().triggerEvent(eventSensor.getName());
        }
    }

    /**
     * Responsible for adding and registering the events to the application.
     * @param application The application to add the event to.
     * @param srcName The source of the event.
     * @param dstName The destination the event tuple transmitted should travel to.
     */
    private void recordEventFlow(Application application, String srcName, String dstName) {
        String eventProcessor = "EventProcessor";

        if(!application.getModules().contains(application.getModuleByName(eventProcessor))) {
            application.addAppModule(eventProcessor, applicationPreset.APP_MODULE_RAM);
        }

        // Register event
        EventSensor eventSensor =  (EventSensor) getSensorBy(srcName, allSensors);
        EventManager.getInstance().registerEventSensor(eventSensor);

        // Connect src to event processor
        addAppEdge(application, srcName, eventProcessor, srcName, Tuple.UP, AppEdge.SENSOR);

        // Connect event processor to dst
        addAppEdge(application, eventProcessor, dstName, dstName, Tuple.DOWN, AppEdge.ACTUATOR);

        application.addTupleMapping(eventProcessor, srcName, dstName, new FractionalSelectivity(1.0));

        final AppLoop loop = new AppLoop(new ArrayList<>(){{add(srcName);add(eventProcessor);add(dstName);}});
        application.getLoops().add(loop);
    }

    /**
     * Sets the list of all sensors used in the application (ONLY USED FOR THE APPLICATION MODEL!).
     * @param sensors The list of all sensors used in the application.
     */
    public void setAllSensors(List<Sensor> sensors) {
        this.allSensors = sensors;
    }

    /**
     * Gets the VDs associated with the topology node.
     * @param node The node associated with the VD.
     * @param vds The list of all VDs used in the application.
     * @return The VD that is linked to the node.
     */
    private VirtualDevice getVDUsed(TopologyNode node, List<VirtualDevice> vds) {
        // Get the thing referenced by the node
        TopologyNode thingUsed = null;
        for(TopologyNode thing : things) {
            if(thing.id().equals(node.thing())) thingUsed = thing;
        }

        // Using the thing node's name find its corresponding VD
        if(thingUsed != null) {
            for(VirtualDevice vd : vds) {
                String formattedThingName = thingUsed.name().replace(" ", "");
                if(vd.getFogDevice().getName().contains(formattedThingName) || vd.getFogDevice().getName().equals(formattedThingName)) {
                    if(thingQuantityParser.getVdsConnectedToEdgeNodes() != null) {
                        vds.remove(vd); // Prevents the same VD being selected
                    }

                    return vd;
                }
            }
        }

        return null;
    }

    /**
     * Connects the master module to the worker module.
     * @param application The application to add this connection to.
     * @param VD_SENSOR The general sensor that represents the VD.
     * @param VD_ACTUATOR The general actuator that represents the VD.
     * @param subFlowSensor The sensor found at the start of sub flow.
     * @param subFlowActuator The actuator usually found at the end of the sub flow.
     * @param MASTER_MODULE The name of the master module.
     * @param WORKER_MODULE_K The name of the worker module to make a connection with the master module.
     * @return A string detailing the connection and tuple types flowing through the worker module (USED FOR DEBUGGING).
     */
    private String connectWorkerModule(
            Application application,
            String VD_SENSOR,
            String VD_ACTUATOR,
            String subFlowSensor,
            String subFlowActuator,
            String MASTER_MODULE,
            String WORKER_MODULE_K
    ) {
        ////////// App Modules //////////

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
        final AppLoop loop = new AppLoop(new ArrayList<>(){{add(VD_SENSOR);add(MASTER_MODULE);add(subFlowSensor);add(WORKER_MODULE_K);add(subFlowActuator);add(MASTER_MODULE);add(VD_ACTUATOR);}});

        // Add the created app loop to the appLoops list
        application.getLoops().add(loop);

        return subFlowSensor + " --> " + WORKER_MODULE_K + " --> " + subFlowActuator; // USED FOR DEBUGGING
    }

    /**
     * Gets a sensor by its name form a list of sensors.
     * @param name The name of the sensor to be found.
     * @param sensors The list of all sensors to search through.
     * @return The sensor found.
     */
    private Sensor getSensorBy(String name, List<Sensor> sensors) {
        for(Sensor sensor : sensors) {
            if(sensor.getName().equals(name)) return sensor;
        }
        return null;
    }

    /**
     * Determines if a topology node is an actuator or not.
     * @param node The topology node being checked.
     * @return True if the topology node is an actuator and false otherwise.
     */
    private boolean isActuator(TopologyNode node) {
        if(!isWoTNode(node)) return false;

        return node.type().equals(NodeRedJSONParser.TYPE_INVOKE_ACTION) || node.type().equals(NodeRedJSONParser.TYPE_WRITE_PROPERTY);
    }

    /**
     * Determines if a topology node is a sensor or not.
     * @param node The topology node being checked.
     * @return True if the topology node is an actuator and false otherwise.
     */
    private boolean isSensor(TopologyNode node) {
        if(!isWoTNode(node)) return false;

        return node.type().equals(NodeRedJSONParser.TYPE_READ_PROPERTY) || node.type().equals(NodeRedJSONParser.TYPE_SUBSCRIBE_EVENT);
    }

    /**
     * Determines if a topology is a WoT node (Does it belong to the palette of nodes found in the node-red-node-wot Node-RED
     * extension) or not.
     * @param node The topology node being checked.
     * @return True if a topology node is a WoT node or false otherwise.
     */
    private static boolean isWoTNode(TopologyNode node) {
        return node.thing() != null && !node.thing().isEmpty();
    }

    /**
     * Adds an edge between a source and destination and defines all edge parameters need to create an application edge.
     * @param application The application to add an edge to.
     * @param srcName The source which the edge starts from.
     * @param dstName The destination which the edge ends at.
     * @param tupleType The tuple type being transmitted across the edge from source to destination.
     * @param edgeDirection Determines the direction the tuple travels.
     * @param edgeType Determines what type of edge the edge is.
     */
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

    /**
     * Constructs the application's physical topology given the list of virtual devices used in the application.
     * @param virtualDevices The list of all virtual devices used in the application.
     * @return The psychical topology instance.
     */
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

            //selectedVirtualDevicesForTopology.addAll(allSelectedVirtualDevices);

            // Get the names of all sensors and actuators used in the application
            Set<String> sensorsAndActuatorsUsed = new HashSet<>(getAllSensorsAndActuatorsUsed(allTopologyNodes));

            // Sets to store unique sensors and actuators used in the application
            Set<Sensor> allSensorsUsedInApplication = new HashSet<>();
            Set<Actuator> allActuatorsUsedInApplication = new HashSet<>();

            // Additional sets to track sensor and actuator names (for preventing duplicates)
            Set<String> uniqueSensorNames = new HashSet<>();
            Set<String> uniqueActuatorNames = new HashSet<>();

            // Iterate once over Virtual Devices to collect sensors & actuators uniquely
            for (VirtualDevice virtualDevice : allSelectedVirtualDevices) {
                // Add the general sensor (if not already added)
                Sensor generalSensor = virtualDevice.getSensor();
                if (uniqueSensorNames.add(generalSensor.getName())) {  // If new, add to set
                    allSensorsUsedInApplication.add(generalSensor);
                }

                // Add the general actuator (if not already added)
                Actuator generalActuator = virtualDevice.getActuator();
                if (uniqueActuatorNames.add(generalActuator.getName())) {
                    allActuatorsUsedInApplication.add(generalActuator);
                }

                // Iterate over sensor properties
                for (Sensor sensor : virtualDevice.getSensorProperties()) {
                    if (sensorsAndActuatorsUsed.contains(sensor.getName()) && uniqueSensorNames.add(sensor.getName())) {
                        allSensorsUsedInApplication.add(sensor);
                    }
                }

                // Iterate over actuator actions
                for (Actuator actuator : virtualDevice.getActuatorActions()) {
                    if (sensorsAndActuatorsUsed.contains(actuator.getName()) && uniqueActuatorNames.add(actuator.getName())) {
                        allActuatorsUsedInApplication.add(actuator);
                    }
                }

                // Iterate over event sensors
                for (Sensor sensor : virtualDevice.getEventSensors()) {
                    if (sensorsAndActuatorsUsed.contains(sensor.getName()) && uniqueSensorNames.add(sensor.getName())) {
                        allSensorsUsedInApplication.add(sensor);
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
            cloud.setLevel(1);

            fogDevices.add(cloud);

            if(thingQuantityParser.getVdsConnectedToEdgeNodes() != null && !thingQuantityParser.getVdsConnectedToEdgeNodes().keySet().isEmpty()) {
            //if(!nodeTopics.isEmpty()) { // If the topics array is set
                // Assign VDs to edge nodes based on topics
                for(String edgeNodeName : thingQuantityParser.getVdsConnectedToEdgeNodes().keySet()) {
                //for(String edgeNodeName : nodeTopics) {
                    // Create the edge node for that topic
                    FogDevice edgeNode = createEdgeNode(edgeNodeName);

                    // Link the edge node to the cloud
                    edgeNode.setParentId(cloud.getId());
                    edgeNode.setUplinkLatency(applicationPreset.UPLINK_LATENCY_EDGE_TO_CLOUD);
                    edgeNode.setLevel(2);
                    fogDevices.add(edgeNode);
                    edgeNodes.add(edgeNode);

                    List<VirtualDevice> virtualDevicesUsed = getVirtualDevicesWithMatchingEdgeNodeName(allSelectedVirtualDevices, thingQuantityParser.getVdsConnectedToEdgeNodes(), edgeNodeName);

                    assert virtualDevicesUsed != null;

                    for(VirtualDevice virtualDevice : virtualDevicesUsed) {
                        CustomFogDevice vdFogDevice = null;

                        if(virtualDevice != null) {
                            vdFogDevice = virtualDevice.getFogDevice();
                        }

                        // Connect the VD's fog device to the specified edge node
                        if(vdFogDevice != null) {
                            vdFogDevice.setParentId(edgeNode.getId());
                            vdFogDevice.setUplinkLatency(applicationPreset.UPLINK_LATENCY_VD_TO_EDGE);
                            vdFogDevice.setLevel(3);
                            fogDevices.add(vdFogDevice);
                            //System.out.println("The VD " + vdFogDevice.getName() + " has been connected to " + edgeNodeName);
                        }
                    }
                }
            } else { // If the topics array is not set, use default node distribution
                // Calculate the number of edge nodes needed
                int numberOfEdgeNodes = Math.max(1, calculateNoOfEdgeNodes(allSelectedVirtualDevices.size()));

                // Create a list of edge nodes (each edge node is represented as a list of VDs)
                List<List<VirtualDevice>> edgeNodeList = new ArrayList<>();

                // Distribute virtual devices among edge nodes
                for (int i = 0; i < numberOfEdgeNodes; i++) {
                    edgeNodeList.add(new ArrayList<>());
                }

                for (int i = 0; i < allSelectedVirtualDevices.size(); i++) {
                    // Assign each virtual device to an edge node in a round-robin manner
                    edgeNodeList.get(i % numberOfEdgeNodes).add(allSelectedVirtualDevices.get(i));
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
                    for(VirtualDevice virtualDevice : edgeNodeList.get(index)) {
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
            applicationPhysicalTopology.setSensors(new ArrayList<>(allSensorsUsedInApplication));
            applicationPhysicalTopology.setActuators(new ArrayList<>(allActuatorsUsedInApplication));
            applicationPhysicalTopology.setEdgeNodes(edgeNodes);

            System.out.println("Application's Physical Topology Constructed Successfully!");

            return applicationPhysicalTopology;
        } catch(Exception e) {
            System.out.println("Application's Physical Topology Constructed Unsuccessfully!");
            System.out.println(e.getMessage());
            e.printStackTrace();
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

    /**
     * Determines the number of edge nodes needed for an application depending on the number of VDs.
     * @param numberOfVDs The number of VDs used in the application.
     * @return The number of edge nodes.
     */
    private int calculateNoOfEdgeNodes(int numberOfVDs) {
        // CHANGE FORMULA AS YOU SEE FIT
        return (numberOfVDs - applicationPreset.MAX_VDS_FOR_ONE_EDE_NODE) / 2;
    }

    /**
     * Gets all selected virtual devices from the application based on the things used in to construct the Node-RED application.
     * @param virtualDevices A list of VDs.
     * @param things The list of things (represented as topology nodes) used in the application.
     * @return A list of VDs used in the application.
     */
    private List<VirtualDevice> getSelectedVirtualDevices(List<VirtualDevice> virtualDevices, List<TopologyNode> things) {
        List<VirtualDevice> selectedVirtualDevices = new ArrayList<>();

        for(VirtualDevice virtualDevice : virtualDevices) {
            String name = virtualDevice.getFogDevice().getName();

            for(TopologyNode thing : things) {
                String formattedThingName = thing.name().replace(" ", "");

                if(name.contains(formattedThingName)) selectedVirtualDevices.add(virtualDevice);
            }
        }

        return selectedVirtualDevices.isEmpty() ? null : selectedVirtualDevices;
    }

    /**
     * Creates an edge node using the edge node presets.
     * @param identifier The identifier used to distinguish the edge node amongst other nodes.
     * @return A fog device which represents the edge node.
     */
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
     * Gets a list of virtual devices that are connected to a particular edge node.
     * @param allVirtualDevices The list of all available VDs in the application.
     * @param vdsConnectedToEdgeNodes The map that defines the VD names that connect to an edge node.
     * @param edgeNodeName The name od the edge node to find VDs that connect to it.
     * @return A list of VDs that connect to the edge node.
     */
    private List<VirtualDevice> getVirtualDevicesWithMatchingEdgeNodeName(List<VirtualDevice> allVirtualDevices, Map<String, List<String>> vdsConnectedToEdgeNodes, String edgeNodeName) {
        if (edgeNodeName == null || edgeNodeName.isEmpty()) throw new Error("The edge node " + edgeNodeName + " does not exist! Ensure edge nodes specified in the VD Quantities file match the edge nodes specified in the Node-RED application topics field.");

        Set<VirtualDevice> virtualDevices = new HashSet<>();

        if(vdsConnectedToEdgeNodes == null) throw new IllegalArgumentException("VD Quantities file is empty or missing!");

        // Get the VD (names) connected to this edge node
        List<String> vdsConnectedToEdgeNode = vdsConnectedToEdgeNodes.getOrDefault(edgeNodeName, null);

        if(vdsConnectedToEdgeNode == null) throw new Error("The edge node " + edgeNodeName + " does not exist! Check Thing Quantities file!");

        // Get the VDs based of their names
        for(VirtualDevice virtualDevice : allVirtualDevices) {
            if(vdsConnectedToEdgeNode.contains(virtualDevice.getFogDevice().getName())) {
                virtualDevices.add(virtualDevice);

                //System.out.println("The VD: " + virtualDevice.getFogDevice().getName() + " found for the edge node " + edgeNodeName);
            }
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

    /**
     * Retrieves all things (parsed from the application topology).
     *
     * @return A list of all things extracted.
     */
    public List<TopologyNode> getThings() {
        return things;
    }

    /**
     * Retrieves a list of all node topics used (parsed from the application topology).
     *
     * @return Al ist of all node topics.
     */
    public List<String> getNodeTopics() {
        return nodeTopics;
    }

    /**
     * Retrieves a list of topology nodes (parsed from the application topology).
     *
     * @return A list of {@link TopologyNodeTree} representing the topology nodes in the application topology.
     */
    public List<TopologyNode> getAllTopologyNodes() {
        return allTopologyNodes;
    }

    /**
     * Retrieves a list of all topology node trees (similar to the structure of the system).
     *
     * @return A list of {@link TopologyNodeTree} representing the node trees in the topology.
     */
    public List<TopologyNodeTree> getTopologyNodeTrees() {
        return topologyNodeTrees;
    }

    /**
     * Retrieves a list of injection flows in the system, represented as topology node trees.
     *
     * @return A list of {@link TopologyNodeTree} representing the injected flows in the topology.
     */
    public List<TopologyNodeTree> getInjectFlows() {
        return injectFlows;
    }

    /**
     * Retrieves a list of event flows in the system, represented as topology node trees.
     *
     * @return A list of {@link TopologyNodeTree} representing the event flows in the topology.
     */
    public List<TopologyNodeTree> getEventFlows() {
        return eventFlows;
    }

    /**
     * Retrieves the application topology parser responsible for parsing the system's topology.
     *
     * @return An instance of {@link ApplicationTopologyParser} used to parse the application's topology.
     */
    public ApplicationTopologyParser getApplicationTopologyParser() {
        return applicationTopologyParser;
    }

    /**
     * Retrieves the list of selected virtual devices in the system.
     *
     * @return A list of {@link VirtualDevice} representing the virtual devices that have been selected.
     */
    public List<VirtualDevice> getSelectedVirtualDevices() {
        return selectedVirtualDevices;
    }

    /**
     * Retrieves the count of processing modules in the system.
     *
     * @return The number of processing modules in the system.
     */
    public int getProcessingModuleCount() {
        return processingModuleCount;
    }

}
