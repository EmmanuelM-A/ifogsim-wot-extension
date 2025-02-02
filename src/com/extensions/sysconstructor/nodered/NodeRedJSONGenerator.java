package com.extensions.sysconstructor.nodered;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class NodeRedJSONGenerator {
    private final List<NodeRedNode> nodes;

    public NodeRedJSONGenerator(List<NodeRedNode> nodes) {
        this.nodes = nodes;
    }

    public ObjectNode generate() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode outputJson = mapper.createObjectNode();

        outputJson.set("applicationDetails", generateApplicationName(mapper));
        outputJson.set("things", generateThings(mapper));
        outputJson.set("topics", generateNodeTopics(mapper));
        outputJson.set("nodes", generateNodes(mapper));
        outputJson.set("subFlows", generateSubFlowTrees(mapper));
        outputJson.set("connections", generateConnections(mapper));
        outputJson.set("dataFlows", generateDataFlows(mapper));
        outputJson.set("events", generateEvents(mapper));

        return outputJson;
    }

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

    private ArrayNode generateNodes(ObjectMapper mapper) {
        ArrayNode nodesArray = mapper.createArrayNode();
        for (NodeRedNode node : nodes) {
            if(node.getType().equals(NodeRedJSONParser.TYPE_CONSUMED_THING)) continue;
            if(node.getType().equals(NodeRedJSONParser.TYPE_TAB)) continue;
            ObjectNode jsonNode = createTopologyNode(mapper, node);
            nodesArray.add(jsonNode);
        }
        return nodesArray;
    }

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

    private ObjectNode generateSubFlowTrees(ObjectMapper mapper) {
        ObjectNode subFlowTrees = mapper.createObjectNode();

        // Group the nodes into tree like sub-flows
        List<SubFlowTree> trees = SubFlowTreeGrouper.groupNodesIntoSubFlowTree(nodes);

        for (int index = 0; index < trees.size(); index++) {
            ObjectNode subFlowTree = mapper.createObjectNode(); // JSON object for each sub flow

            // Set root node
            JsonNode rootNode = createTopologyNode(mapper, trees.get(index).getRootNode());
            subFlowTree.set("rootNode", rootNode);

            // Set the branches
            ObjectNode branchesJson = mapper.createObjectNode();
            int branchIndex = 0;
            for (TreeBranch branch : trees.get(index).getBranches()) {
                ArrayNode branchArray = mapper.createArrayNode();
                for (NodeRedNode node : branch.nodes()) {
                    branchArray.add(createTopologyNode(mapper, node));
                }
                branchIndex++;
                branchesJson.set("branch-" + branchIndex, branchArray); // Add the branch array to the branch
            }

            subFlowTree.set("branches", branchesJson); // Set the branches array
            subFlowTrees.set("subFlow-" + index, subFlowTree); // Add the sub flow tree to the subFlowTrees ArrayNode
        }

        return subFlowTrees;
    }

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

    private ArrayNode generateConnections(ObjectMapper mapper) {
        ArrayNode wiresArray = mapper.createArrayNode();
        for (NodeRedNode node : nodes) {
            if(node.getConnections() == null) continue;
            for (String wire : node.getConnections()) {
                ObjectNode wireJson = mapper.createObjectNode();
                wireJson.put("source", node.getId());
                wireJson.put("destination", wire);
                wiresArray.add(wireJson);
            }
        }
        return wiresArray;
    }

    private ArrayNode generateDataFlows(ObjectMapper mapper) {
        ArrayNode dataFlowsArray = mapper.createArrayNode();
        for (NodeRedNode node : nodes) {
            if (node.getType().equals(NodeRedJSONParser.TYPE_INJECT)) {
                ObjectNode dataFlow = mapper.createObjectNode();
                dataFlow.put("source", node.getId());
                ArrayNode targets = mapper.createArrayNode();
                for (String connection : node.getConnections()) {
                    targets.add(connection);
                }
                dataFlow.set("targets", targets);
                dataFlowsArray.add(dataFlow);
            }
        }
        return dataFlowsArray;
    }

    private ArrayNode generateEvents(ObjectMapper mapper) {
        ArrayNode eventsArray = mapper.createArrayNode();
        for (NodeRedNode node : nodes) {
            if (node.getType().toLowerCase().contains("event")) {
                ObjectNode event = mapper.createObjectNode();
                event.put("id", node.getId());
                event.put("name", node.getUniqueAttribute());
                eventsArray.add(event);
            }
        }
        return eventsArray;
    }
}

