package com.demo.icds.domain;

import java.time.Instant;
import java.util.List;

public record IncidentAnalyzeRequest(
        String incidentId,
        String service,
        Instant startTime,
        Instant endTime,
        List<String> symptoms) {
}
