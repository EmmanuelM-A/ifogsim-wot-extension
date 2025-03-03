package com.extensions.sysconstructor.core;

import com.extensions.sysconstructor.nodered.NodeRedJSONParser;
import com.extensions.sysconstructor.topology.TopologyNode;
import com.extensions.sysconstructor.topology.TopologyNodeTree;
import com.extensions.utils.processors.FileProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for parsing and extracting the information from the application topology
 * file. The application topology file describes the structure and components of an IoT application.
 */
public class ApplicationTopologyParser implements FileProcessor<JsonNode> {
    /**
     *  The object mapper instance used to parse JSON files.
     */
    private final ObjectMapper objectMapper;

    /**
     * The main JsonNode which contains the application topology JSON object.
     */
    private final JsonNode applicationTopology;

    /**
     * The list of topology nodes from the application topology to include (upon parsing and extraction).
     */
    private final List<String> topologyNodeTypesToInclude;

    /**
     * Constructs an ApplicationTopologyParser with the specified application topology file.
     *
     * @param applicationTopologyFile The JSON file containing the application topology.
     * @throws IOException If an error occurs while reading or parsing the file.
     */
    public ApplicationTopologyParser(File applicationTopologyFile) throws IOException {
        this.objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.topologyNodeTypesToInclude = new ArrayList<>();

        topologyNodeTypesToInclude.add(NodeRedJSONParser.TYPE_INVOKE_ACTION);
        topologyNodeTypesToInclude.add(NodeRedJSONParser.TYPE_READ_PROPERTY);
        topologyNodeTypesToInclude.add(NodeRedJSONParser.TYPE_WRITE_PROPERTY);
        topologyNodeTypesToInclude.add(NodeRedJSONParser.TYPE_SUBSCRIBE_EVENT);
        topologyNodeTypesToInclude.add(NodeRedJSONParser.TYPE_INJECT);
        topologyNodeTypesToInclude.add(NodeRedJSONParser.TYPE_CONSUMED_THING);

        this.applicationTopology = process(applicationTopologyFile);
    }

    /**
     * Processes the given file and returns a JsonNode representing the parsed content.
     *
     * @param file The file to process.
     * @return A JsonNode representing the parsed content.
     * @throws IOException If an error occurs while reading or parsing the file.
     */
    @Override
    public JsonNode process(File file) throws IOException {
        return objectMapper.readTree(file);
    }

    /**
     * Parses the application title from the application topology.
     *
     * @return The application title, or "IoT-Application" if not found.
     * @throws JsonProcessingException If an error occurs while processing the JSON data.
     */
    public String parseApplicationTitle() throws JsonProcessingException {
        // Get the JsonNode that contains the application details
        JsonNode applicationDetails = applicationTopology.get("applicationDetails");

        // Make sure the JsonNode is valid
        if(applicationDetails == null || !applicationDetails.has("title")) return null;

        // Return the application title, else the default value
        return applicationDetails.get("title").asText("IoT-Application");
    }

    /**
     * Parses the list of topics from the application topology.
     *
     * @return The list of topics.
     */
    public List<String> parseTopologyNodeTopics() {
        List<String> nodeTopics = new ArrayList<>();

        // Get the jsonNode that contains the topics JSON array
        JsonNode topics = applicationTopology.get("topics");

        // Loop through it adding it to the D.S
        if (topics != null && topics.isArray()) {
            for (JsonNode topicNode : topics) {
                nodeTopics.add(topicNode.asText());
            }
        }

        return nodeTopics;
    }

