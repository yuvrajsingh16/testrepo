package com.demo.icds.controller;

import com.demo.icds.domain.AnalysisStatus;
import com.demo.icds.domain.DiagnosticIssue;
import com.demo.icds.domain.TimelineEvent;

import java.util.List;

public record IncidentAnalysisResponse(
        String analysisId,
        AnalysisStatus status,
        List<DiagnosticIssue> detectedIssues,
        List<TimelineEvent> timeline) {
}
