package com.extensions.sysconstructor.topology;

import java.util.List;

public record TopologyDataFlow(String source, List<String> targets) {
}