    /**
     * Parses the topology node trees from the application topology.
     *
     * @return The list of topology node trees.
     */
    public List<TopologyNodeTree> parseTopologyNodeTrees() {
        List<TopologyNodeTree> nodeTrees = new ArrayList<>();

        // Get the jsonNode that contains the sub-flow objects list
        JsonNode subFlows = applicationTopology.get("subFlows");

        // Make sure the sub-flow JSON object is valid
        if (subFlows != null && subFlows.isObject()) {
            // Iterate through
            Iterator<Map.Entry<String, JsonNode>> fields = subFlows.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode subFlow = field.getValue();

                TopologyNode rootNode = parseTopologyNode(subFlow.get("rootNode"));
                String subFlowId = rootNode.id();
                List<List<TopologyNode>> branches = new ArrayList<>();

                JsonNode branchesNode = subFlow.get("branches");
                if (branchesNode != null && branchesNode.isObject()) {

                    Iterator<Map.Entry<String, JsonNode>> branchFields = branchesNode.fields();
                    while (branchFields.hasNext()) {
                        Map.Entry<String, JsonNode> branchField = branchFields.next();
                        JsonNode branchArrayNode = branchField.getValue();

                        List<TopologyNode> branch = new ArrayList<>();
                        if (branchArrayNode.isArray()) {
                            for (JsonNode nodeNode : branchArrayNode) {
                                TopologyNode node = parseTopologyNode(nodeNode);
                                TopologyNode nodeWithRootSet = new TopologyNode(node.id(), node.name(), node.topic(), node.type(), node.thing(), node.uniqueAttribute(), subFlowId);
                                branch.add(nodeWithRootSet);
                            }
                        }
                        branches.add(branch);
                    }
                }

                nodeTrees.add(new TopologyNodeTree(rootNode, branches));
            }
        }

        return nodeTrees;
    }

    /**
     * Parses a topology node from a JsonNode.
     *
     * @param node The JsonNode representing the topology node.
     * @return The parsed TopologyNode.
     */
    public TopologyNode parseTopologyNode(JsonNode node) {
        String id = node.path("id").asText(null);
        String name = node.path("name").asText(null);
        String topic = node.path("topic").asText(null);
        String type = node.path("type").asText(null);
        String thing = node.path("thing").asText(null);
        String uniqueAttribute = null;

        // Extract the correct unique attribute based on type
        switch (type) {
            case NodeRedJSONParser.TYPE_INVOKE_ACTION -> uniqueAttribute = node.path("action").asText(null);
            case NodeRedJSONParser.TYPE_SUBSCRIBE_EVENT -> uniqueAttribute = node.path("event").asText(null);
            case NodeRedJSONParser.TYPE_READ_PROPERTY, NodeRedJSONParser.TYPE_WRITE_PROPERTY -> uniqueAttribute = node.path("property").asText(null);
        }

        // Create TopologyNode instance
        return new TopologyNode(id, name, topic, type, thing, uniqueAttribute, null);
    }

    /**
     * Parses a list of topology nodes of the specified type from the application topology.
     *
     * @param nodeType The type of nodes to parse.
     * @return The list of parsed topology nodes.
     */
    public List<TopologyNode> parseTopologyNodes(String nodeType) {
        List<TopologyNode> nodes = new ArrayList<>();

        JsonNode listOfNodes = applicationTopology.path(nodeType);

        if(listOfNodes.isArray()) {
            for(JsonNode node : listOfNodes) {
                String id = node.path("id").asText(null);
                String name = node.path("name").asText(null);
                String topic = node.path("topic").asText(null);
                String type = node.path("type").asText(null);
                String thing = node.path("thing").asText(null);
                String uniqueAttribute = null;

                // Only process node types that are relevant
                if(!topologyNodeTypesToInclude.contains(type)) continue;

                // Extract the correct unique attribute based on type
                switch (type) {
                    case NodeRedJSONParser.TYPE_INVOKE_ACTION -> uniqueAttribute = node.path("action").asText(null);
                    case NodeRedJSONParser.TYPE_SUBSCRIBE_EVENT -> uniqueAttribute = node.path("event").asText(null);
                    case NodeRedJSONParser.TYPE_READ_PROPERTY, NodeRedJSONParser.TYPE_WRITE_PROPERTY -> uniqueAttribute = node.path("property").asText(null);
                }

                // Create TopologyNode instance
                TopologyNode topologyNode = new TopologyNode(id, name, topic, type, thing, uniqueAttribute, null);
                nodes.add(topologyNode);
            }
        }

        return nodes;
    }
}
