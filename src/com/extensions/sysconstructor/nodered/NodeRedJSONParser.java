package com.extensions.sysconstructor.nodered;

import com.extensions.utils.processors.FileProcessor;
import com.extensions.vdcreation.models.ThingDescription;
import com.extensions.vdcreation.parsers.ThingDescriptionParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodeRedJSONParser implements FileProcessor<List<NodeRedNode>> {
    private final ObjectMapper objectMapper;

    public NodeRedJSONParser() {
        this.objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        //objectMapper.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public List<NodeRedNode> process(File file) throws IOException {
        JsonNode rootNode = objectMapper.readTree(file);

        List<NodeRedNode> nodes = new ArrayList<>();

        if(rootNode.isArray()) {
            for(JsonNode node : rootNode) {
                String id = cleanString(node.has("id") ? String.valueOf(node.get("id")) : null);
                String name = cleanString(node.has("name") ? String.valueOf(node.get("name")) : null);
                String type = cleanString(node.has("type") ? String.valueOf(node.get("type")) : null);
                String thingId = cleanString(node.has("thing") ? String.valueOf(node.get("thing")) : null);

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
                    case "tab" -> {
                        String title = cleanString(node.has("label") ? String.valueOf(node.get("label")) : "Unknown Application");
                        yield new NodeRedNode(null, type, title, null, null, null);
                    }
                    case "consumed-thing" -> {
                        String td = node.get("td").asText();
                        JsonNode tdNode = objectMapper.readTree(td);
                        String title = tdNode.get("title").asText();
                        yield new NodeRedNode(id, type, title, null, null, null);
                    }
                    case "invoke-action" -> {
                        String action = cleanString(node.has("action") ? String.valueOf(node.get("action")) : null);
                        yield new NodeRedNode(id, type, name, thingId, action, wires);
                    }
                    case "subscribe-event" -> {
                        String event = cleanString(node.has("event") ? String.valueOf(node.get("event")) : null);
                        yield new NodeRedNode(id, type, name, thingId, event, wires);
                    }
                    case "read-property", "write-property" -> {
                        String property = cleanString(node.has("property") ? String.valueOf(node.get("property")) : null);
                        yield new NodeRedNode(id, type, name, thingId, property, wires);
                    }
                    default -> new NodeRedNode(id, type, name, thingId, null, wires);
                };

                nodes.add(nodeRedNode);
            }
        }

        return nodes;
    }

    private String cleanString(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\"", "").trim(); // Remove all double quotes and trim whitespace
    }
}
