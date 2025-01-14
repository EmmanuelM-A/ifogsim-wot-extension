package com.extensions.sysconstructor.nodered;

import com.extensions.utils.processors.FileProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class NodeRedJSONParser implements FileProcessor<List<NodeRedNode>> {

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
                String thingId = node.has("thingId") ? String.valueOf(node.get("thingId")) : null;

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

                NodeRedNode nodeRedNode = switch (type) {
                    case "invoke-action" -> {
                        String action = node.has("action") ? String.valueOf(node.get("action")) : null;
                        yield new ActionNode(id, type, name, thingId, wires, action);
                    }
                    case "subscribe-event" -> {
                        String event = node.has("event") ? String.valueOf(node.get("event")) : null;
                        yield new EventNode(id, type, name, thingId, wires, event);
                    }
                    case "read-property", "write-property" -> {
                        String property = node.has("property") ? String.valueOf(node.get("property")) : null;
                        yield new ActionNode(id, type, name, thingId, wires, property);
                    }
                    default -> new NodeRedNode(id, type, name, thingId, wires);
                };

                nodes.add(nodeRedNode);
            }
        }

        return nodes;
    }
}
