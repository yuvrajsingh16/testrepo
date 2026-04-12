package com.demo.icds.store;

import com.demo.icds.domain.RuleDefinition;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RuleStore {

    private final ConcurrentMap<String, RuleDefinition> rulesById = new ConcurrentHashMap<>();

    public RuleDefinition upsert(RuleDefinition ruleDefinition) {
        rulesById.put(ruleDefinition.ruleId(), ruleDefinition);
        return ruleDefinition;
    }

    public Collection<RuleDefinition> list() {
        return rulesById.values();
    }

    public Optional<RuleDefinition> findById(String ruleId) {
        return Optional.ofNullable(rulesById.get(ruleId));
    }

    public boolean delete(String ruleId) {
        return rulesById.remove(ruleId) != null;
    }

    public void clear() {
        rulesById.clear();
    }
}
