package com.demo.icds.orchestrator;

import com.demo.icds.domain.AnalysisStatus;
import com.demo.icds.domain.DiagnosticIssue;
import com.demo.icds.domain.IncidentAnalyzeRequest;
import com.demo.icds.domain.IncidentAnalysis;
import com.demo.icds.domain.IncidentContext;
import com.demo.icds.domain.TimelineEvent;
import com.demo.icds.engine.CorrelationEngine;
import com.demo.icds.engine.DiagnosticsGenerator;
import com.demo.icds.engine.RuleEngine;
import com.demo.icds.service.ContextService;
import com.demo.icds.store.IncidentAnalysisStore;
import com.demo.icds.store.RuleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DefaultIncidentOrchestrator implements IncidentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(DefaultIncidentOrchestrator.class);

    private final ContextService contextService;
    private final RuleStore ruleStore;
    private final RuleEngine ruleEngine;
    private final CorrelationEngine correlationEngine;
    private final DiagnosticsGenerator diagnosticsGenerator;
    private final IncidentAnalysisStore incidentAnalysisStore;

    public DefaultIncidentOrchestrator(
            ContextService contextService,
            RuleStore ruleStore,
            RuleEngine ruleEngine,
            CorrelationEngine correlationEngine,
            DiagnosticsGenerator diagnosticsGenerator,
            IncidentAnalysisStore incidentAnalysisStore) {
        this.contextService = contextService;
        this.ruleStore = ruleStore;
        this.ruleEngine = ruleEngine;
        this.correlationEngine = correlationEngine;
        this.diagnosticsGenerator = diagnosticsGenerator;
        this.incidentAnalysisStore = incidentAnalysisStore;
    }

    @Override
    public IncidentAnalysis analyze(IncidentAnalyzeRequest request) {
        validateRequest(request);

        String analysisId = "ANL-" + UUID.randomUUID();
        Instant now = Instant.now();

        IncidentContext context = contextService.getContextForIncident(request.incidentId())
                .orElseThrow(() -> new IllegalArgumentException("Context not found for incidentId=" + request.incidentId()));

        try {
            List<DiagnosticIssue> rawIssues = ruleEngine.evaluate(List.copyOf(ruleStore.list()), context);
            List<DiagnosticIssue> issues = diagnosticsGenerator.normalize(rawIssues);
            List<TimelineEvent> timeline = correlationEngine.correlate(request, context, issues);

            IncidentAnalysis analysis = new IncidentAnalysis(
                    analysisId,
                    request.incidentId(),
                    AnalysisStatus.COMPLETED,
                    issues,
                    timeline,
                    now);

            incidentAnalysisStore.put(analysis);
            return analysis;
        } catch (RuntimeException e) {
            log.error("Analysis failed for incidentId={}", request.incidentId(), e);
            throw e;
        }
    }

    private static void validateRequest(IncidentAnalyzeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }
        if (isBlank(request.incidentId())) {
            throw new IllegalArgumentException("incidentId is required");
        }
        if (isBlank(request.service())) {
            throw new IllegalArgumentException("service is required");
        }
        if (request.startTime() == null || request.endTime() == null) {
            throw new IllegalArgumentException("startTime and endTime are required");
        }
        if (request.endTime().isBefore(request.startTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
