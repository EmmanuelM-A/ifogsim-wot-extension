package com.extensions.sysconstructor.nodered;

import java.util.List;

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
        outputJson.set("nodes", generateNodes(mapper));
        outputJson.set("connections", generateConnections(mapper));
        outputJson.set("dataFlows", generateDataFlows(mapper));
        outputJson.set("events", generateEvents(mapper));

        return outputJson;
    }

    private ObjectNode generateApplicationName(ObjectMapper mapper) {
        ObjectNode title = mapper.createObjectNode();
        for (NodeRedNode node : nodes) {
            if (node.getType().equals("tab")) {
                title.put("title", node.getName());
                return title;
            }
        }
        return null; // Or return an empty ObjectNode if null is not preferred.
    }

    private ArrayNode generateThings(ObjectMapper mapper) {
        ArrayNode thingsArray = mapper.createArrayNode();
        for (NodeRedNode node : nodes) {
            if(node.getType().equals("consumed-thing")) {
                ObjectNode thing = mapper.createObjectNode();
                thing.put("id", node.getId());
                thing.put("name", node.getName());
                thing.put("type", node.getType());
                thingsArray.add(thing);
                return thingsArray;
            }
        }
        return null;
    }

    private ArrayNode generateNodes(ObjectMapper mapper) {
        ArrayNode nodesArray = mapper.createArrayNode();
        for (NodeRedNode node : nodes) {
            if(node.getType().equals("consumed-thing")) continue;
            if(node.getType().equals("tab")) continue;
            ObjectNode jsonNode = mapper.createObjectNode();
            jsonNode.put("id", node.getId());
            jsonNode.put("name", node.getName());
            jsonNode.put("type", node.getType());
            jsonNode.put("thing", node.getThingID());
            switch (node.getType()) {
                case "invoke-action" -> jsonNode.put("action", node.getUniqueAttribute());
                case "subscribe-event" -> jsonNode.put("event", node.getUniqueAttribute());
                case "read-property", "write-property" -> jsonNode.put("property", node.getUniqueAttribute());
            }
            nodesArray.add(jsonNode);
        }
        return nodesArray;
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
            if (node.getType().equals("inject")) {
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

