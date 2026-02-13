package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.InsightResult;
import com.localmind.domain.mcp.port.out.InsightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InsightEngineServiceTest {

    @Mock
    private InsightRepository repository;

    private InsightEngineService service;

    @BeforeEach
    void setUp() {
        service = new InsightEngineService(repository);
        when(repository.save(any(InsightResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ========== queryInsight Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void queryInsight_velocityQuestion_returnsVelocityInsight() {
        Map<String, Object> result = service.queryInsight("How is our team velocity?");

        assertThat(result.get("question")).isEqualTo("How is our team velocity?");
        assertThat((String) result.get("insight")).contains("velocity");
        assertThat((double) result.get("confidence")).isGreaterThan(0.5);
        assertThat((List<String>) result.get("suggestedActions")).isNotEmpty();
        assertThat(result.get("queriedAt")).isNotNull();
    }

    @Test
    void queryInsight_budgetQuestion_returnsBudgetInsight() {
        Map<String, Object> result = service.queryInsight("What is the budget status?");

        assertThat((String) result.get("insight")).containsIgnoringCase("budget");
        assertThat((double) result.get("confidence")).isGreaterThan(0.5);
    }

    @Test
    void queryInsight_sprintQuestion_returnsSprintInsight() {
        Map<String, Object> result = service.queryInsight("How is the sprint going?");

        assertThat((String) result.get("insight")).containsIgnoringCase("sprint");
    }

    @Test
    void queryInsight_incidentQuestion_returnsIncidentInsight() {
        Map<String, Object> result = service.queryInsight("Any incidents recently?");

        assertThat((String) result.get("insight")).containsIgnoringCase("incident");
    }

    @Test
    void queryInsight_qualityQuestion_returnsQualityInsight() {
        Map<String, Object> result = service.queryInsight("What is the code quality?");

        assertThat((String) result.get("insight")).containsIgnoringCase("quality");
    }

    @Test
    void queryInsight_genericQuestion_returnsGenericInsight() {
        Map<String, Object> result = service.queryInsight("Tell me something interesting");

        assertThat(result.get("insight")).isNotNull();
        assertThat((double) result.get("confidence")).isEqualTo(0.60);
    }

    @Test
    void queryInsight_savesToRepository() {
        service.queryInsight("Test question");

        ArgumentCaptor<InsightResult> captor = ArgumentCaptor.forClass(InsightResult.class);
        verify(repository).save(captor.capture());
        InsightResult saved = captor.getValue();
        assertThat(saved.getQuestion()).isEqualTo("Test question");
        assertThat(saved.getResultJson()).isNotNull();
    }

    @Test
    void queryInsight_emptyQuestion_returnsError() {
        Map<String, Object> result = service.queryInsight("");

        assertThat(result.get("error")).isEqualTo("Question is required");
        verify(repository, never()).save(any());
    }

    @Test
    void queryInsight_nullQuestion_returnsError() {
        Map<String, Object> result = service.queryInsight(null);

        assertThat(result.get("error")).isEqualTo("Question is required");
    }

    // ========== correlateMetrics Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void correlateMetrics_twoMetrics_returnsOneCorrelation() {
        Map<String, Object> result = service.correlateMetrics("[\"velocity\",\"coverage\"]", "last-sprint");

        List<Map<String, Object>> correlations = (List<Map<String, Object>>) result.get("correlations");
        assertThat(correlations).hasSize(1);
        assertThat(correlations.get(0).get("metricA")).isEqualTo("velocity");
        assertThat(correlations.get(0).get("metricB")).isEqualTo("coverage");
        assertThat(correlations.get(0)).containsKeys("strength", "direction", "interpretation");
    }

    @Test
    @SuppressWarnings("unchecked")
    void correlateMetrics_threeMetrics_returnsThreeCorrelations() {
        Map<String, Object> result = service.correlateMetrics("[\"velocity\",\"coverage\",\"bugs\"]", "last-month");

        List<Map<String, Object>> correlations = (List<Map<String, Object>>) result.get("correlations");
        assertThat(correlations).hasSize(3); // 3 pairs: v-c, v-b, c-b
    }

    @Test
    void correlateMetrics_singleMetric_returnsError() {
        Map<String, Object> result = service.correlateMetrics("[\"velocity\"]", "last-sprint");

        assertThat(result.get("error")).isEqualTo("At least 2 metrics are required for correlation");
    }

    @Test
    void correlateMetrics_emptyJson_returnsError() {
        Map<String, Object> result = service.correlateMetrics("", "last-sprint");

        assertThat(result.get("error")).isEqualTo("Metrics JSON is required");
    }

    @Test
    void correlateMetrics_nullPeriod_defaultsToAllTime() {
        Map<String, Object> result = service.correlateMetrics("[\"velocity\",\"coverage\"]", null);

        assertThat(result.get("period")).isEqualTo("all-time");
    }

    // ========== explainTrend Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void explainTrend_increasing_returnsExplanation() {
        Map<String, Object> result = service.explainTrend("velocity", "increasing", "last-month");

        assertThat(result.get("metric")).isEqualTo("velocity");
        assertThat(result.get("direction")).isEqualTo("increasing");
        assertThat((String) result.get("explanation")).contains("increasing");
        assertThat((List<String>) result.get("contributingFactors")).isNotEmpty();
        assertThat((double) result.get("confidence")).isGreaterThan(0);
    }

    @Test
    void explainTrend_decreasing_returnsExplanation() {
        Map<String, Object> result = service.explainTrend("coverage", "decreasing", "last-sprint");

        assertThat((String) result.get("explanation")).contains("decreasing");
    }

    @Test
    void explainTrend_stable_returnsExplanation() {
        Map<String, Object> result = service.explainTrend("bugs", "stable", null);

        assertThat((String) result.get("explanation")).contains("stable");
        assertThat(result.get("period")).isEqualTo("all-time");
    }

    @Test
    void explainTrend_blankMetric_returnsError() {
        Map<String, Object> result = service.explainTrend("", "increasing", "last-month");

        assertThat(result.get("error")).isEqualTo("Metric name is required");
    }

    @Test
    void explainTrend_blankDirection_returnsError() {
        Map<String, Object> result = service.explainTrend("velocity", "", "last-month");

        assertThat(result.get("error")).isEqualTo("Direction is required");
    }

    // ========== healthDashboard Tests ==========

    @Test
    @SuppressWarnings("unchecked")
    void healthDashboard_returnsAllAreas() {
        Map<String, Object> result = service.healthDashboard();

        assertThat(result.get("overallHealth")).isNotNull();
        assertThat(result.get("overallScore")).isNotNull();
        assertThat(result.get("generatedAt")).isNotNull();

        List<Map<String, Object>> areas = (List<Map<String, Object>>) result.get("areas");
        assertThat(areas).isNotEmpty();
        assertThat(areas.size()).isGreaterThanOrEqualTo(5);

        for (Map<String, Object> area : areas) {
            assertThat(area).containsKeys("name", "status", "score", "details");
        }
    }

    @Test
    void healthDashboard_overallHealthIsValid() {
        Map<String, Object> result = service.healthDashboard();

        String health = (String) result.get("overallHealth");
        assertThat(health).isIn("good", "warning", "critical");
    }

    // ========== parseMetricNames Tests ==========

    @Test
    void parseMetricNames_validArray_parsesCorrectly() {
        List<String> names = service.parseMetricNames("[\"velocity\",\"coverage\",\"bugs\"]");

        assertThat(names).containsExactly("velocity", "coverage", "bugs");
    }

    @Test
    void parseMetricNames_emptyArray_returnsEmpty() {
        List<String> names = service.parseMetricNames("[]");
        assertThat(names).isEmpty();
    }

    @Test
    void parseMetricNames_null_returnsEmpty() {
        List<String> names = service.parseMetricNames(null);
        assertThat(names).isEmpty();
    }
}
