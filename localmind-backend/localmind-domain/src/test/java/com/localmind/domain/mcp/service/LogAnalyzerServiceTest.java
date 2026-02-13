package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.LogAnalysis;
import com.localmind.domain.mcp.port.out.LogAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.atLeastOnce;

@ExtendWith(MockitoExtension.class)
class LogAnalyzerServiceTest {

    @Mock
    private LogAnalysisRepository logAnalysisRepository;

    private LogAnalyzerService service;

    @BeforeEach
    void setUp() {
        lenient().when(logAnalysisRepository.save(any(LogAnalysis.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        service = new LogAnalyzerService(logAnalysisRepository);
    }

    // ======================== analyzeLogFile ========================

    @Test
    @SuppressWarnings("unchecked")
    void analyzeLogFile_nullContent_returnsEmptyResult() {
        Map<String, Object> result = service.analyzeLogFile(null, "auto");

        assertThat(result.get("totalLines")).isEqualTo(0);
        assertThat((Map<?, ?>) result.get("levels")).isEmpty();
        assertThat((List<?>) result.get("topErrors")).isEmpty();
        verify(logAnalysisRepository).save(any(LogAnalysis.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeLogFile_emptyContent_returnsEmptyResult() {
        Map<String, Object> result = service.analyzeLogFile("", "auto");

        assertThat(result.get("totalLines")).isEqualTo(0);
        assertThat((Map<?, ?>) result.get("levels")).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeLogFile_plainFormat_countsLevels() {
        String content = "2024-01-15 10:00:00 INFO Application started\n" +
                "2024-01-15 10:00:01 ERROR Connection failed\n" +
                "2024-01-15 10:00:02 WARN Low memory\n" +
                "2024-01-15 10:00:03 ERROR Timeout occurred\n" +
                "2024-01-15 10:00:04 DEBUG Processing request";

        Map<String, Object> result = service.analyzeLogFile(content, "plain");

        assertThat(result.get("totalLines")).isEqualTo(5);
        Map<String, Integer> levels = (Map<String, Integer>) result.get("levels");
        assertThat(levels.get("INFO")).isEqualTo(1);
        assertThat(levels.get("ERROR")).isEqualTo(2);
        assertThat(levels.get("WARN")).isEqualTo(1);
        assertThat(levels.get("DEBUG")).isEqualTo(1);
        assertThat(result.get("format")).isEqualTo("plain");
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeLogFile_jsonFormat_extractsFields() {
        String content = "{\"timestamp\": \"2024-01-15T10:00:00Z\", \"level\": \"ERROR\", \"message\": \"DB connection failed\"}\n" +
                "{\"timestamp\": \"2024-01-15T10:00:01Z\", \"level\": \"INFO\", \"message\": \"Server started\"}\n" +
                "{\"timestamp\": \"2024-01-15T10:00:02Z\", \"level\": \"ERROR\", \"message\": \"DB connection failed\"}";

        Map<String, Object> result = service.analyzeLogFile(content, "json");

        assertThat(result.get("totalLines")).isEqualTo(3);
        Map<String, Integer> levels = (Map<String, Integer>) result.get("levels");
        assertThat(levels.get("ERROR")).isEqualTo(2);
        assertThat(levels.get("INFO")).isEqualTo(1);
        assertThat(result.get("format")).isEqualTo("json");
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeLogFile_autoDetectsJson() {
        // More than 50% of first 10 lines are JSON
        String content = "{\"level\": \"ERROR\", \"message\": \"err1\"}\n" +
                "{\"level\": \"INFO\", \"message\": \"info1\"}\n" +
                "{\"level\": \"WARN\", \"message\": \"warn1\"}";

        Map<String, Object> result = service.analyzeLogFile(content, "auto");

        assertThat(result.get("format")).isEqualTo("json");
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeLogFile_autoDetectsPlain() {
        String content = "2024-01-15 10:00:00 INFO Started\n" +
                "2024-01-15 10:00:01 ERROR Failed\n" +
                "Some plain text line";

        Map<String, Object> result = service.analyzeLogFile(content, "auto");

        assertThat(result.get("format")).isEqualTo("plain");
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeLogFile_normalizesWarningToWarn() {
        String content = "2024-01-15 10:00:00 WARNING Memory low\n" +
                "2024-01-15 10:00:01 WARN Disk usage high";

        Map<String, Object> result = service.analyzeLogFile(content, "plain");

        Map<String, Integer> levels = (Map<String, Integer>) result.get("levels");
        assertThat(levels.get("WARN")).isEqualTo(2);
        assertThat(levels).doesNotContainKey("WARNING");
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeLogFile_extractsTimeRange() {
        String content = "2024-01-15 08:00:00 INFO First line\n" +
                "2024-01-15 09:30:00 ERROR Middle line\n" +
                "2024-01-15 12:00:00 INFO Last line";

        Map<String, Object> result = service.analyzeLogFile(content, "plain");

        Map<String, Object> timeRange = (Map<String, Object>) result.get("timeRange");
        assertThat((String) timeRange.get("earliest")).contains("2024-01-15");
        assertThat((String) timeRange.get("latest")).contains("2024-01-15");
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeLogFile_collectsTopErrors() {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            content.append("2024-01-15 10:00:0").append(i).append(" ERROR Connection timeout\n");
        }
        for (int i = 0; i < 3; i++) {
            content.append("2024-01-15 10:01:0").append(i).append(" ERROR Null pointer exception\n");
        }

        Map<String, Object> result = service.analyzeLogFile(content.toString(), "plain");

        List<Map<String, Object>> topErrors = (List<Map<String, Object>>) result.get("topErrors");
        assertThat(topErrors).isNotEmpty();
        // First error should be the most frequent
        assertThat(topErrors.get(0).get("message")).asString().contains("Connection timeout");
        assertThat(topErrors.get(0).get("count")).isEqualTo(5);
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeLogFile_jsonAlternativeFieldNames() {
        String content = "{\"severity\": \"ERROR\", \"time\": \"2024-01-15T10:00:00Z\", \"msg\": \"Something broke\"}";

        Map<String, Object> result = service.analyzeLogFile(content, "json");

        Map<String, Integer> levels = (Map<String, Integer>) result.get("levels");
        assertThat(levels.get("ERROR")).isEqualTo(1);
    }

    @Test
    void analyzeLogFile_savesToRepository() {
        String content = "2024-01-15 10:00:00 INFO test";

        service.analyzeLogFile(content, "plain");

        ArgumentCaptor<LogAnalysis> captor = ArgumentCaptor.forClass(LogAnalysis.class);
        verify(logAnalysisRepository).save(captor.capture());

        LogAnalysis saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTotalLines()).isEqualTo(1);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // ======================== findErrorPatterns ========================

    @Test
    @SuppressWarnings("unchecked")
    void findErrorPatterns_nullContent_returnsEmpty() {
        Map<String, Object> result = service.findErrorPatterns(null, 2);

        assertThat(result.get("totalPatternsFound")).isEqualTo(0);
        assertThat((List<?>) result.get("patterns")).isEmpty();
        assertThat(result.get("minCount")).isEqualTo(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findErrorPatterns_normalizesAndGroupsErrors() {
        String content = "2024-01-15 10:00:00 ERROR Connection to 192.168.1.1 failed after 30s\n" +
                "2024-01-15 10:00:05 ERROR Connection to 10.0.0.1 failed after 45s\n" +
                "2024-01-15 10:00:10 ERROR Connection to 172.16.0.1 failed after 60s";

        Map<String, Object> result = service.findErrorPatterns(content, 2);

        List<Map<String, Object>> patterns = (List<Map<String, Object>>) result.get("patterns");
        assertThat(patterns).isNotEmpty();
        // All three should be grouped into one pattern since IPs and numbers are normalized
        assertThat((int) patterns.get(0).get("count")).isGreaterThanOrEqualTo(3);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findErrorPatterns_respectsMinCount() {
        String content = "2024-01-15 10:00:00 ERROR Error A occurred\n" +
                "2024-01-15 10:00:01 ERROR Error B occurred\n" +
                "2024-01-15 10:00:02 ERROR Error A occurred\n" +
                "2024-01-15 10:00:03 ERROR Error A occurred";

        Map<String, Object> result = service.findErrorPatterns(content, 3);

        List<Map<String, Object>> patterns = (List<Map<String, Object>>) result.get("patterns");
        // Only "Error A occurred" should pass minCount=3
        for (Map<String, Object> pattern : patterns) {
            assertThat((int) pattern.get("count")).isGreaterThanOrEqualTo(3);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void findErrorPatterns_collectsUpTo3Examples() {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            content.append("2024-01-15 10:00:0").append(i)
                    .append(" ERROR Failed to process request ").append(i).append("\n");
        }

        Map<String, Object> result = service.findErrorPatterns(content.toString(), 2);

        List<Map<String, Object>> patterns = (List<Map<String, Object>>) result.get("patterns");
        assertThat(patterns).isNotEmpty();
        List<?> examples = (List<?>) patterns.get(0).get("examples");
        assertThat(examples.size()).isLessThanOrEqualTo(3);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findErrorPatterns_defaultMinCountIs2() {
        String content = "2024-01-15 10:00:00 ERROR Single error\n" +
                "2024-01-15 10:00:01 ERROR Repeated error\n" +
                "2024-01-15 10:00:02 ERROR Repeated error";

        Map<String, Object> result = service.findErrorPatterns(content, 0);

        assertThat(result.get("minCount")).isEqualTo(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findErrorPatterns_normalizesUuidsAndHex() {
        String content = "2024-01-15 10:00:00 ERROR Request a1b2c3d4-e5f6-7890-abcd-ef1234567890 failed\n" +
                "2024-01-15 10:00:01 ERROR Request 11111111-2222-3333-4444-555555555555 failed\n" +
                "2024-01-15 10:00:02 ERROR Hash deadbeef01234567 invalid\n" +
                "2024-01-15 10:00:03 ERROR Hash abcdef0123456789 invalid";

        Map<String, Object> result = service.findErrorPatterns(content, 2);

        List<Map<String, Object>> patterns = (List<Map<String, Object>>) result.get("patterns");
        assertThat(patterns.size()).isGreaterThanOrEqualTo(1);
    }

    // ======================== tailLog ========================

    @Test
    void tailLog_nullContent_returnsEmpty() {
        Map<String, Object> result = service.tailLog(null, 50, null);

        assertThat(result.get("totalLines")).isEqualTo(0);
        assertThat(result.get("returnedLines")).isEqualTo(0);
        assertThat(result.get("content")).isEqualTo("");
    }

    @Test
    void tailLog_returnsLastNLines() {
        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= 100; i++) {
            content.append("Line ").append(i).append("\n");
        }

        Map<String, Object> result = service.tailLog(content.toString(), 10, null);

        assertThat(result.get("totalLines")).isEqualTo(100);
        assertThat(result.get("returnedLines")).isEqualTo(10);
        String resultContent = (String) result.get("content");
        assertThat(resultContent).contains("Line 91");
        assertThat(resultContent).contains("Line 100");
        assertThat(resultContent).doesNotContain("Line 90");
    }

    @Test
    void tailLog_filtersCaseInsensitive() {
        String content = "INFO Application started\n" +
                "ERROR Connection failed\n" +
                "INFO Processing\n" +
                "error Timeout occurred\n" +
                "DEBUG Details";

        Map<String, Object> result = service.tailLog(content, 50, "error");

        assertThat(result.get("returnedLines")).isEqualTo(2);
        String resultContent = (String) result.get("content");
        assertThat(resultContent).contains("ERROR Connection failed");
        assertThat(resultContent).contains("error Timeout occurred");
    }

    @Test
    void tailLog_defaultLines50() {
        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= 200; i++) {
            content.append("Line ").append(i).append("\n");
        }

        Map<String, Object> result = service.tailLog(content.toString(), 0, null);

        assertThat(result.get("returnedLines")).isEqualTo(50);
    }

    @Test
    void tailLog_filterAndTail() {
        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= 100; i++) {
            if (i % 2 == 0) {
                content.append("ERROR Line ").append(i).append("\n");
            } else {
                content.append("INFO Line ").append(i).append("\n");
            }
        }

        Map<String, Object> result = service.tailLog(content.toString(), 5, "ERROR");

        assertThat(result.get("returnedLines")).isEqualTo(5);
        String resultContent = (String) result.get("content");
        // Last 5 ERROR lines should be lines 92, 94, 96, 98, 100
        assertThat(resultContent).contains("ERROR Line 100");
        assertThat(resultContent).contains("ERROR Line 92");
    }

    // ======================== generateLogSummary ========================

    @Test
    void generateLogSummary_nullContent_returnsDefaultSummary() {
        Map<String, Object> result = service.generateLogSummary(null);

        assertThat(result.get("summary")).isEqualTo("No log content provided.");
        assertThat(result.get("totalLines")).isEqualTo(0);
        assertThat(result.get("errorRate")).isEqualTo(0.0);
        assertThat(result.get("warningRate")).isEqualTo(0.0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateLogSummary_calculatesRates() {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 10; i++) content.append("2024-01-15 10:00:00 INFO ok\n");
        for (int i = 0; i < 5; i++) content.append("2024-01-15 10:00:00 ERROR fail\n");
        for (int i = 0; i < 3; i++) content.append("2024-01-15 10:00:00 WARN slow\n");
        for (int i = 0; i < 2; i++) content.append("2024-01-15 10:00:00 DEBUG detail\n");

        Map<String, Object> result = service.generateLogSummary(content.toString());

        assertThat(result.get("totalLines")).isEqualTo(20);
        double errorRate = (double) result.get("errorRate");
        double warningRate = (double) result.get("warningRate");
        // 5 errors out of 20 = 25%
        assertThat(errorRate).isEqualTo(25.0);
        // 3 warnings out of 20 = 15%
        assertThat(warningRate).isEqualTo(15.0);

        String summary = (String) result.get("summary");
        assertThat(summary).contains("Total lines: 20");
        assertThat(summary).contains("Error rate: 25.0%");
        assertThat(summary).contains("Warning rate: 15.0%");
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateLogSummary_includesLevelBreakdown() {
        String content = "2024-01-15 10:00:00 INFO Started\n" +
                "2024-01-15 10:00:01 ERROR Failed\n" +
                "2024-01-15 10:00:02 WARN Low mem";

        Map<String, Object> result = service.generateLogSummary(content);

        Map<String, Integer> levels = (Map<String, Integer>) result.get("levels");
        assertThat(levels).containsEntry("INFO", 1);
        assertThat(levels).containsEntry("ERROR", 1);
        assertThat(levels).containsEntry("WARN", 1);

        String summary = (String) result.get("summary");
        assertThat(summary).contains("Level breakdown:");
        assertThat(summary).contains("INFO:");
        assertThat(summary).contains("ERROR:");
        assertThat(summary).contains("WARN:");
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateLogSummary_truncatesLongErrorMessages() {
        // Create an error message longer than 100 chars
        String longMsg = "A".repeat(150);
        String content = "2024-01-15 10:00:00 ERROR " + longMsg + "\n" +
                "2024-01-15 10:00:01 ERROR " + longMsg;

        Map<String, Object> result = service.generateLogSummary(content);

        String summary = (String) result.get("summary");
        assertThat(summary).contains("...");
    }

    @Test
    void generateLogSummary_savesToRepository() {
        String content = "2024-01-15 10:00:00 INFO test";

        service.generateLogSummary(content);

        verify(logAnalysisRepository, atLeastOnce()).save(any(LogAnalysis.class));
    }

    // ======================== Cross-cutting ========================

    @Test
    @SuppressWarnings("unchecked")
    void analyzeLogFile_handlesFatalAndCriticalLevels() {
        String content = "2024-01-15 10:00:00 FATAL System crash\n" +
                "2024-01-15 10:00:01 CRITICAL Memory exhausted";

        Map<String, Object> result = service.analyzeLogFile(content, "plain");

        Map<String, Integer> levels = (Map<String, Integer>) result.get("levels");
        assertThat(levels.get("FATAL")).isEqualTo(1);
        assertThat(levels.get("CRITICAL")).isEqualTo(1);

        // Both should be counted as error-level in topErrors
        List<Map<String, Object>> topErrors = (List<Map<String, Object>>) result.get("topErrors");
        assertThat(topErrors).hasSize(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void analyzeLogFile_handlesTraceLevelLogs() {
        String content = "2024-01-15 10:00:00 TRACE Entering method doSomething\n" +
                "2024-01-15 10:00:01 TRACE Exiting method doSomething";

        Map<String, Object> result = service.analyzeLogFile(content, "plain");

        Map<String, Integer> levels = (Map<String, Integer>) result.get("levels");
        assertThat(levels.get("TRACE")).isEqualTo(2);
    }
}
