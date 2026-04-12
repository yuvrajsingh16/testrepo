package com.demo.icds.controller;

import com.demo.icds.domain.ConditionOperator;
import com.demo.icds.domain.DiagnosticIssue;
import com.demo.icds.domain.RuleCondition;
import com.demo.icds.domain.RuleDefinition;
import com.demo.icds.domain.RuleOutput;
import com.demo.icds.store.RuleStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/v1/rules")
public class RuleController {

    private final RuleStore ruleStore;

    public RuleController(RuleStore ruleStore) {
        this.ruleStore = ruleStore;
    }

    @PostMapping
    public RuleResponse create(@RequestBody CreateRuleRequest request) {
        RuleDefinition ruleDefinition = toDomain(request);
        return RuleResponse.fromDomain(ruleStore.upsert(ruleDefinition));
    }

    @GetMapping
    public List<RuleResponse> list() {
        return ruleStore.list().stream().map(RuleResponse::fromDomain).toList();
    }

    @DeleteMapping("/{ruleId}")
    public ResponseEntity<?> delete(@PathVariable String ruleId) {
        boolean deleted = ruleStore.delete(ruleId);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("Rule not found"));
        }
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    private static RuleDefinition toDomain(CreateRuleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }
        if (request.ruleId() == null || request.ruleId().isBlank()) {
            throw new IllegalArgumentException("ruleId is required");
        }
        if (request.conditions() == null || request.conditions().isEmpty()) {
            throw new IllegalArgumentException("conditions are required");
        }
        if (request.output() == null) {
            throw new IllegalArgumentException("output is required");
        }

        List<RuleCondition> conditions = request.conditions().stream()
                .map(conditionDto -> {
                    String field = required(conditionDto.field(), "field");
                    ConditionOperator operator = ConditionOperator.fromWireOperator(required(conditionDto.operator(), "operator"));
                    validateOperatorAllowed(field, operator);
                    return new RuleCondition(field, operator, conditionDto.value());
                })
                .toList();

        RuleOutput output = new RuleOutput(
                required(request.output().type(), "type"),
                parseSeverity(request.output().severity()));

        return new RuleDefinition(request.ruleId(), conditions, output);
    }

    private static DiagnosticIssue.Severity parseSeverity(String severity) {
        if (severity == null) {
            throw new IllegalArgumentException("severity is required");
        }
        return DiagnosticIssue.Severity.valueOf(severity.toUpperCase(Locale.ROOT));
    }

    private static void validateOperatorAllowed(String field, ConditionOperator operator) {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(operator, "operator");

        String normalizedField = field.toLowerCase(Locale.ROOT);
        boolean isLogField = normalizedField.startsWith("log.");

        boolean isAllowed = switch (operator) {
            case CONTAINS -> isLogField;
            case GREATER_THAN, LESS_THAN, EQUALS -> !isLogField;
        };

        if (!isAllowed) {
            String fieldType = isLogField ? "log" : "metric";
            throw new IllegalArgumentException("Unsupported operator for " + fieldType + " field: " + field);
        }
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }
}
