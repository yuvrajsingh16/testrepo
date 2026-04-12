package com.demo.icds.service;

import com.demo.icds.domain.IncidentContext;

import java.util.Optional;

public interface ContextService {

    Optional<IncidentContext> getContextForIncident(String incidentId);
}
