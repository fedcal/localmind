package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.DecisionLink;
import com.localmind.domain.mcp.model.DecisionRecord;
import com.localmind.domain.mcp.port.out.DecisionLogRepository;
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
class DecisionLogServiceTest {

    @Mock
    private DecisionLogRepository repository;

    private DecisionLogService service;

    @BeforeEach
    void setUp() {
        service = new DecisionLogService(repository);
        when(repository.save(any(DecisionRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.saveLink(any(DecisionLink.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ========== recordDecision Tests ==========

    @Test
    void recordDecision_withValidData_returnsDecision() {
        Map<String, Object> result = service.recordDecision(
                "Use PostgreSQL", "Need a relational database", "Chose PostgreSQL for its features",
                "[\"MySQL\",\"SQLite\"]", "Need to manage migrations", "accepted");

        assertThat(result.get("title")).isEqualTo("Use PostgreSQL");
        assertThat(result.get("context")).isEqualTo("Need a relational database");
        assertThat(result.get("decision")).isEqualTo("Chose PostgreSQL for its features");
        assertThat(result.get("alternatives")).isEqualTo("[\"MySQL\",\"SQLite\"]");
        assertThat(result.get("consequences")).isEqualTo("Need to manage migrations");
        assertThat(result.get("status")).isEqualTo("accepted");
        assertThat(result.get("id")).isNotNull();
        assertThat(result.get("createdAt")).isNotNull();
    }

    @Test
    void recordDecision_withNullStatus_defaultsToProposed() {
        Map<String, Object> result = service.recordDecision(
                "Title", "Context", "Decision", null, null, null);

        assertThat(result.get("status")).isEqualTo("proposed");
    }

    @Test
    void recordDecision_withInvalidStatus_defaultsToProposed() {
        Map<String, Object> result = service.recordDecision(
                "Title", "Context", "Decision", null, null, "invalid");

        assertThat(result.get("status")).isEqualTo("proposed");
    }

    @Test
    void recordDecision_withBlankTitle_returnsError() {
        Map<String, Object> result = service.recordDecision("", "Context", "Decision", null, null, null);

        assertThat(result.get("error")).isEqualTo("Title cannot be blank");
        verify(repository, never()).save(any(DecisionRecord.class));
    }

    @Test
    void recordDecision_withNullTitle_returnsError() {
        Map<String, Object> result = service.recordDecision(null, "Context", "Decision", null, null, null);

        assertThat(result.get("error")).isEqualTo("Title cannot be blank");
    }

    @Test
    void recordDecision_withBlankContext_returnsError() {
        Map<String, Object> result = service.recordDecision("Title", "", "Decision", null, null, null);

        assertThat(result.get("error")).isEqualTo("Context cannot be blank");
    }

    @Test
    void recordDecision_withBlankDecision_returnsError() {
        Map<String, Object> result = service.recordDecision("Title", "Context", "", null, null, null);

        assertThat(result.get("error")).isEqualTo("Decision cannot be blank");
    }

    @Test
    void recordDecision_savesToRepository() {
        service.recordDecision("Title", "Context", "Decision", null, null, "proposed");

        ArgumentCaptor<DecisionRecord> captor = ArgumentCaptor.forClass(DecisionRecord.class);
        verify(repository).save(captor.capture());
        DecisionRecord saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Title");
        assertThat(saved.getContext()).isEqualTo("Context");
        assertThat(saved.getDecision()).isEqualTo("Decision");
        assertThat(saved.getStatus()).isEqualTo("proposed");
        assertThat(saved.getId()).isNotNull();
    }

    // ========== listDecisions Tests ==========

    @Test
    void listDecisions_withNullFilters_returnsAll() {
        DecisionRecord r1 = buildRecord("id-1", "Decision A", "proposed");
        DecisionRecord r2 = buildRecord("id-2", "Decision B", "accepted");
        when(repository.findAll()).thenReturn(List.of(r1, r2));

        Map<String, Object> result = service.listDecisions(null, null, 50);

        assertThat(result.get("count")).isEqualTo(2);
        List<Map<String, Object>> decisions = (List<Map<String, Object>>) result.get("decisions");
        assertThat(decisions).hasSize(2);
    }

    @Test
    void listDecisions_withStatusFilter_filtersCorrectly() {
        DecisionRecord r1 = buildRecord("id-1", "Decision A", "proposed");
        DecisionRecord r2 = buildRecord("id-2", "Decision B", "accepted");
        when(repository.findAll()).thenReturn(List.of(r1, r2));

        Map<String, Object> result = service.listDecisions("accepted", null, 50);

        assertThat(result.get("count")).isEqualTo(1);
        List<Map<String, Object>> decisions = (List<Map<String, Object>>) result.get("decisions");
        assertThat(decisions.get(0).get("title")).isEqualTo("Decision B");
    }

    @Test
    void listDecisions_withSearchFilter_searchesTitleContextDecision() {
        DecisionRecord r1 = DecisionRecord.builder()
                .id("id-1").title("Use Redis").context("Caching needed").decision("Redis chosen")
                .status("accepted").createdAt(Instant.now()).build();
        DecisionRecord r2 = DecisionRecord.builder()
                .id("id-2").title("Use Kafka").context("Event streaming").decision("Kafka chosen")
                .status("accepted").createdAt(Instant.now()).build();
        when(repository.findAll()).thenReturn(List.of(r1, r2));

        Map<String, Object> result = service.listDecisions(null, "redis", 50);

        assertThat(result.get("count")).isEqualTo(1);
        List<Map<String, Object>> decisions = (List<Map<String, Object>>) result.get("decisions");
        assertThat(decisions.get(0).get("title")).isEqualTo("Use Redis");
    }

    @Test
    void listDecisions_withSearchInContext_matchesContext() {
        DecisionRecord r1 = DecisionRecord.builder()
                .id("id-1").title("Title A").context("Need caching layer").decision("Decision A")
                .status("proposed").createdAt(Instant.now()).build();
        when(repository.findAll()).thenReturn(List.of(r1));

        Map<String, Object> result = service.listDecisions(null, "caching", 50);

        assertThat(result.get("count")).isEqualTo(1);
    }

    @Test
    void listDecisions_withLimit_respectsLimit() {
        List<DecisionRecord> records = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            records.add(buildRecord("id-" + i, "Decision " + i, "proposed"));
        }
        when(repository.findAll()).thenReturn(records);

        Map<String, Object> result = service.listDecisions(null, null, 3);

        assertThat(result.get("count")).isEqualTo(3);
    }

    @Test
    void listDecisions_withEmptyList_returnsEmptyResult() {
        when(repository.findAll()).thenReturn(new ArrayList<>());

        Map<String, Object> result = service.listDecisions(null, null, 50);

        assertThat(result.get("count")).isEqualTo(0);
        List<Map<String, Object>> decisions = (List<Map<String, Object>>) result.get("decisions");
        assertThat(decisions).isEmpty();
    }

    // ========== getDecision Tests ==========

    @Test
    void getDecision_whenFound_returnsDecisionWithLinks() {
        DecisionRecord record = buildRecord("id-1", "Decision A", "accepted");
        DecisionLink link = DecisionLink.builder()
                .id("link-1").decisionId("id-1").linkType("ticket")
                .targetId("JIRA-123").description("Related ticket")
                .createdAt(Instant.now()).build();

        when(repository.findById("id-1")).thenReturn(Optional.of(record));
        when(repository.findLinksByDecisionId("id-1")).thenReturn(List.of(link));

        Map<String, Object> result = service.getDecision("id-1");

        Map<String, Object> decisionMap = (Map<String, Object>) result.get("decision");
        assertThat(decisionMap.get("title")).isEqualTo("Decision A");

        List<Map<String, Object>> links = (List<Map<String, Object>>) result.get("links");
        assertThat(links).hasSize(1);
        assertThat(links.get(0).get("linkType")).isEqualTo("ticket");
        assertThat(links.get(0).get("targetId")).isEqualTo("JIRA-123");
    }

    @Test
    void getDecision_whenNotFound_returnsError() {
        when(repository.findById("nonexistent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.getDecision("nonexistent");

        assertThat(result.get("error")).isEqualTo("Decision not found: nonexistent");
    }

    @Test
    void getDecision_withBlankId_returnsError() {
        Map<String, Object> result = service.getDecision("");

        assertThat(result.get("error")).isEqualTo("Decision ID cannot be blank");
    }

    // ========== supersedeDecision Tests ==========

    @Test
    void supersedeDecision_whenFound_updatesStatus() {
        DecisionRecord existing = buildRecord("id-1", "Old Decision", "accepted");
        when(repository.findById("id-1")).thenReturn(Optional.of(existing));

        Map<String, Object> result = service.supersedeDecision("id-1", "id-2");

        assertThat(result.get("id")).isEqualTo("id-1");
        assertThat(result.get("status")).isEqualTo("superseded");
        assertThat(result.get("supersededBy")).isEqualTo("id-2");

        ArgumentCaptor<DecisionRecord> captor = ArgumentCaptor.forClass(DecisionRecord.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("superseded");
    }

    @Test
    void supersedeDecision_whenNotFound_returnsError() {
        when(repository.findById("nonexistent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.supersedeDecision("nonexistent", "id-2");

        assertThat(result.get("error")).isEqualTo("Decision not found: nonexistent");
    }

    @Test
    void supersedeDecision_withBlankId_returnsError() {
        Map<String, Object> result = service.supersedeDecision("", "id-2");

        assertThat(result.get("error")).isEqualTo("Decision ID cannot be blank");
    }

    @Test
    void supersedeDecision_withBlankSupersededById_returnsError() {
        Map<String, Object> result = service.supersedeDecision("id-1", "");

        assertThat(result.get("error")).isEqualTo("Superseded-by ID cannot be blank");
    }

    // ========== linkDecision Tests ==========

    @Test
    void linkDecision_withValidData_createsLink() {
        DecisionRecord record = buildRecord("id-1", "Decision A", "accepted");
        when(repository.findById("id-1")).thenReturn(Optional.of(record));

        Map<String, Object> result = service.linkDecision("id-1", "ticket", "JIRA-456", "Bug fix");

        assertThat(result.get("decisionId")).isEqualTo("id-1");
        assertThat(result.get("linkType")).isEqualTo("ticket");
        assertThat(result.get("targetId")).isEqualTo("JIRA-456");
        assertThat(result.get("description")).isEqualTo("Bug fix");
        assertThat(result.get("id")).isNotNull();
    }

    @Test
    void linkDecision_withCommitType_createsLink() {
        DecisionRecord record = buildRecord("id-1", "Decision A", "accepted");
        when(repository.findById("id-1")).thenReturn(Optional.of(record));

        Map<String, Object> result = service.linkDecision("id-1", "commit", "abc123", null);

        assertThat(result.get("linkType")).isEqualTo("commit");
        assertThat(result.get("targetId")).isEqualTo("abc123");
    }

    @Test
    void linkDecision_withInvalidLinkType_returnsError() {
        Map<String, Object> result = service.linkDecision("id-1", "invalid-type", "target", null);

        assertThat(result.get("error")).isEqualTo("Link type must be one of: ticket, commit, impact, related");
        verify(repository, never()).saveLink(any(DecisionLink.class));
    }

    @Test
    void linkDecision_withNullLinkType_returnsError() {
        Map<String, Object> result = service.linkDecision("id-1", null, "target", null);

        assertThat(result.get("error")).isEqualTo("Link type must be one of: ticket, commit, impact, related");
    }

    @Test
    void linkDecision_withBlankTargetId_returnsError() {
        Map<String, Object> result = service.linkDecision("id-1", "ticket", "", null);

        assertThat(result.get("error")).isEqualTo("Target ID cannot be blank");
    }

    @Test
    void linkDecision_whenDecisionNotFound_returnsError() {
        when(repository.findById("nonexistent")).thenReturn(Optional.empty());

        Map<String, Object> result = service.linkDecision("nonexistent", "ticket", "target", null);

        assertThat(result.get("error")).isEqualTo("Decision not found: nonexistent");
    }

    @Test
    void linkDecision_savesLinkToRepository() {
        DecisionRecord record = buildRecord("id-1", "Decision A", "accepted");
        when(repository.findById("id-1")).thenReturn(Optional.of(record));

        service.linkDecision("id-1", "impact", "perf-improvement", "Improved latency");

        ArgumentCaptor<DecisionLink> captor = ArgumentCaptor.forClass(DecisionLink.class);
        verify(repository).saveLink(captor.capture());
        DecisionLink saved = captor.getValue();
        assertThat(saved.getDecisionId()).isEqualTo("id-1");
        assertThat(saved.getLinkType()).isEqualTo("impact");
        assertThat(saved.getTargetId()).isEqualTo("perf-improvement");
        assertThat(saved.getDescription()).isEqualTo("Improved latency");
    }

    // ========== Helper Methods ==========

    private DecisionRecord buildRecord(String id, String title, String status) {
        return DecisionRecord.builder()
                .id(id)
                .title(title)
                .context("Some context")
                .decision("Some decision")
                .status(status)
                .createdAt(Instant.now())
                .build();
    }
}
