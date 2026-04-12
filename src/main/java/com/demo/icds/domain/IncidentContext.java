package com.demo.icds.domain;

import java.util.List;
import java.util.Map;

public record IncidentContext(
        String incidentId,
        List<String> logs,
        Map<String, Object> metrics,
        List<DeploymentEvent> deployments) {
}
