package com.demo.icds.engine;

import com.demo.icds.domain.DiagnosticIssue;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DefaultDiagnosticsGenerator implements DiagnosticsGenerator {

    @Override
    public List<DiagnosticIssue> normalize(List<DiagnosticIssue> issues) {
        Map<String, DiagnosticIssue> deduped = new LinkedHashMap<>();
        for (DiagnosticIssue issue : issues) {
            deduped.put(issue.type(), issue);
        }
        return List.copyOf(deduped.values());
    }
}
