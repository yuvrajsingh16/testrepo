package com.demo.icds.engine;

import com.demo.icds.domain.DeploymentEvent;
import com.demo.icds.domain.DiagnosticIssue;
import com.demo.icds.domain.IncidentAnalyzeRequest;
import com.demo.icds.domain.IncidentContext;
import com.demo.icds.domain.TimelineEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class DefaultCorrelationEngine implements CorrelationEngine {

    @Override
    public List<TimelineEvent> correlate(IncidentAnalyzeRequest request, IncidentContext context, List<DiagnosticIssue> issues) {
        List<TimelineEvent> timeline = new ArrayList<>();

        timeline.add(new TimelineEvent(Instant.now(), "INCIDENT", "Analyze incidentId=" + request.incidentId()));
        for (DeploymentEvent deployment : context.deployments()) {
            timeline.add(new TimelineEvent(deployment.time(), "DEPLOYMENT", "Deployed version=" + deployment.version()));
        }

        for (DiagnosticIssue issue : issues) {
            timeline.add(new TimelineEvent(Instant.now(), "ISSUE", issue.type() + " severity=" + issue.severity()));
        }

        timeline.sort(Comparator.comparing(TimelineEvent::time));
        return timeline;
    }
}
