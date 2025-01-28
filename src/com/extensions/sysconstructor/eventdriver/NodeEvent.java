package com.extensions.sysconstructor.eventdriver;

import java.util.List;

public record NodeEvent(String eventType, String source, List<String> destination, double timestamp) {}
