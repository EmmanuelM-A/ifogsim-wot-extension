package com.extensions.sysconstructor.topology;

import com.extensions.utils.processors.FileProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.json.simple.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApplicationTopologyParser implements FileProcessor<JsonNode> {
    private final ObjectMapper objectMapper;
    private final JsonNode applicationTopology;
    private final List<String> topologyNodeTypesToInclude;

    public ApplicationTopologyParser(File applicationTopologyFile) throws IOException {
        // Create and configure object instance
        this.objectMapper = new ObjectMapper();
        this.topologyNodeTypesToInclude = new ArrayList<>();
        topologyNodeTypesToInclude.add("invoke-action");


        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.applicationTopology = process(applicationTopologyFile);
    }

    @Override
    public JsonNode process(File file) throws IOException {
        return objectMapper.readTree(file);
    }

    public List<TopologyNodeConnection> parseTopologyConnections() throws JsonProcessingException {
        List<TopologyNodeConnection> nodeConnections = new ArrayList<>();

        String connectionsNode = applicationTopology.get("connections").asText();

        JsonNode connections = objectMapper.readTree(connectionsNode);

        for(JsonNode connection : connections) {
            String src = connection.get("source").asText();
            String dst = connection.get("destination").asText();

            TopologyNodeConnection nodeConnection = new TopologyNodeConnection(src, dst);

            nodeConnections.add(nodeConnection);
        }

        return nodeConnections;
    }

    public List<TopologyNode> parseTopologyNodes(String nodeType) {
        List<TopologyNode> nodes = new ArrayList<>();

        JsonNode listOfNodes = applicationTopology.path(nodeType);

        if(listOfNodes.isArray()) {
            for(JsonNode node : listOfNodes) {
                String id = node.path("id").asText();
                String name = node.path("name").asText();
                String type = node.path("type").asText();
                String thing = node.path("thing").asText();
                String uniqueAttribute = null;

                if(thing != null) uniqueAttribute = node.path(type).asText();

                TopologyNode topologyNode = new TopologyNode(id, name, type, thing, uniqueAttribute);
                nodes.add(topologyNode);
            }
        }


    }
}
