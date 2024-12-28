package com.extensions.vdcreation.parsers;

import com.extensions.utils.processors.FileProcessor;
import com.extensions.vdcreation.core.VirtualDeviceConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VirtualDeviceConfigParser implements FileProcessor<List<VirtualDeviceConfig>> {
    /**
     * Parses a JSON file containing VirtualDevice configurations and returns a list of VirtualDeviceConfig objects.
     *
     * @param file The path to the JSON file.
     * @return A list of VirtualDeviceConfig objects parsed from the JSON file.
     * @throws IOException If there is an error reading the file.
     */
    @Override
    public List<VirtualDeviceConfig> process(File file) throws IOException {
        // Create an empty list of config objects
        List<VirtualDeviceConfig> configs = new ArrayList<>();

        // Create an ObjectMapper instance for parsing JSON
        ObjectMapper objectMapper = new ObjectMapper();

        // Parse the JSON file into a JsonNode
        JsonNode rootNode = objectMapper.readTree(file);

        // Ensure the "configs" key exists
        JsonNode configsNode = rootNode.get("configs");
        if (configsNode == null || !configsNode.isArray()) {
            throw new IllegalArgumentException("JSON file must contain a 'configs' key with an array of configurations.");
        }

        // Iterate over the JSON array of configurations
        for (JsonNode node : configsNode) {
            // Extract tagNames as a list of strings
            List<String> tags = new ArrayList<>();
            for (JsonNode tagNode : node.get("tagNames")) {
                tags.add(tagNode.asText());
            }

            // Extract other attributes
            long mips = node.get("mips").asLong();
            int ram = node.get("ram").asInt();
            long upBw = node.get("upBw").asLong();
            long downBw = node.get("downBw").asLong();
            int level = node.get("level").asInt();
            double ratePerMips = node.get("ratePerMips").asDouble();
            double busyPower = node.get("busyPower").asDouble();
            double idlePower = node.get("idlePower").asDouble();

            // Create a VirtualDeviceConfig object and add it to the list
            VirtualDeviceConfig config = new VirtualDeviceConfig(tags, mips, ram, upBw, downBw, level, ratePerMips, busyPower, idlePower);
            configs.add(config);
        }

        return configs;
    }
}
