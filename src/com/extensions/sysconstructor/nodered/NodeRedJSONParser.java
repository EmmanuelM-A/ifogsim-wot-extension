package com.extensions.sysconstructor.nodered;

import com.extensions.utils.processors.FileProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class NodeRedJSONParser implements FileProcessor<List<NodeRedNode>> {
    private final List<String> nodeTypesToParseFor;

    public NodeRedJSONParser() {
        this.nodeTypesToParseFor = new ArrayList<>();

        nodeTypesToParseFor.add("invoke-action");
        nodeTypesToParseFor.add("subscribe-event");
        nodeTypesToParseFor.add("read-property");
        nodeTypesToParseFor.add("write-property");
        nodeTypesToParseFor.add("inject");
        nodeTypesToParseFor.add("consumed-thing");
    }

    @Override
    public List<NodeRedNode> process(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(file);

        List<NodeRedNode> nodes = new ArrayList<>();

        if(rootNode.isArray()) {
            for(JsonNode node : rootNode) {
                String id = node.has("id") ? String.valueOf(node.get("id")) : null;
                String name = node.has("name") ? String.valueOf(node.get("name")) : null;
                String type = node.has("type") ? String.valueOf(node.get("type")) : null;
                String thingId = node.has("thing") ? String.valueOf(node.get("thing")) : null;

                List<String> wires = new ArrayList<>();
                if (node.has("wires") && node.get("wires").isArray()) {
                    for (JsonNode wire : node.get("wires")) {
                        if (wire.isArray()) {
                            for (JsonNode wireTarget : wire) {
                                wires.add(wireTarget.asText());
                            }
                        }
                    }
                }

                if(type == null) continue;

                //if(!nodeTypesToParseFor.contains(type)) continue;

                switch (type) {
                    case "tab":
                        String title = node.has("label") ? String.valueOf(node.get("label")) : "Unknown Application";
                        NodeRedNode titleNode = new NodeRedNode(null, null, title, null, null);
                        nodes.add(titleNode);
                        break;

                    case "consumed-thing":
                        String td = node.has("consumed-thing") ? String.valueOf(node.get("consumed-thing")) : null;
                        NodeRedNode thingNode = new NodeRedNode(id, type, extractThingName(td), null, null);
                        nodes.add(thingNode);
                        break;

                    case "invoke-action":
                        String action = node.has("action") ? String.valueOf(node.get("action")) : null;
                        ActionNode actionNode = new ActionNode(id, type, name, thingId, wires, action);
                        nodes.add(actionNode);
                        break;

                    case "subscribe-event":
                        String event = node.has("event") ? String.valueOf(node.get("event")) : null;
                        EventNode eventNode = new EventNode(id, type, name, thingId, wires, event);
                        nodes.add(eventNode);
                        break;

                    case "read-property", "write-property":
                        String property = node.has("property") ? String.valueOf(node.get("property")) : null;
                        PropertyNode propertyNode = new PropertyNode(id, type, name, thingId, wires, property);
                        nodes.add(propertyNode);
                        break;

                    default:
                        NodeRedNode nodeRedNode = new NodeRedNode(id, type, name, thingId, wires);
                        nodes.add(nodeRedNode);
                        break;
                };
            }
        }

        return nodes;
    }

    private String extractThingName(String td) {
        if (td == null || td.isEmpty()) {
            return "Unknown Thing";
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode tdJson = mapper.readTree(td);
            return tdJson.has("title") ? tdJson.get("title").asText("Unknown Thing") : "Unknown Thing";
        } catch (IOException e) {
            // If the TD cannot be parsed, return a default value
            return "Unknown Thing";
        }
    }
}
