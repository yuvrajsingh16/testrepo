package com.demo.icds.controller;

import java.util.List;

public record CreateRuleRequest(
        String ruleId,
        List<ConditionDto> conditions,
        OutputDto output) {

    public record ConditionDto(String field, String operator, Object value) {
    }

    public record OutputDto(String type, String severity) {
    }
}
