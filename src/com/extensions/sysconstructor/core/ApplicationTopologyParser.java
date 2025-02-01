package com.extensions.sysconstructor.core;

import com.extensions.sysconstructor.nodered.NodeRedJSONParser;
import com.extensions.sysconstructor.topology.TopologyDataFlow;
import com.extensions.sysconstructor.topology.TopologyNode;
import com.extensions.sysconstructor.topology.TopologyNodeConnection;
import com.extensions.utils.processors.FileProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApplicationTopologyParser implements FileProcessor<JsonNode> {
    private final ObjectMapper objectMapper;
    private final JsonNode applicationTopology;
    private final List<String> topologyNodeTypesToInclude;

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

        this.applicationTopology = process(applicationTopologyFile);
    }

    @Override
    public JsonNode process(File file) throws IOException {
        return objectMapper.readTree(file);
    }

    public String parseApplicationTitle() throws JsonProcessingException {
        JsonNode applicationDetails = applicationTopology.get("applicationDetails");

        if(applicationDetails == null || !applicationDetails.has("title")) return null;

        return applicationDetails.get("title").asText(null);
    }

    public List<String> parseTopologyNodeTopics() {
        List<String> nodeTopics = new ArrayList<>();

        JsonNode topics = applicationTopology.get("topics");

        if (topics != null && topics.isArray()) {
            for (JsonNode topicNode : topics) {
                nodeTopics.add(topicNode.asText());
            }
        }

        return nodeTopics;
    }

    public List<TopologyNodeConnection> parseTopologyConnections() throws JsonProcessingException {
        List<TopologyNodeConnection> nodeConnections = new ArrayList<>();

        JsonNode connections = applicationTopology.get("connections");

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
                String id = node.path("id").asText(null);
                String name = node.path("name").asText(null);
                String topic = node.path("topic").asText(null);
                String type = node.path("type").asText(null);
                String thing = node.path("thing").asText(null);
                String uniqueAttribute = null;

                if(!topologyNodeTypesToInclude.contains(type)) continue;

                if(thing != null) uniqueAttribute = node.path(type).asText(null);

                TopologyNode topologyNode = new TopologyNode(id, name, topic, type, thing, uniqueAttribute);
                nodes.add(topologyNode);
            }
        }

        return nodes;
    }

    public List<TopologyDataFlow> parseTopologyDataFlows() {
        List<TopologyDataFlow> dataFlows = new ArrayList<>();

        JsonNode dataFlowsNode = applicationTopology.get("dataFlows");
        if (dataFlowsNode != null && dataFlowsNode.isArray()) {
            for (JsonNode dataFlowNode : dataFlowsNode) {
                String source = dataFlowNode.get("source").asText();
                List<String> targets = new ArrayList<>();
                JsonNode targetsNode = dataFlowNode.get("targets");
                if (targetsNode != null && targetsNode.isArray()) {
                    for (JsonNode targetNode : targetsNode) {
                        targets.add(targetNode.asText());
                    }
                }
                dataFlows.add(new TopologyDataFlow(source, targets));
            }
        }

        return dataFlows;
    }


}
