package com.demo.icds.domain;

import java.util.List;

public record RuleDefinition(String ruleId, List<RuleCondition> conditions, RuleOutput output) {
}
