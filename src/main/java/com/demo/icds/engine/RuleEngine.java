package com.demo.icds.engine;

import com.demo.icds.domain.DiagnosticIssue;
import com.demo.icds.domain.IncidentContext;
import com.demo.icds.domain.RuleCondition;
import com.demo.icds.domain.RuleDefinition;

import java.util.List;

public interface RuleEngine {

    List<DiagnosticIssue> evaluate(List<RuleDefinition> rules, IncidentContext context);

    boolean matchesAllConditions(List<RuleCondition> conditions, IncidentContext context);
}
