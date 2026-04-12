package com.demo.icds.controller;

import java.util.List;
import java.util.Map;

public record IncidentContextRequest(
        String incidentId,
        List<String> logs,
        Map<String, Object> metrics,
        List<DeploymentDto> deployments) {

    public record DeploymentDto(String version, String time) {
    }
}
