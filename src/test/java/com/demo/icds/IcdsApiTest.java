package com.demo.icds;

import com.demo.icds.store.IncidentAnalysisStore;
import com.demo.icds.store.IncidentContextStore;
import com.demo.icds.store.RuleStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IcdsApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IncidentContextStore incidentContextStore;

    @Autowired
    private RuleStore ruleStore;

    @Autowired
    private IncidentAnalysisStore incidentAnalysisStore;

    @BeforeEach
    void resetStores() {
        incidentContextStore.clear();
        ruleStore.clear();
        incidentAnalysisStore.clear();
    }

    @Test
    void context_then_rule_then_analyze_then_fetch_result() throws Exception {
        mockMvc.perform(post("/v1/context")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "incidentId", "INC123",
                        "logs", List.of("error: database timeout"),
                        "metrics", Map.of("cpu", 90, "latency", 1200),
                        "deployments", List.of(Map.of(
                                "version", "v1.2",
                                "time", "2026-04-10T10:05:00Z"))
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"));

        mockMvc.perform(post("/v1/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "ruleId", "RULE-1",
                        "conditions", List.of(Map.of(
                                "field", "cpu",
                                "operator", ">",
                                "value", 85
                        )),
                        "output", Map.of(
                                "type", "RESOURCE_SATURATION",
                                "severity", "MEDIUM"
                        )
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ruleId").value("RULE-1"));

        String analysisResponse = mockMvc.perform(post("/v1/incidents/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "incidentId", "INC123",
                        "service", "payment-service",
                        "startTime", "2026-04-10T10:00:00Z",
                        "endTime", "2026-04-10T10:30:00Z",
                        "symptoms", List.of("latency", "errors")
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.analysisId").isNotEmpty())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.detectedIssues").isArray())
            .andExpect(jsonPath("$.timeline").isArray())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String analysisId = objectMapper.readTree(analysisResponse).get("analysisId").asText();

        mockMvc.perform(get("/v1/incidents/{analysisId}", analysisId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.analysisId").value(analysisId))
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void rules_list_and_delete() throws Exception {
        mockMvc.perform(post("/v1/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "ruleId", "RULE-DELETE",
                        "conditions", List.of(Map.of(
                                "field", "latency",
                                "operator", ">",
                                "value", 1000
                        )),
                        "output", Map.of(
                                "type", "LATENCY_SPIKE",
                                "severity", "HIGH"
                        )
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ruleId").value("RULE-DELETE"));

        mockMvc.perform(get("/v1/rules"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.ruleId=='RULE-DELETE')]").exists());

        mockMvc.perform(delete("/v1/rules/{ruleId}", "RULE-DELETE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("deleted"));

        mockMvc.perform(delete("/v1/rules/{ruleId}", "RULE-DELETE"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Rule not found"));
    }

    @Test
    void analyze_rejects_invalid_request() throws Exception {
        mockMvc.perform(post("/v1/incidents/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "incidentId", "",
                        "service", "payment-service",
                        "startTime", "2026-04-10T10:00:00Z",
                        "endTime", "2026-04-10T10:30:00Z"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("incidentId is required"));
    }

    @Test
    void context_rejects_missing_incident_id() throws Exception {
        mockMvc.perform(post("/v1/context")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "logs", List.of("error")))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("incidentId is required"));
    }

    @Test
    void rule_rejects_unsupported_operator() throws Exception {
        mockMvc.perform(post("/v1/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "ruleId", "RULE-BAD-OP",
                        "conditions", List.of(Map.of(
                                "field", "cpu",
                                "operator", ">=",
                                "value", 85
                        )),
                        "output", Map.of(
                                "type", "RESOURCE_SATURATION",
                                "severity", "MEDIUM"
                        )
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Unsupported operator: >="));
    }

    @Test
    void rule_rejects_contains_for_metric_fields() throws Exception {
        mockMvc.perform(post("/v1/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "ruleId", "RULE-CONTAINS-METRIC",
                        "conditions", List.of(Map.of(
                                "field", "cpu",
                                "operator", "contains",
                                "value", "10"
                        )),
                        "output", Map.of(
                                "type", "BAD_RULE",
                                "severity", "LOW"
                        )
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Unsupported operator for metric field: cpu"));
    }

    @Test
    void rule_rejects_greater_than_for_log_fields() throws Exception {
        mockMvc.perform(post("/v1/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "ruleId", "RULE-GT-LOG",
                        "conditions", List.of(Map.of(
                                "field", "log.message",
                                "operator", ">",
                                "value", 1
                        )),
                        "output", Map.of(
                                "type", "BAD_RULE",
                                "severity", "LOW"
                        )
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Unsupported operator for log field: log.message"));
    }

    @Test
    void create_rule_returns_dto_shape_with_wire_operator() throws Exception {
        mockMvc.perform(post("/v1/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "ruleId", "RULE-DTO",
                        "conditions", List.of(Map.of(
                                "field", "latency",
                                "operator", ">",
                                "value", 1000
                        )),
                        "output", Map.of(
                                "type", "LATENCY_SPIKE",
                                "severity", "HIGH"
                        )
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ruleId").value("RULE-DTO"))
            .andExpect(jsonPath("$.conditions[0].field").value("latency"))
            .andExpect(jsonPath("$.conditions[0].operator").value(">"))
            .andExpect(jsonPath("$.output.type").value("LATENCY_SPIKE"))
            .andExpect(jsonPath("$.output.severity").value("HIGH"));
    }

    @Test
    void analyze_supports_less_than_equals_and_contains_for_logs() throws Exception {
        mockMvc.perform(post("/v1/context")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "incidentId", "INC-OPS",
                        "logs", List.of("WARN: upstream timeout"),
                        "metrics", Map.of("cpu", 10, "latency", 1200),
                        "deployments", List.of()
                ))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/v1/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "ruleId", "RULE-LOW-CPU",
                        "conditions", List.of(Map.of(
                                "field", "cpu",
                                "operator", "<",
                                "value", 20
                        )),
                        "output", Map.of(
                                "type", "CPU_LOW",
                                "severity", "LOW"
                        )
                ))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/v1/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "ruleId", "RULE-LATENCY-EQ",
                        "conditions", List.of(Map.of(
                                "field", "latency",
                                "operator", "=",
                                "value", 1200
                        )),
                        "output", Map.of(
                                "type", "LATENCY_EXACT",
                                "severity", "MEDIUM"
                        )
                ))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/v1/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "ruleId", "RULE-LOG-CONTAINS",
                        "conditions", List.of(Map.of(
                                "field", "logs",
                                "operator", "contains",
                                "value", "timeout"
                        )),
                        "output", Map.of(
                                "type", "UPSTREAM_TIMEOUT",
                                "severity", "HIGH"
                        )
                ))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/v1/incidents/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "incidentId", "INC-OPS",
                        "service", "payment-service",
                        "startTime", "2026-04-10T10:00:00Z",
                        "endTime", "2026-04-10T10:30:00Z",
                        "symptoms", List.of("latency")
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.detectedIssues[?(@.type=='CPU_LOW')]").exists())
            .andExpect(jsonPath("$.detectedIssues[?(@.type=='LATENCY_EXACT')]").exists())
            .andExpect(jsonPath("$.detectedIssues[?(@.type=='UPSTREAM_TIMEOUT')]").exists());
    }

    @Test
    void get_analysis_not_found() throws Exception {
        mockMvc.perform(get("/v1/incidents/{analysisId}", "ANL-NOT-THERE"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Analysis not found"));
    }
}

