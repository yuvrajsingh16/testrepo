package com.demo.icds.store;

import com.demo.icds.domain.IncidentContext;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class IncidentContextStore {

    private final ConcurrentMap<String, IncidentContext> contextByIncidentId = new ConcurrentHashMap<>();

    public void upsert(IncidentContext context) {
        contextByIncidentId.put(context.incidentId(), context);
    }

    public Optional<IncidentContext> findByIncidentId(String incidentId) {
        return Optional.ofNullable(contextByIncidentId.get(incidentId));
    }

    public void clear() {
        contextByIncidentId.clear();
    }
}
