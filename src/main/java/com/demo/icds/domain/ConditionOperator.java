package com.demo.icds.domain;

public enum ConditionOperator {
    GREATER_THAN,
    LESS_THAN,
    EQUALS,
    CONTAINS;

    public static ConditionOperator fromWireOperator(String operator) {
        return switch (operator) {
            case ">" -> GREATER_THAN;
            case "<" -> LESS_THAN;
            case "=" -> EQUALS;
            case "contains" -> CONTAINS;
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };
    }

    public String toWireOperator() {
        return switch (this) {
            case GREATER_THAN -> ">";
            case LESS_THAN -> "<";
            case EQUALS -> "=";
            case CONTAINS -> "contains";
        };
    }
}
