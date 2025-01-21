package com.extensions.sysconstructor.architecture;

import java.util.List;

public record NodeEvent(String eventType, String source, List<String> destination, double timestamp) {}
