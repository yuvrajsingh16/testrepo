package com.demo.icds.controller;

import com.demo.icds.domain.IncidentAnalyzeRequest;
import com.demo.icds.domain.IncidentAnalysis;
import com.demo.icds.orchestrator.IncidentOrchestrator;
import com.demo.icds.store.IncidentAnalysisStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/v1/incidents")
public class IncidentController {

    private final IncidentOrchestrator incidentOrchestrator;
    private final IncidentAnalysisStore incidentAnalysisStore;

    public IncidentController(IncidentOrchestrator incidentOrchestrator, IncidentAnalysisStore incidentAnalysisStore) {
        this.incidentOrchestrator = incidentOrchestrator;
        this.incidentAnalysisStore = incidentAnalysisStore;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyze(@RequestBody IncidentAnalyzeRequest request) {
        IncidentAnalysis analysis = incidentOrchestrator.analyze(request);
        return ResponseEntity.ok(new IncidentAnalysisResponse(
                analysis.analysisId(),
                analysis.status(),
                analysis.detectedIssues(),
                analysis.timeline()));
    }

    @GetMapping("/{analysisId}")
    public ResponseEntity<?> get(@PathVariable String analysisId) {
        Optional<IncidentAnalysis> analysis = incidentAnalysisStore.findByAnalysisId(analysisId);
        if (analysis.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("Analysis not found"));
        }

        IncidentAnalysis value = analysis.get();
        return ResponseEntity.ok(new IncidentAnalysisResponse(
                value.analysisId(),
                value.status(),
                value.detectedIssues(),
                value.timeline()));
    }
}
