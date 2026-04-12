package com.demo.icds.engine;

import com.demo.icds.domain.DiagnosticIssue;

import java.util.List;

public interface DiagnosticsGenerator {

    List<DiagnosticIssue> normalize(List<DiagnosticIssue> issues);
}
