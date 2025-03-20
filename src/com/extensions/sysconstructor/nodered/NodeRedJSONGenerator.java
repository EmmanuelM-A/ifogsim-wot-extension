package com.extensions.sysconstructor.nodered;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This class is responsible for generating the application topology file and its content into a JSON file.
 */
public class NodeRedJSONGenerator {
    /**
     * The list of all node red nodes extracted from the node red application.
     */
    private final List<NodeRedNode> nodes;

    /**
     * Initializes the list of node red nodes.
     * @param nodes The list of node red nodes.
     */
    public NodeRedJSONGenerator(List<NodeRedNode> nodes) {
        this.nodes = nodes;
    }

    /**
     * Generates the application topology file.
     * @return The JSON object node containing the contents of the application topology file.
     */
    public ObjectNode generate() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode outputJson = mapper.createObjectNode();

        outputJson.set("applicationDetails", generateApplicationName(mapper));
        outputJson.set("things", generateThings(mapper));
        outputJson.set("topics", generateNodeTopics(mapper));
        outputJson.set("subFlows", generateSubFlowTrees(mapper));

        return outputJson;
    }

    /**
     * Generates a JSON object containing the application name.
     * Extracts the name from the first node of type {@code TYPE_TAB}.
     *
     * @param mapper The {@link ObjectMapper} used to create the JSON object
     * @return An {@link ObjectNode} containing the application name, or {@code null} if no such node is found
     */
    private ObjectNode generateApplicationName(ObjectMapper mapper) {
        ObjectNode title = mapper.createObjectNode();
        for (NodeRedNode node : nodes) {
            if (node.getType().equals(NodeRedJSONParser.TYPE_TAB)) {
                title.put("title", node.getName());
                return title;
            }
        }
        return null; // Or return an empty ObjectNode if null is not preferred.
    }

    /**
     * Generates a JSON array containing information about consumed things.
     * Each object in the array represents a thing with its ID, name, and type.
     *
     * @param mapper The {@link ObjectMapper} used to create the JSON array
     * @return An {@link ArrayNode} containing JSON representations of consumed things
     */
    private ArrayNode generateThings(ObjectMapper mapper) {
        ArrayNode thingsArray = mapper.createArrayNode();
        for (NodeRedNode node : nodes) {
            if(node.getType().equals(NodeRedJSONParser.TYPE_CONSUMED_THING)) {
                ObjectNode thing = mapper.createObjectNode();
                thing.put("id", node.getId());
                thing.put("name", node.getName());
                thing.put("type", node.getType());
                thingsArray.add(thing);
            }
        }
        return thingsArray;
    }

    /**
     * Generates a JSON array of unique node topics.
     * Duplicates are filtered out using a {@link HashSet}.
     *
     * @param mapper The {@link ObjectMapper} used to create the JSON array
     * @return An {@link ArrayNode} containing unique node topics
     */
    private ArrayNode generateNodeTopics(ObjectMapper mapper) {
        ArrayNode nodesArray = mapper.createArrayNode();
        Set<String> uniqueTopics = new HashSet<>(); // Use a Set to track unique topics

        for (NodeRedNode node : nodes) {
            if (node.getTopic() != null && !node.getTopic().isEmpty() && uniqueTopics.add(node.getTopic())) {
                // Add the topic to the ArrayNode only if it's not already in the Set
                nodesArray.add(node.getTopic());
            }
        }

        return nodesArray;
    }

    /**
     * Generates a JSON representation of sub-flow trees.
     * Groups nodes into sub-flows and represents them as a hierarchical structure.
     *
     * @param mapper The {@link ObjectMapper} used to create the JSON structure
     * @return An {@link ObjectNode} containing sub-flow trees
     */
    private ObjectNode generateSubFlowTrees(ObjectMapper mapper) {
        ObjectNode subFlowTrees = mapper.createObjectNode();

        // Group the nodes into tree like sub-flows
        List<SubFlowTree> trees = SubFlowTreeGrouper.groupNodesIntoSubFlowTree(nodes);

        for (int index = 0; index < trees.size(); index++) {
            ObjectNode subFlowTree = mapper.createObjectNode(); // JSON object for each sub flow

            // Set root node
            JsonNode rootNode = createTopologyNode(mapper, trees.get(index).rootNode());
            subFlowTree.set("rootNode", rootNode);

            // Set the branches
            ObjectNode branchesJson = mapper.createObjectNode();
            int branchIndex = 0;
            for (TreeBranch branch : trees.get(index).branches()) {
                ArrayNode branchArray = mapper.createArrayNode();
                for (NodeRedNode node : branch.nodes()) {
                    branchArray.add(createTopologyNode(mapper, node));
                }

                branchesJson.set("branch-" + branchIndex, branchArray); // Add the branch array to the branch
                branchIndex++;
            }

            subFlowTree.set("branches", branchesJson); // Set the branches array
            subFlowTrees.set("subFlow-" + index, subFlowTree); // Add the sub flow tree to the subFlowTrees ArrayNode
        }

        return subFlowTrees;
    }

    /**
     * Creates a JSON representation of a topology node.
     * Includes details such as ID, name, type, topic, thing, and specific attributes based on the node type.
     *
     * @param mapper The {@link ObjectMapper} used to create the JSON object
     * @param node   The {@link NodeRedNode} to convert into a JSON representation
     * @return An {@link ObjectNode} representing the topology node
     */
    private ObjectNode createTopologyNode(ObjectMapper mapper, NodeRedNode node) {
        ObjectNode topologyNode = mapper.createObjectNode();
        topologyNode.put("id", node.getId());
        topologyNode.put("name", node.getName());
        topologyNode.put("type", node.getType());
        topologyNode.put("topic", node.getTopic());
        topologyNode.put("thing", node.getThingID());
        switch (node.getType()) {
            case NodeRedJSONParser.TYPE_INVOKE_ACTION -> topologyNode.put("action", node.getUniqueAttribute());
            case NodeRedJSONParser.TYPE_SUBSCRIBE_EVENT -> topologyNode.put("event", node.getUniqueAttribute());
            case NodeRedJSONParser.TYPE_READ_PROPERTY, NodeRedJSONParser.TYPE_WRITE_PROPERTY -> topologyNode.put("property", node.getUniqueAttribute());
        }
        return topologyNode;
    }
}

