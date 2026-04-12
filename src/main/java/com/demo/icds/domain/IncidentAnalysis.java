package com.demo.icds.domain;

import java.time.Instant;
import java.util.List;

public record IncidentAnalysis(
        String analysisId,
        String incidentId,
        AnalysisStatus status,
        List<DiagnosticIssue> detectedIssues,
        List<TimelineEvent> timeline,
        Instant createdAt) {
}
