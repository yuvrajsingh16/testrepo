package com.demo.icds.store;

import com.demo.icds.domain.IncidentAnalysis;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class IncidentAnalysisStore {

    private final ConcurrentMap<String, IncidentAnalysis> analysisById = new ConcurrentHashMap<>();

    public void put(IncidentAnalysis analysis) {
        analysisById.put(analysis.analysisId(), analysis);
    }

    public Optional<IncidentAnalysis> findByAnalysisId(String analysisId) {
        return Optional.ofNullable(analysisById.get(analysisId));
    }

    public void clear() {
        analysisById.clear();
    }
}
