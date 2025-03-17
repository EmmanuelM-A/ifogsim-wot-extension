package com.extensions.sysconstructor.nodered;

import com.extensions.utils.processors.FileProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a JSON file containing Node-RED nodes and converts it into a list of {@link NodeRedNode} objects.
 */
public class NodeRedJSONParser implements FileProcessor<List<NodeRedNode>> {
    /**
     * The object mapper instance used to parse JSON files.
     */
    private final ObjectMapper objectMapper;

    /**
     * Node type representing an action invocation.
     */
    public final static String TYPE_INVOKE_ACTION = "invoke-action";

    /**
     * Node type representing an event subscription.
     */
    public final static String TYPE_SUBSCRIBE_EVENT = "subscribe-event";

    /**
     * Node type representing a read property operation.
     */
    public final static String TYPE_READ_PROPERTY = "read-property";

    /**
     * Node type representing a write property operation.
     */
    public final static String TYPE_WRITE_PROPERTY = "write-property";

    /**
     * Node type representing a consumed Thing.
     */
    public final static String TYPE_CONSUMED_THING = "consumed-thing";

    /**
     * Node type representing a flow tab.
     */
    public final static String TYPE_TAB = "tab";

    /**
     * Node type representing an inject node.
     */
    public final static String TYPE_INJECT = "inject";

    /**
     * Constructs a new {@code NodeRedJSONParser} with a configured {@code ObjectMapper}.
     */
    public NodeRedJSONParser() {
        this.objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Processes a JSON file and converts it into a list of {@link NodeRedNode} objects.
     *
     * @param file The JSON file to parse.
     * @return A list of parsed {@link NodeRedNode} objects.
     * @throws IOException If an error occurs while reading or parsing the file.
     */
    @Override
    public List<NodeRedNode> process(File file) throws IOException {
        JsonNode rootNode = objectMapper.readTree(file);

        List<NodeRedNode> nodes = new ArrayList<>();

        if(rootNode.isArray()) {
            for(JsonNode node : rootNode) {
                String id = cleanString(node.has("id") ? String.valueOf(node.get("id")) : null);
                String name = cleanString(node.has("name") ? String.valueOf(node.get("name")) : null);
                String topic = cleanString(node.has("topic") ? String.valueOf(node.get("topic")) : null);
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
                    case TYPE_TAB -> {
                        String title = cleanString(node.has("label") ? String.valueOf(node.get("label")) : "Unknown Application");
                        yield new NodeRedNode(null, type, title, topic, null, null, null);
                    }
                    case TYPE_CONSUMED_THING -> {
                        String td = node.get("td").asText();
                        JsonNode tdNode = objectMapper.readTree(td);
                        String title = tdNode.get("title").asText();
                        yield new NodeRedNode(id, type, title, topic, null, null, null);
                    }
                    case TYPE_INVOKE_ACTION -> {
                        String action = cleanString(node.has("action") ? String.valueOf(node.get("action")) : null);
                        yield new NodeRedNode(id, type, name, topic, thingId, action, wires);
                    }
                    case TYPE_SUBSCRIBE_EVENT -> {
                        String event = cleanString(node.has("event") ? String.valueOf(node.get("event")) : null);
                        yield new NodeRedNode(id, type, name, topic, thingId, event, wires);
                    }
                    case TYPE_READ_PROPERTY, TYPE_WRITE_PROPERTY -> {
                        String property = cleanString(node.has("property") ? String.valueOf(node.get("property")) : null);
                        yield new NodeRedNode(id, type, name, topic, thingId, property, wires);
                    }
                    default -> new NodeRedNode(id, type, name, topic, thingId, null, wires);
                };

                nodes.add(nodeRedNode);
            }
        }

        return nodes;
    }

    /**
     * Cleans a string by removing double quotes and trimming whitespace.
     *
     * @param value The string to clean.
     * @return The cleaned string, or {@code null} if the input was {@code null}.
     */
    private String cleanString(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\"", "").trim(); // Remove all double quotes and trim whitespace
    }
}