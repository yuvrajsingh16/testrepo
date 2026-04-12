package com.demo.icds.engine;

import com.demo.icds.domain.DiagnosticIssue;
import com.demo.icds.domain.IncidentAnalyzeRequest;
import com.demo.icds.domain.IncidentContext;
import com.demo.icds.domain.TimelineEvent;

import java.util.List;

public interface CorrelationEngine {

    List<TimelineEvent> correlate(IncidentAnalyzeRequest request, IncidentContext context, List<DiagnosticIssue> issues);
}
