package com.demo.icds.service;

import com.demo.icds.domain.DeploymentEvent;
import com.demo.icds.domain.IncidentContext;
import com.demo.icds.store.IncidentContextStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StoreBackedContextService implements ContextService {

    private final IncidentContextStore incidentContextStore;

    public StoreBackedContextService(IncidentContextStore incidentContextStore) {
        this.incidentContextStore = incidentContextStore;
    }

    @Override
    public Optional<IncidentContext> getContextForIncident(String incidentId) {
        return incidentContextStore.findByIncidentId(incidentId);
    }
}
