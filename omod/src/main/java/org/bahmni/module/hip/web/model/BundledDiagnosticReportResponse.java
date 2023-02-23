package org.bahmni.module.hip.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class BundledDiagnosticReportResponse {
    private List<DiagnosticReportBundle> diagnosticReports;
}

