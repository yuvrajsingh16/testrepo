package com.demo.icds.domain;

public record RuleCondition(String field, ConditionOperator operator, Object value) {
}
