package com.demo.icds.controller;

import com.demo.icds.domain.DeploymentEvent;
import com.demo.icds.domain.IncidentContext;
import com.demo.icds.store.IncidentContextStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/v1/context")
public class ContextController {

    private final IncidentContextStore incidentContextStore;

    public ContextController(IncidentContextStore incidentContextStore) {
        this.incidentContextStore = incidentContextStore;
    }

    @PostMapping
    public ResponseEntity<?> upsert(@RequestBody IncidentContextRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }
        if (request.incidentId() == null || request.incidentId().isBlank()) {
            throw new IllegalArgumentException("incidentId is required");
        }

        List<String> logs = request.logs() == null ? List.of() : request.logs();
        Map<String, Object> metrics = request.metrics() == null ? Map.of() : request.metrics();
        List<DeploymentEvent> deployments = parseDeployments(request);

        incidentContextStore.upsert(new IncidentContext(request.incidentId(), logs, metrics, deployments));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    private static List<DeploymentEvent> parseDeployments(IncidentContextRequest request) {
        if (request.deployments() == null) {
            return List.of();
        }

        List<DeploymentEvent> deployments = new ArrayList<>(request.deployments().size());
        for (int index = 0; index < request.deployments().size(); index++) {
            IncidentContextRequest.DeploymentDto deployment = request.deployments().get(index);
            if (deployment == null) {
                throw new IllegalArgumentException("deployments[" + index + "] is required");
            }

            String version = Objects.toString(deployment.version(), "").trim();
            if (version.isBlank()) {
                throw new IllegalArgumentException("deployments[" + index + "].version is required");
            }

            String time = Objects.toString(deployment.time(), "").trim();
            if (time.isBlank()) {
                throw new IllegalArgumentException("deployments[" + index + "].time is required");
            }

            Instant parsedTime;
            try {
                parsedTime = Instant.parse(time);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("deployments[" + index + "].time must be ISO-8601", e);
            }

            deployments.add(new DeploymentEvent(version, parsedTime));
        }

        return deployments;
    }
}
