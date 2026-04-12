package com.demo.icds.domain;

public record DiagnosticIssue(String type, Severity severity) {
    public enum Severity {
        LOW,
        MEDIUM,
        HIGH
    }
}
