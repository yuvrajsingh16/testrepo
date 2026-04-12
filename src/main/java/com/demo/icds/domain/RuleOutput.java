package com.demo.icds.domain;

public record RuleOutput(String type, DiagnosticIssue.Severity severity) {
}
