package com.localmind.infrastructure.llm.adapter;

import com.localmind.domain.llm.model.OllamaStatus;
import com.localmind.domain.llm.model.PullProgress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OllamaModelAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    private OllamaModelAdapter adapter;

    private static final String BASE_URL = "http://localhost:11434";

    @BeforeEach
    void setUp() {
        adapter = new OllamaModelAdapter();
        ReflectionTestUtils.setField(adapter, "restTemplate", restTemplate);
    }

    @Test
    void listAvailableModels_shouldCallRestTemplate() {
        // given
        String expectedUrl = BASE_URL + "/api/tags";
        // We cannot easily create the private inner class OllamaTagsResponse,
        // so we mock the call to return null and verify the URL is called correctly.
        when(restTemplate.getForObject(eq(expectedUrl), any(Class.class))).thenReturn(null);

        // when
        List<String> result = adapter.listAvailableModels(BASE_URL);

        // then
        verify(restTemplate).getForObject(eq(expectedUrl), any(Class.class));
        assertThat(result).isEmpty();
    }

    @Test
    void listAvailableModels_shouldThrowOnError() {
        // given
        String expectedUrl = BASE_URL + "/api/tags";
        when(restTemplate.getForObject(eq(expectedUrl), any(Class.class)))
                .thenThrow(new RestClientException("Connection refused"));

        // when / then
        assertThatThrownBy(() -> adapter.listAvailableModels(BASE_URL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unable to contact Ollama")
                .hasMessageContaining(BASE_URL);
    }

    @Test
    void pullModel_shouldDelegateToStreamingWithExecute() {
        // given
        String expectedUrl = BASE_URL + "/api/pull";
        when(restTemplate.execute(eq(expectedUrl), eq(HttpMethod.POST),
                any(RequestCallback.class), any(ResponseExtractor.class)))
                .thenReturn(null);

        // when
        adapter.pullModel(BASE_URL, "llama3");

        // then
        verify(restTemplate).execute(eq(expectedUrl), eq(HttpMethod.POST),
                any(RequestCallback.class), any(ResponseExtractor.class));
    }

    @Test
    void pullModelStreaming_shouldCallCallbackOnError() {
        // given
        String expectedUrl = BASE_URL + "/api/pull";
        when(restTemplate.execute(eq(expectedUrl), eq(HttpMethod.POST),
                any(RequestCallback.class), any(ResponseExtractor.class)))
                .thenThrow(new RestClientException("Connection refused"));

        List<PullProgress> received = new ArrayList<>();

        // when
        adapter.pullModelStreaming(BASE_URL, "llama3", received::add);

        // then
        assertThat(received).hasSize(1);
        assertThat(received.get(0).isError()).isTrue();
        assertThat(received.get(0).isDone()).isTrue();
        assertThat(received.get(0).getErrorMessage()).contains("Connection refused");
    }

    @Test
    void checkStatus_shouldReturnOnlineWithVersionAndModels() {
        // given - mock /api/version and /api/tags
        // We cannot create private inner class instances, so we mock with null.
        // The method handles null gracefully: version="unknown", models=empty.
        String versionUrl = BASE_URL + "/api/version";
        String tagsUrl = BASE_URL + "/api/tags";
        when(restTemplate.getForObject(eq(versionUrl), any(Class.class))).thenReturn(null);
        when(restTemplate.getForObject(eq(tagsUrl), any(Class.class))).thenReturn(null);

        // when
        OllamaStatus result = adapter.checkStatus(BASE_URL);

        // then
        assertThat(result.isOnline()).isTrue();
        assertThat(result.getVersion()).isEqualTo("unknown");
        assertThat(result.getModels()).isEmpty();
        assertThat(result.getErrorMessage()).isNull();
        verify(restTemplate).getForObject(eq(versionUrl), any(Class.class));
        verify(restTemplate).getForObject(eq(tagsUrl), any(Class.class));
    }

    @Test
    void checkStatus_shouldReturnOfflineOnConnectionError() {
        // given
        String versionUrl = BASE_URL + "/api/version";
        when(restTemplate.getForObject(eq(versionUrl), any(Class.class)))
                .thenThrow(new RestClientException("Connection refused"));

        // when
        OllamaStatus result = adapter.checkStatus(BASE_URL);

        // then
        assertThat(result.isOnline()).isFalse();
        assertThat(result.getErrorMessage()).contains("Connection refused");
        assertThat(result.getModels()).isEmpty();
    }

    @Test
    void deleteModel_shouldCallRestTemplate() {
        // given
        String expectedUrl = BASE_URL + "/api/delete";
        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        // when
        adapter.deleteModel(BASE_URL, "llama3");

        // then
        verify(restTemplate).exchange(eq(expectedUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(String.class));
    }
}
