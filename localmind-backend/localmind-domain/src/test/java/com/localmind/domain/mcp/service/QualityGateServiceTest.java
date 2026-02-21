package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.GateEvaluation;
import com.localmind.domain.mcp.model.QualityGate;
import com.localmind.domain.mcp.port.out.QualityGateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QualityGateServiceTest {

    @Mock
    private QualityGateRepository repository;

    private QualityGateService service;

    @BeforeEach
    void setUp() {
        service = new QualityGateService(repository);
        when(repository.saveGate(any(QualityGate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.saveEvaluation(any(GateEvaluation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ========== defineGate Tests ==========

    @Test
    void defineGate_withValidData_returnsGateDetails() {
        String checksJson = "[{\"metric\":\"coverage\",\"operator\":\">=\",\"threshold\":80}]";

        Map<String, Object> result = service.defineGate("Production Gate", "my-project", checksJson);

        assertThat(result.get("name")).isEqualTo("Production Gate");
        assertThat(result.get("projectName")).isEqualTo("my-project");
        assertThat(result.get("id")).isNotNull();
        assertThat(result.get("createdAt")).isNotNull();
        assertThat(result.get("checks")).isNotNull();
    }

    @Test
    void defineGate_savesToRepository() {
        String checksJson = "[{\"metric\":\"coverage\",\"operator\":\">=\",\"threshold\":80}]";

        service.defineGate("Test Gate", null, checksJson);

        ArgumentCaptor<QualityGate> captor = ArgumentCaptor.forClass(QualityGate.class);
        verify(repository).saveGate(captor.capture());
        QualityGate saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Test Gate");
        assertThat(saved.getChecksJson()).isEqualTo(checksJson);
    }

    @Test
    void defineGate_withBlankName_returnsError() {
        Map<String, Object> result = service.defineGate("", null, "[{}]");

        assertThat(result.get("error")).isEqualTo("Gate name is required");
        verify(repository, never()).saveGate(any());
    }

    @Test
    void defineGate_withNullName_returnsError() {
        Map<String, Object> result = service.defineGate(null, null, "[{}]");

        assertThat(result.get("error")).isEqualTo("Gate name is required");
    }

    @Test
    void defineGate_withBlankChecks_returnsError() {
        Map<String, Object> result = service.defineGate("Gate", null, "");

        assertThat(result.get("error")).isEqualTo("Checks JSON is required");
    }

    @Test
    void defineGate_withNullChecks_returnsError() {
        Map<String, Object> result = service.defineGate("Gate", null, null);

        assertThat(result.get("error")).isEqualTo("Checks JSON is required");
    }

    // ========== evaluateGate Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void evaluateGate_allChecksPassing_returnsPassed() {
        QualityGate gate = QualityGate.builder()
                .id("gate-1")
                .name("Prod Gate")
                .checksJson("[{\"metric\":\"coverage\",\"operator\":\">=\",\"threshold\":80}]")
                .createdAt(Instant.now())
                .build();
        when(repository.findGateById("gate-1")).thenReturn(Optional.of(gate));

        Map<String, Object> result = service.evaluateGate("gate-1", "{\"coverage\":90}");

        assertThat(result.get("passed")).isEqualTo(true);
        assertThat(result.get("gateName")).isEqualTo("Prod Gate");
        List<Map<String, Object>> results = (List<Map<String, Object>>) result.get("results");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).get("passed")).isEqualTo(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    void evaluateGate_checkFailing_returnsFailed() {
        QualityGate gate = QualityGate.builder()
                .id("gate-2")
                .name("Quality Gate")
                .checksJson("[{\"metric\":\"coverage\",\"operator\":\">=\",\"threshold\":80}]")
                .createdAt(Instant.now())
                .build();
        when(repository.findGateById("gate-2")).thenReturn(Optional.of(gate));

        Map<String, Object> result = service.evaluateGate("gate-2", "{\"coverage\":50}");

        assertThat(result.get("passed")).isEqualTo(false);
        List<Map<String, Object>> results = (List<Map<String, Object>>) result.get("results");
        assertThat(results.get(0).get("passed")).isEqualTo(false);
        assertThat(results.get(0).get("actual")).isEqualTo(50.0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void evaluateGate_multipleChecks_allMustPass() {
        QualityGate gate = QualityGate.builder()
                .id("gate-3")
                .name("Multi Gate")
                .checksJson("[{\"metric\":\"coverage\",\"operator\":\">=\",\"threshold\":80},{\"metric\":\"bugs\",\"operator\":\"<=\",\"threshold\":5}]")
                .createdAt(Instant.now())
                .build();
        when(repository.findGateById("gate-3")).thenReturn(Optional.of(gate));

        // coverage passes (90>=80) but bugs fails (10 > 5)
        Map<String, Object> result = service.evaluateGate("gate-3", "{\"coverage\":90,\"bugs\":10}");

        assertThat(result.get("passed")).isEqualTo(false);
        List<Map<String, Object>> results = (List<Map<String, Object>>) result.get("results");
        assertThat(results).hasSize(2);
        assertThat(results.get(0).get("passed")).isEqualTo(true);
        assertThat(results.get(1).get("passed")).isEqualTo(false);
    }

    @Test
    @SuppressWarnings("unchecked")
    void evaluateGate_multipleChecks_allPassing() {
        QualityGate gate = QualityGate.builder()
                .id("gate-4")
                .name("All Pass Gate")
                .checksJson("[{\"metric\":\"coverage\",\"operator\":\">=\",\"threshold\":80},{\"metric\":\"bugs\",\"operator\":\"<=\",\"threshold\":5}]")
                .createdAt(Instant.now())
                .build();
        when(repository.findGateById("gate-4")).thenReturn(Optional.of(gate));

        Map<String, Object> result = service.evaluateGate("gate-4", "{\"coverage\":90,\"bugs\":2}");

        assertThat(result.get("passed")).isEqualTo(true);
    }

    @Test
    void evaluateGate_gateNotFound_returnsError() {
        when(repository.findGateById("missing")).thenReturn(Optional.empty());

        Map<String, Object> result = service.evaluateGate("missing", "{\"coverage\":90}");

        assertThat(result.get("error")).isEqualTo("Gate not found: missing");
    }

    @Test
    void evaluateGate_blankGateId_returnsError() {
        Map<String, Object> result = service.evaluateGate("", "{\"coverage\":90}");

        assertThat(result.get("error")).isEqualTo("Gate ID is required");
    }

    @Test
    void evaluateGate_blankMetrics_returnsError() {
        Map<String, Object> result = service.evaluateGate("gate-1", "");

        assertThat(result.get("error")).isEqualTo("Metrics JSON is required");
    }

    @Test
    void evaluateGate_savesEvaluation() {
        QualityGate gate = QualityGate.builder()
                .id("gate-5")
                .name("Save Gate")
                .checksJson("[{\"metric\":\"coverage\",\"operator\":\">=\",\"threshold\":80}]")
                .createdAt(Instant.now())
                .build();
        when(repository.findGateById("gate-5")).thenReturn(Optional.of(gate));

        service.evaluateGate("gate-5", "{\"coverage\":85}");

        ArgumentCaptor<GateEvaluation> captor = ArgumentCaptor.forClass(GateEvaluation.class);
        verify(repository).saveEvaluation(captor.capture());
        GateEvaluation saved = captor.getValue();
        assertThat(saved.getGateId()).isEqualTo("gate-5");
        assertThat(saved.isPassed()).isTrue();
    }

    // ========== Operator Tests ==========

    @Test
    void evaluateCheck_greaterThanOrEqual() {
        assertThat(service.evaluateCheck(80, ">=", 80)).isTrue();
        assertThat(service.evaluateCheck(90, ">=", 80)).isTrue();
        assertThat(service.evaluateCheck(70, ">=", 80)).isFalse();
    }

    @Test
    void evaluateCheck_lessThanOrEqual() {
        assertThat(service.evaluateCheck(5, "<=", 5)).isTrue();
        assertThat(service.evaluateCheck(3, "<=", 5)).isTrue();
        assertThat(service.evaluateCheck(10, "<=", 5)).isFalse();
    }

    @Test
    void evaluateCheck_greaterThan() {
        assertThat(service.evaluateCheck(81, ">", 80)).isTrue();
        assertThat(service.evaluateCheck(80, ">", 80)).isFalse();
    }

    @Test
    void evaluateCheck_lessThan() {
        assertThat(service.evaluateCheck(4, "<", 5)).isTrue();
        assertThat(service.evaluateCheck(5, "<", 5)).isFalse();
    }

    @Test
    void evaluateCheck_equalTo() {
        assertThat(service.evaluateCheck(80, "==", 80)).isTrue();
        assertThat(service.evaluateCheck(81, "==", 80)).isFalse();
    }

    @Test
    void evaluateCheck_notEqualTo() {
        assertThat(service.evaluateCheck(81, "!=", 80)).isTrue();
        assertThat(service.evaluateCheck(80, "!=", 80)).isFalse();
    }

    @Test
    void evaluateCheck_unknownOperator_returnsFalse() {
        assertThat(service.evaluateCheck(80, "~", 80)).isFalse();
    }

    // ========== listGates Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void listGates_noFilter_returnsAll() {
        List<QualityGate> gates = List.of(
                QualityGate.builder().id("g1").name("Gate 1").projectName("proj-a")
                        .checksJson("[]").createdAt(Instant.now()).build(),
                QualityGate.builder().id("g2").name("Gate 2").projectName("proj-b")
                        .checksJson("[]").createdAt(Instant.now()).build()
        );
        when(repository.findAllGates()).thenReturn(gates);

        Map<String, Object> result = service.listGates(null);

        assertThat(result.get("count")).isEqualTo(2);
        List<Map<String, Object>> gateList = (List<Map<String, Object>>) result.get("gates");
        assertThat(gateList).hasSize(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void listGates_withFilter_filtersCorrectly() {
        List<QualityGate> gates = List.of(
                QualityGate.builder().id("g1").name("Gate 1").projectName("proj-a")
                        .checksJson("[]").createdAt(Instant.now()).build(),
                QualityGate.builder().id("g2").name("Gate 2").projectName("proj-b")
                        .checksJson("[]").createdAt(Instant.now()).build()
        );
        when(repository.findAllGates()).thenReturn(gates);

        Map<String, Object> result = service.listGates("proj-a");

        assertThat(result.get("count")).isEqualTo(1);
        List<Map<String, Object>> gateList = (List<Map<String, Object>>) result.get("gates");
        assertThat(gateList.get(0).get("name")).isEqualTo("Gate 1");
    }

    // ========== getGateHistory Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void getGateHistory_returnsOrderedEvaluations() {
        Instant older = Instant.parse("2026-01-01T10:00:00Z");
        Instant newer = Instant.parse("2026-02-01T10:00:00Z");

        List<GateEvaluation> evals = new ArrayList<>();
        evals.add(GateEvaluation.builder().id("e1").gateId("g1").passed(true)
                .metricsJson("{}").resultsJson("[]").createdAt(older).build());
        evals.add(GateEvaluation.builder().id("e2").gateId("g1").passed(false)
                .metricsJson("{}").resultsJson("[]").createdAt(newer).build());
        when(repository.findEvaluationsByGateId("g1")).thenReturn(evals);

        Map<String, Object> result = service.getGateHistory("g1", 10);

        assertThat(result.get("count")).isEqualTo(2);
        List<Map<String, Object>> evalList = (List<Map<String, Object>>) result.get("evaluations");
        // Newer should come first
        assertThat(evalList.get(0).get("id")).isEqualTo("e2");
        assertThat(evalList.get(1).get("id")).isEqualTo("e1");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getGateHistory_respectsLimit() {
        List<GateEvaluation> evals = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            evals.add(GateEvaluation.builder().id("e" + i).gateId("g1").passed(true)
                    .metricsJson("{}").resultsJson("[]")
                    .createdAt(Instant.now().minusSeconds(i * 1000)).build());
        }
        when(repository.findEvaluationsByGateId("g1")).thenReturn(evals);

        Map<String, Object> result = service.getGateHistory("g1", 2);

        assertThat(result.get("count")).isEqualTo(2);
    }

    @Test
    void getGateHistory_blankGateId_returnsError() {
        Map<String, Object> result = service.getGateHistory("", 10);

        assertThat(result.get("error")).isEqualTo("Gate ID is required");
    }

    // ========== parseChecksArray Tests ==========

    @Test
    void parseChecksArray_validJson_parseCorrectly() {
        String json = "[{\"metric\":\"coverage\",\"operator\":\">=\",\"threshold\":80}]";

        List<Map<String, Object>> checks = service.parseChecksArray(json);

        assertThat(checks).hasSize(1);
        assertThat(checks.get(0).get("metric")).isEqualTo("coverage");
        assertThat(checks.get(0).get("operator")).isEqualTo(">=");
    }

    @Test
    void parseChecksArray_emptyArray_returnsEmptyList() {
        List<Map<String, Object>> checks = service.parseChecksArray("[]");
        assertThat(checks).isEmpty();
    }

    @Test
    void parseChecksArray_nullInput_returnsEmptyList() {
        List<Map<String, Object>> checks = service.parseChecksArray(null);
        assertThat(checks).isEmpty();
    }

    // ========== parseMetrics Tests ==========

    @Test
    void parseMetrics_validJson_parsesCorrectly() {
        Map<String, Double> metrics = service.parseMetrics("{\"coverage\":85,\"bugs\":3}");

        assertThat(metrics).hasSize(2);
        assertThat(metrics.get("coverage")).isEqualTo(85.0);
        assertThat(metrics.get("bugs")).isEqualTo(3.0);
    }

    @Test
    void parseMetrics_emptyJson_returnsEmptyMap() {
        Map<String, Double> metrics = service.parseMetrics("{}");
        assertThat(metrics).isEmpty();
    }
}
