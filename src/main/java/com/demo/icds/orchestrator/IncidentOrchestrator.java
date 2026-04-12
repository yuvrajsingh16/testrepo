package com.demo.icds.orchestrator;

import com.demo.icds.domain.IncidentAnalyzeRequest;
import com.demo.icds.domain.IncidentAnalysis;

public interface IncidentOrchestrator {

    IncidentAnalysis analyze(IncidentAnalyzeRequest request);
}
