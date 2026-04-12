package com.demo.icds.engine;

import com.demo.icds.domain.ConditionOperator;
import com.demo.icds.domain.DiagnosticIssue;
import com.demo.icds.domain.IncidentContext;
import com.demo.icds.domain.RuleCondition;
import com.demo.icds.domain.RuleDefinition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
public class DefaultRuleEngine implements RuleEngine {

    @Override
    public List<DiagnosticIssue> evaluate(List<RuleDefinition> rules, IncidentContext context) {
        List<DiagnosticIssue> issues = new ArrayList<>();
        for (RuleDefinition rule : rules) {
            if (matchesAllConditions(rule.conditions(), context)) {
                issues.add(new DiagnosticIssue(rule.output().type(), rule.output().severity()));
            }
        }
        return issues;
    }

    @Override
    public boolean matchesAllConditions(List<RuleCondition> conditions, IncidentContext context) {
        for (RuleCondition condition : conditions) {
            if (!matchesCondition(condition, context)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesCondition(RuleCondition condition, IncidentContext context) {
        Objects.requireNonNull(condition.field(), "field");
        Objects.requireNonNull(condition.operator(), "operator");

        return switch (condition.field()) {
            case "logs" -> matchesLogs(condition, context.logs());
            default -> matchesMetric(condition, context.metrics());
        };
    }

    private static boolean matchesLogs(RuleCondition condition, List<String> logs) {
        if (condition.operator() != ConditionOperator.CONTAINS) {
            throw new IllegalArgumentException("Unsupported operator for logs: " + condition.operator());
        }

        String needle = Objects.toString(condition.value(), "").toLowerCase(Locale.ROOT);
        return logs.stream().anyMatch(log -> log != null && log.toLowerCase(Locale.ROOT).contains(needle));
    }

    private static boolean matchesMetric(RuleCondition condition, Map<String, Object> metrics) {
        Object metricValue = metrics.get(condition.field());
        if (metricValue == null) {
            return false;
        }

        return switch (condition.operator()) {
            case GREATER_THAN -> toDouble(metricValue) > toDouble(condition.value());
            case LESS_THAN -> toDouble(metricValue) < toDouble(condition.value());
            case EQUALS -> Objects.equals(normalize(metricValue), normalize(condition.value()));
            case CONTAINS -> throw new IllegalArgumentException("Unsupported operator for metric field: " + condition.field());
        };
    }

    private static double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(Objects.toString(value));
    }

    private static Object normalize(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return value;
    }
}
