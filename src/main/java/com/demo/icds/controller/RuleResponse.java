package com.demo.icds.controller;

import com.demo.icds.domain.RuleCondition;
import com.demo.icds.domain.RuleDefinition;

import java.util.List;

public record RuleResponse(
        String ruleId,
        List<ConditionResponse> conditions,
        OutputResponse output
) {

    public static RuleResponse fromDomain(RuleDefinition ruleDefinition) {
        return new RuleResponse(
                ruleDefinition.ruleId(),
                ruleDefinition.conditions().stream().map(ConditionResponse::fromDomain).toList(),
                OutputResponse.fromDomain(ruleDefinition)
        );
    }

    public record ConditionResponse(String field, String operator, Object value) {
        public static ConditionResponse fromDomain(RuleCondition condition) {
            return new ConditionResponse(condition.field(), condition.operator().toWireOperator(), condition.value());
        }
    }

    public record OutputResponse(String type, String severity) {
        public static OutputResponse fromDomain(RuleDefinition ruleDefinition) {
            return new OutputResponse(ruleDefinition.output().type(), ruleDefinition.output().severity().name());
        }
    }
}
