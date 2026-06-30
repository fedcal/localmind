package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.AccessAuditEntry;
import com.localmind.domain.mcp.model.AccessPolicy;
import com.localmind.infrastructure.persistence.entity.mcp.AccessAuditEntity;
import com.localmind.infrastructure.persistence.entity.mcp.AccessPolicyEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaAccessAuditRepository;
import com.localmind.infrastructure.persistence.repository.mcp.JpaAccessPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessPolicyRepositoryAdapterTest {

    @Mock
    private JpaAccessPolicyRepository policyJpaRepository;

    @Mock
    private JpaAccessAuditRepository auditJpaRepository;

    private AccessPolicyRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final UUID TEST_UUID_2 = UUID.fromString("b1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-13T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new AccessPolicyRepositoryAdapter(policyJpaRepository, auditJpaRepository);
    }

    // --- AccessPolicy Tests ---

    @Test
    void savePolicy_mapsDomainToEntityAndBack() {
        AccessPolicy domain = AccessPolicy.builder()
                .id(TEST_UUID.toString())
                .name("admin-access")
                .effect("allow")
                .rulesJson("{\"server\":\"*\"}")
                .createdAt(NOW)
                .build();

        AccessPolicyEntity savedEntity = AccessPolicyEntity.builder()
                .id(TEST_UUID)
                .name("admin-access")
                .effect("allow")
                .rulesJson("{\"server\":\"*\"}")
                .createdAt(NOW)
                .build();

        when(policyJpaRepository.save(any(AccessPolicyEntity.class))).thenReturn(savedEntity);

        AccessPolicy result = adapter.save(domain);

        ArgumentCaptor<AccessPolicyEntity> captor = ArgumentCaptor.forClass(AccessPolicyEntity.class);
        verify(policyJpaRepository).save(captor.capture());

        AccessPolicyEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getName()).isEqualTo("admin-access");
        assertThat(captured.getEffect()).isEqualTo("allow");
        assertThat(captured.getRulesJson()).isEqualTo("{\"server\":\"*\"}");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getName()).isEqualTo("admin-access");
        assertThat(result.getEffect()).isEqualTo("allow");
        assertThat(result.getRulesJson()).isEqualTo("{\"server\":\"*\"}");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void findAll_mapsEntitiesCorrectly() {
        AccessPolicyEntity entity1 = AccessPolicyEntity.builder()
                .id(TEST_UUID).name("policy-1").effect("allow")
                .rulesJson("{}").createdAt(NOW).build();
        AccessPolicyEntity entity2 = AccessPolicyEntity.builder()
                .id(TEST_UUID_2).name("policy-2").effect("deny")
                .rulesJson("{}").createdAt(NOW).build();

        when(policyJpaRepository.findAll()).thenReturn(List.of(entity1, entity2));

        List<AccessPolicy> result = adapter.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("policy-1");
        assertThat(result.get(1).getName()).isEqualTo("policy-2");
    }

    @Test
    void findById_whenFound_returnsMappedDomain() {
        AccessPolicyEntity entity = AccessPolicyEntity.builder()
                .id(TEST_UUID).name("test-policy").effect("deny")
                .rulesJson("{\"tool\":\"delete\"}").createdAt(NOW).build();

        when(policyJpaRepository.findById(TEST_UUID)).thenReturn(Optional.of(entity));

        Optional<AccessPolicy> result = adapter.findById(TEST_UUID.toString());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("test-policy");
        assertThat(result.get().getEffect()).isEqualTo("deny");
    }

    // --- AccessAuditEntry Tests ---

    @Test
    void saveAudit_mapsDomainToEntityAndBack() {
        AccessAuditEntry domain = AccessAuditEntry.builder()
                .id(TEST_UUID.toString())
                .userId("user-1")
                .server("server-1")
                .tool("tool-1")
                .allowed(true)
                .policyId("policy-1")
                .createdAt(NOW)
                .build();

        AccessAuditEntity savedEntity = AccessAuditEntity.builder()
                .id(TEST_UUID)
                .userId("user-1")
                .server("server-1")
                .tool("tool-1")
                .allowed(true)
                .policyId("policy-1")
                .createdAt(NOW)
                .build();

        when(auditJpaRepository.save(any(AccessAuditEntity.class))).thenReturn(savedEntity);

        AccessAuditEntry result = adapter.saveAudit(domain);

        ArgumentCaptor<AccessAuditEntity> captor = ArgumentCaptor.forClass(AccessAuditEntity.class);
        verify(auditJpaRepository).save(captor.capture());

        AccessAuditEntity captured = captor.getValue();
        assertThat(captured.getUserId()).isEqualTo("user-1");
        assertThat(captured.getServer()).isEqualTo("server-1");
        assertThat(captured.getTool()).isEqualTo("tool-1");
        assertThat(captured.isAllowed()).isTrue();
        assertThat(captured.getPolicyId()).isEqualTo("policy-1");

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getUserId()).isEqualTo("user-1");
        assertThat(result.isAllowed()).isTrue();
    }

    @Test
    void findAuditByUserId_mapsEntitiesCorrectly() {
        AccessAuditEntity entity = AccessAuditEntity.builder()
                .id(TEST_UUID).userId("user-1").server("s1").tool("t1")
                .allowed(false).policyId(null).createdAt(NOW).build();

        when(auditJpaRepository.findByUserId("user-1")).thenReturn(List.of(entity));

        List<AccessAuditEntry> result = adapter.findAuditByUserId("user-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo("user-1");
        assertThat(result.get(0).isAllowed()).isFalse();
        assertThat(result.get(0).getPolicyId()).isNull();
    }
}
