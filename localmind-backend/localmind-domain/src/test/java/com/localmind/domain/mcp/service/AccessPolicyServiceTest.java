package com.localmind.domain.mcp.service;

import com.localmind.domain.mcp.model.AccessAuditEntry;
import com.localmind.domain.mcp.model.AccessPolicy;
import com.localmind.domain.mcp.port.out.AccessPolicyRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccessPolicyServiceTest {

    @Mock
    private AccessPolicyRepository repository;

    private AccessPolicyService service;

    @BeforeEach
    void setUp() {
        service = new AccessPolicyService(repository);
        when(repository.save(any(AccessPolicy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.saveAudit(any(AccessAuditEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ========== createPolicy Tests ==========

    @Test
    void createPolicy_withValidData_returnsPolicy() {
        Map<String, Object> result = service.createPolicy("admin-access", "allow", "{\"server\":\"*\"}");

        assertThat(result.get("name")).isEqualTo("admin-access");
        assertThat(result.get("effect")).isEqualTo("allow");
        assertThat(result.get("rules")).isEqualTo("{\"server\":\"*\"}");
        assertThat(result.get("id")).isNotNull();
        assertThat(result.get("createdAt")).isNotNull();
    }

    @Test
    void createPolicy_withDenyEffect_normalizes() {
        Map<String, Object> result = service.createPolicy("block-tool", "DENY", "{\"tool\":\"dangerous\"}");

        assertThat(result.get("effect")).isEqualTo("deny");
    }

    @Test
    void createPolicy_withBlankName_returnsError() {
        Map<String, Object> result = service.createPolicy("", "allow", "{}");

        assertThat(result.get("error")).isEqualTo("Policy name cannot be blank");
        verify(repository, never()).save(any(AccessPolicy.class));
    }

    @Test
    void createPolicy_withNullName_returnsError() {
        Map<String, Object> result = service.createPolicy(null, "allow", "{}");

        assertThat(result.get("error")).isEqualTo("Policy name cannot be blank");
        verify(repository, never()).save(any(AccessPolicy.class));
    }

    @Test
    void createPolicy_withInvalidEffect_returnsError() {
        Map<String, Object> result = service.createPolicy("test", "invalid", "{}");

        assertThat(result.get("error")).isEqualTo("Effect must be 'allow' or 'deny'");
        verify(repository, never()).save(any(AccessPolicy.class));
    }

    @Test
    void createPolicy_withNullEffect_returnsError() {
        Map<String, Object> result = service.createPolicy("test", null, "{}");

        assertThat(result.get("error")).isEqualTo("Effect must be 'allow' or 'deny'");
    }

    @Test
    void createPolicy_withNullRulesJson_defaultsToEmptyJson() {
        Map<String, Object> result = service.createPolicy("test", "allow", null);

        assertThat(result.get("rules")).isEqualTo("{}");
    }

    @Test
    void createPolicy_savesToRepository() {
        service.createPolicy("my-policy", "allow", "{\"server\":\"test\"}");

        ArgumentCaptor<AccessPolicy> captor = ArgumentCaptor.forClass(AccessPolicy.class);
        verify(repository).save(captor.capture());
        AccessPolicy saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("my-policy");
        assertThat(saved.getEffect()).isEqualTo("allow");
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    // ========== checkAccess Tests ==========

    @Test
    void checkAccess_withMatchingAllowPolicy_returnsAllowed() {
        AccessPolicy policy = AccessPolicy.builder()
                .id("policy-1")
                .name("allow-test-server")
                .effect("allow")
                .rulesJson("{\"server\":\"test-server\",\"tool\":\"run-query\"}")
                .createdAt(Instant.now())
                .build();
        when(repository.findAll()).thenReturn(List.of(policy));

        Map<String, Object> result = service.checkAccess("user-1", "test-server", "run-query");

        assertThat(result.get("allowed")).isEqualTo(true);
        assertThat(result.get("userId")).isEqualTo("user-1");
        assertThat(result.get("server")).isEqualTo("test-server");
        assertThat(result.get("tool")).isEqualTo("run-query");
    }

    @Test
    void checkAccess_withMatchingDenyPolicy_returnsDenied() {
        AccessPolicy policy = AccessPolicy.builder()
                .id("policy-2")
                .name("deny-dangerous")
                .effect("deny")
                .rulesJson("{\"server\":\"prod-server\",\"tool\":\"delete-all\"}")
                .createdAt(Instant.now())
                .build();
        when(repository.findAll()).thenReturn(List.of(policy));

        Map<String, Object> result = service.checkAccess("user-1", "prod-server", "delete-all");

        assertThat(result.get("allowed")).isEqualTo(false);
    }

    @Test
    void checkAccess_withNoMatchingPolicy_defaultsToAllow() {
        when(repository.findAll()).thenReturn(new ArrayList<>());

        Map<String, Object> result = service.checkAccess("user-1", "unknown-server", "unknown-tool");

        assertThat(result.get("allowed")).isEqualTo(true);
        assertThat(result.get("matchedPolicy")).isNull();
    }

    @Test
    void checkAccess_withWildcardPolicy_matchesAnyTool() {
        AccessPolicy policy = AccessPolicy.builder()
                .id("policy-3")
                .name("allow-all-tools")
                .effect("allow")
                .rulesJson("{\"server\":\"my-server\",\"tool\":\"*\"}")
                .createdAt(Instant.now())
                .build();
        when(repository.findAll()).thenReturn(List.of(policy));

        Map<String, Object> result = service.checkAccess("user-1", "my-server", "any-tool");

        assertThat(result.get("allowed")).isEqualTo(true);
        assertThat(result.get("matchedPolicy")).isNotNull();
    }

    @Test
    void checkAccess_recordsAuditEntry() {
        when(repository.findAll()).thenReturn(new ArrayList<>());

        service.checkAccess("user-1", "server-1", "tool-1");

        ArgumentCaptor<AccessAuditEntry> captor = ArgumentCaptor.forClass(AccessAuditEntry.class);
        verify(repository).saveAudit(captor.capture());
        AccessAuditEntry audit = captor.getValue();
        assertThat(audit.getUserId()).isEqualTo("user-1");
        assertThat(audit.getServer()).isEqualTo("server-1");
        assertThat(audit.getTool()).isEqualTo("tool-1");
        assertThat(audit.isAllowed()).isTrue();
    }

    @Test
    void checkAccess_withBlankUserId_returnsError() {
        Map<String, Object> result = service.checkAccess("", "server", "tool");

        assertThat(result.get("error")).isEqualTo("User ID cannot be blank");
    }

    @Test
    void checkAccess_withBlankServer_returnsError() {
        Map<String, Object> result = service.checkAccess("user", "", "tool");

        assertThat(result.get("error")).isEqualTo("Server cannot be blank");
    }

    @Test
    void checkAccess_withBlankTool_returnsError() {
        Map<String, Object> result = service.checkAccess("user", "server", "");

        assertThat(result.get("error")).isEqualTo("Tool cannot be blank");
    }

    // ========== listPolicies Tests ==========

    @Test
    void listPolicies_withPolicies_returnsList() {
        AccessPolicy p1 = AccessPolicy.builder().id("id-1").name("policy-1").effect("allow")
                .rulesJson("{}").createdAt(Instant.now()).build();
        AccessPolicy p2 = AccessPolicy.builder().id("id-2").name("policy-2").effect("deny")
                .rulesJson("{}").createdAt(Instant.now()).build();
        when(repository.findAll()).thenReturn(List.of(p1, p2));

        Map<String, Object> result = service.listPolicies();

        assertThat(result.get("count")).isEqualTo(2);
        List<Map<String, Object>> policies = (List<Map<String, Object>>) result.get("policies");
        assertThat(policies).hasSize(2);
        assertThat(policies.get(0).get("name")).isEqualTo("policy-1");
        assertThat(policies.get(1).get("name")).isEqualTo("policy-2");
    }

    @Test
    void listPolicies_withEmptyList_returnsEmptyResult() {
        when(repository.findAll()).thenReturn(new ArrayList<>());

        Map<String, Object> result = service.listPolicies();

        assertThat(result.get("count")).isEqualTo(0);
        List<Map<String, Object>> policies = (List<Map<String, Object>>) result.get("policies");
        assertThat(policies).isEmpty();
    }

    // ========== assignRole Tests ==========

    @Test
    void assignRole_withValidData_returnsAssignment() {
        Map<String, Object> result = service.assignRole("user-1", "admin");

        assertThat(result.get("userId")).isEqualTo("user-1");
        assertThat(result.get("roleName")).isEqualTo("admin");
        assertThat(result.get("assigned")).isEqualTo(true);
    }

    @Test
    void assignRole_savesPolicy() {
        service.assignRole("user-1", "developer");

        ArgumentCaptor<AccessPolicy> captor = ArgumentCaptor.forClass(AccessPolicy.class);
        verify(repository).save(captor.capture());
        AccessPolicy saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("role-developer-user-1");
        assertThat(saved.getEffect()).isEqualTo("allow");
        assertThat(saved.getRulesJson()).contains("developer");
        assertThat(saved.getRulesJson()).contains("user-1");
    }

    @Test
    void assignRole_withBlankUserId_returnsError() {
        Map<String, Object> result = service.assignRole("", "admin");

        assertThat(result.get("error")).isEqualTo("User ID cannot be blank");
        verify(repository, never()).save(any(AccessPolicy.class));
    }

    @Test
    void assignRole_withBlankRoleName_returnsError() {
        Map<String, Object> result = service.assignRole("user-1", "");

        assertThat(result.get("error")).isEqualTo("Role name cannot be blank");
        verify(repository, never()).save(any(AccessPolicy.class));
    }

    // ========== auditAccess Tests ==========

    @Test
    void auditAccess_returnsFilteredEntries() {
        AccessAuditEntry e1 = AccessAuditEntry.builder().id("a1").userId("user-1")
                .server("server-1").tool("tool-1").allowed(true)
                .createdAt(Instant.now()).build();
        AccessAuditEntry e2 = AccessAuditEntry.builder().id("a2").userId("user-1")
                .server("server-2").tool("tool-2").allowed(false)
                .createdAt(Instant.now()).build();
        when(repository.findAuditByUserId("user-1")).thenReturn(List.of(e1, e2));

        Map<String, Object> result = service.auditAccess("user-1", "server-1", 50);

        assertThat(result.get("count")).isEqualTo(1);
        List<Map<String, Object>> entries = (List<Map<String, Object>>) result.get("entries");
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).get("server")).isEqualTo("server-1");
    }

    @Test
    void auditAccess_withNullServer_returnsAllEntries() {
        AccessAuditEntry e1 = AccessAuditEntry.builder().id("a1").userId("user-1")
                .server("s1").tool("t1").allowed(true).createdAt(Instant.now()).build();
        AccessAuditEntry e2 = AccessAuditEntry.builder().id("a2").userId("user-1")
                .server("s2").tool("t2").allowed(false).createdAt(Instant.now()).build();
        when(repository.findAuditByUserId("user-1")).thenReturn(List.of(e1, e2));

        Map<String, Object> result = service.auditAccess("user-1", null, 50);

        assertThat(result.get("count")).isEqualTo(2);
    }

    @Test
    void auditAccess_withLimit_respectsLimit() {
        List<AccessAuditEntry> entries = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            entries.add(AccessAuditEntry.builder().id("a" + i).userId("user-1")
                    .server("s").tool("t").allowed(true).createdAt(Instant.now()).build());
        }
        when(repository.findAuditByUserId("user-1")).thenReturn(entries);

        Map<String, Object> result = service.auditAccess("user-1", null, 3);

        assertThat(result.get("count")).isEqualTo(3);
    }

    @Test
    void auditAccess_withBlankUserId_returnsError() {
        Map<String, Object> result = service.auditAccess("", null, 50);

        assertThat(result.get("error")).isEqualTo("User ID cannot be blank");
    }

    @Test
    void auditAccess_withZeroLimit_defaultsTo50() {
        when(repository.findAuditByUserId("user-1")).thenReturn(new ArrayList<>());

        Map<String, Object> result = service.auditAccess("user-1", null, 0);

        // Should not fail, returns empty list
        assertThat(result.get("count")).isEqualTo(0);
    }
}
