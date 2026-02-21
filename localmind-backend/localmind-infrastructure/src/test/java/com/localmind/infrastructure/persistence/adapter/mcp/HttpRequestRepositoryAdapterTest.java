package com.localmind.infrastructure.persistence.adapter.mcp;

import com.localmind.domain.mcp.model.HttpRequestResult;
import com.localmind.infrastructure.persistence.entity.mcp.HttpRequestEntity;
import com.localmind.infrastructure.persistence.repository.mcp.JpaHttpRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpRequestRepositoryAdapterTest {

    @Mock
    private JpaHttpRequestRepository jpaRepository;

    private HttpRequestRepositoryAdapter adapter;

    private final UUID TEST_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private final Instant NOW = Instant.parse("2026-02-12T10:00:00Z");

    @BeforeEach
    void setUp() {
        adapter = new HttpRequestRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_mapsCorrectly() {
        HttpRequestResult domain = HttpRequestResult.builder()
                .id(TEST_UUID.toString())
                .url("https://api.example.com/data")
                .method("GET")
                .statusCode(200)
                .responseTimeMs(150)
                .responseBody("{\"message\":\"ok\"}")
                .createdAt(NOW)
                .build();

        HttpRequestEntity savedEntity = HttpRequestEntity.builder()
                .id(TEST_UUID)
                .url("https://api.example.com/data")
                .method("GET")
                .statusCode(200)
                .responseTimeMs(150)
                .responseBody("{\"message\":\"ok\"}")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(HttpRequestEntity.class))).thenReturn(savedEntity);

        HttpRequestResult result = adapter.save(domain);

        ArgumentCaptor<HttpRequestEntity> captor = ArgumentCaptor.forClass(HttpRequestEntity.class);
        verify(jpaRepository).save(captor.capture());

        HttpRequestEntity captured = captor.getValue();
        assertThat(captured.getId()).isEqualTo(TEST_UUID);
        assertThat(captured.getUrl()).isEqualTo("https://api.example.com/data");
        assertThat(captured.getMethod()).isEqualTo("GET");
        assertThat(captured.getStatusCode()).isEqualTo(200);
        assertThat(captured.getResponseTimeMs()).isEqualTo(150);
        assertThat(captured.getResponseBody()).isEqualTo("{\"message\":\"ok\"}");
        assertThat(captured.getCreatedAt()).isEqualTo(NOW);

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getUrl()).isEqualTo("https://api.example.com/data");
        assertThat(result.getMethod()).isEqualTo("GET");
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getResponseTimeMs()).isEqualTo(150);
        assertThat(result.getResponseBody()).isEqualTo("{\"message\":\"ok\"}");
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void save_withNullId_doesNotSetIdOnEntity() {
        HttpRequestResult domain = HttpRequestResult.builder()
                .id(null)
                .url("https://api.example.com/test")
                .method("POST")
                .statusCode(201)
                .responseTimeMs(250)
                .responseBody("{\"id\":1}")
                .createdAt(NOW)
                .build();

        HttpRequestEntity savedEntity = HttpRequestEntity.builder()
                .id(TEST_UUID)
                .url("https://api.example.com/test")
                .method("POST")
                .statusCode(201)
                .responseTimeMs(250)
                .responseBody("{\"id\":1}")
                .createdAt(NOW)
                .build();

        when(jpaRepository.save(any(HttpRequestEntity.class))).thenReturn(savedEntity);

        HttpRequestResult result = adapter.save(domain);

        ArgumentCaptor<HttpRequestEntity> captor = ArgumentCaptor.forClass(HttpRequestEntity.class);
        verify(jpaRepository).save(captor.capture());

        HttpRequestEntity captured = captor.getValue();
        assertThat(captured.getId()).isNull();

        assertThat(result.getId()).isEqualTo(TEST_UUID.toString());
        assertThat(result.getMethod()).isEqualTo("POST");
        assertThat(result.getStatusCode()).isEqualTo(201);
    }

    @Test
    void findAll_returnsAll() {
        UUID uuid1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID uuid2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

        HttpRequestEntity entity1 = HttpRequestEntity.builder()
                .id(uuid1)
                .url("https://api.example.com/first")
                .method("GET")
                .statusCode(200)
                .responseTimeMs(100)
                .responseBody("response 1")
                .createdAt(NOW)
                .build();

        HttpRequestEntity entity2 = HttpRequestEntity.builder()
                .id(uuid2)
                .url("https://api.example.com/second")
                .method("POST")
                .statusCode(201)
                .responseTimeMs(200)
                .responseBody("response 2")
                .createdAt(NOW)
                .build();

        when(jpaRepository.findAll()).thenReturn(List.of(entity1, entity2));

        List<HttpRequestResult> results = adapter.findAll();

        assertThat(results).hasSize(2);

        assertThat(results.get(0).getId()).isEqualTo(uuid1.toString());
        assertThat(results.get(0).getUrl()).isEqualTo("https://api.example.com/first");
        assertThat(results.get(0).getMethod()).isEqualTo("GET");
        assertThat(results.get(0).getStatusCode()).isEqualTo(200);
        assertThat(results.get(0).getResponseTimeMs()).isEqualTo(100);
        assertThat(results.get(0).getResponseBody()).isEqualTo("response 1");

        assertThat(results.get(1).getId()).isEqualTo(uuid2.toString());
        assertThat(results.get(1).getUrl()).isEqualTo("https://api.example.com/second");
        assertThat(results.get(1).getMethod()).isEqualTo("POST");
        assertThat(results.get(1).getStatusCode()).isEqualTo(201);
        assertThat(results.get(1).getResponseTimeMs()).isEqualTo(200);
        assertThat(results.get(1).getResponseBody()).isEqualTo("response 2");

        verify(jpaRepository).findAll();
    }

    @Test
    void findAll_emptyList_returnsEmptyList() {
        when(jpaRepository.findAll()).thenReturn(List.of());

        List<HttpRequestResult> results = adapter.findAll();

        assertThat(results).isEmpty();
        verify(jpaRepository).findAll();
    }
}
