package com.localmind.domain.llm.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.model.LlmProviderConfig;
import com.localmind.domain.llm.model.OllamaStatus;
import com.localmind.domain.llm.model.PullProgress;
import com.localmind.domain.llm.port.in.ProviderConfigUseCase.TestResult;
import com.localmind.domain.llm.port.out.OllamaModelPort;
import com.localmind.domain.llm.port.out.ProviderConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderConfigServiceTest {

    @Mock
    private ProviderConfigRepository repository;

    @Mock
    private OllamaModelPort ollamaModelPort;

    @InjectMocks
    private ProviderConfigService service;

    @Test
    void save_shouldDelegateToRepository() {
        LlmProviderConfig config = LlmProviderConfig.builder()
                .name("Ollama Local")
                .type(LlmProvider.OLLAMA)
                .baseUrl("http://localhost:11434")
                .enabled(true)
                .build();

        LlmProviderConfig savedConfig = LlmProviderConfig.builder()
                .id("config-1")
                .name("Ollama Local")
                .type(LlmProvider.OLLAMA)
                .baseUrl("http://localhost:11434")
                .enabled(true)
                .build();

        when(repository.save(config)).thenReturn(savedConfig);

        LlmProviderConfig result = service.save(config);

        assertThat(result.getId()).isEqualTo("config-1");
        assertThat(result.getName()).isEqualTo("Ollama Local");
        verify(repository).save(config);
    }

    @Test
    void listAll_shouldReturnAll() {
        List<LlmProviderConfig> configs = List.of(
                LlmProviderConfig.builder().id("1").name("Ollama").type(LlmProvider.OLLAMA).build(),
                LlmProviderConfig.builder().id("2").name("OpenAI").type(LlmProvider.OPENAI).build()
        );

        when(repository.findAll()).thenReturn(configs);

        List<LlmProviderConfig> result = service.listAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(LlmProviderConfig::getName)
                .containsExactly("Ollama", "OpenAI");
        verify(repository).findAll();
    }

    @Test
    void getById_shouldReturnConfig() {
        LlmProviderConfig config = LlmProviderConfig.builder()
                .id("config-1")
                .name("Ollama Local")
                .type(LlmProvider.OLLAMA)
                .build();

        when(repository.findById("config-1")).thenReturn(Optional.of(config));

        LlmProviderConfig result = service.getById("config-1");

        assertThat(result.getId()).isEqualTo("config-1");
        assertThat(result.getName()).isEqualTo("Ollama Local");
        verify(repository).findById("config-1");
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(repository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    @Test
    void deleteById_shouldDeleteWhenExists() {
        LlmProviderConfig config = LlmProviderConfig.builder()
                .id("config-1")
                .name("Ollama")
                .build();

        when(repository.findById("config-1")).thenReturn(Optional.of(config));

        service.deleteById("config-1");

        verify(repository).findById("config-1");
        verify(repository).deleteById("config-1");
    }

    @Test
    void deleteById_shouldThrowWhenNotFound() {
        when(repository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteById("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("nonexistent");

        verify(repository, never()).deleteById(any());
    }

    @Test
    void testConnection_ollamaOk() {
        LlmProviderConfig config = LlmProviderConfig.builder()
                .id("ollama-1")
                .type(LlmProvider.OLLAMA)
                .baseUrl("http://localhost:11434")
                .build();

        when(repository.findById("ollama-1")).thenReturn(Optional.of(config));
        when(ollamaModelPort.listAvailableModels("http://localhost:11434"))
                .thenReturn(List.of("llama3", "mistral", "codellama"));

        TestResult result = service.testConnection("ollama-1");

        assertThat(result.status()).isEqualTo("OK");
        assertThat(result.message()).contains("3");
    }

    @Test
    void testConnection_ollamaFails() {
        LlmProviderConfig config = LlmProviderConfig.builder()
                .id("ollama-1")
                .type(LlmProvider.OLLAMA)
                .baseUrl("http://localhost:11434")
                .build();

        when(repository.findById("ollama-1")).thenReturn(Optional.of(config));
        when(ollamaModelPort.listAvailableModels("http://localhost:11434"))
                .thenThrow(new RuntimeException("Connection refused"));

        TestResult result = service.testConnection("ollama-1");

        assertThat(result.status()).isEqualTo("ERROR");
        assertThat(result.message()).contains("Connection refused");
    }

    @Test
    void testConnection_openaiWithApiKey() {
        LlmProviderConfig config = LlmProviderConfig.builder()
                .id("openai-1")
                .type(LlmProvider.OPENAI)
                .apiKey("sk-test-key-123")
                .build();

        when(repository.findById("openai-1")).thenReturn(Optional.of(config));

        TestResult result = service.testConnection("openai-1");

        assertThat(result.status()).isEqualTo("OK");
    }

    @Test
    void testConnection_openaiWithoutApiKey() {
        LlmProviderConfig config = LlmProviderConfig.builder()
                .id("openai-1")
                .type(LlmProvider.OPENAI)
                .apiKey(null)
                .build();

        when(repository.findById("openai-1")).thenReturn(Optional.of(config));

        TestResult result = service.testConnection("openai-1");

        assertThat(result.status()).isEqualTo("ERROR");
    }

    @Test
    void listOllamaModels_shouldDelegate() {
        List<String> models = List.of("llama3", "mistral");
        when(ollamaModelPort.listAvailableModels("http://localhost:11434")).thenReturn(models);

        List<String> result = service.listOllamaModels("http://localhost:11434");

        assertThat(result).containsExactly("llama3", "mistral");
        verify(ollamaModelPort).listAvailableModels("http://localhost:11434");
    }

    @Test
    void checkOllamaStatus_shouldDelegateToPort() {
        OllamaStatus expectedStatus = OllamaStatus.builder()
                .online(true)
                .version("0.1.30")
                .models(List.of(
                        OllamaStatus.OllamaModelInfo.builder()
                                .name("llama3")
                                .sizeBytes(4_000_000_000L)
                                .modifiedAt("2024-01-15T10:30:00Z")
                                .build()
                ))
                .build();

        when(ollamaModelPort.checkStatus("http://localhost:11434")).thenReturn(expectedStatus);

        OllamaStatus result = service.checkOllamaStatus("http://localhost:11434");

        assertThat(result.isOnline()).isTrue();
        assertThat(result.getVersion()).isEqualTo("0.1.30");
        assertThat(result.getModels()).hasSize(1);
        assertThat(result.getModels().get(0).getName()).isEqualTo("llama3");
        verify(ollamaModelPort).checkStatus("http://localhost:11434");
    }

    @Test
    void startOllama_shouldDelegateToPort() {
        doNothing().when(ollamaModelPort).startOllama();

        service.startOllama();

        verify(ollamaModelPort).startOllama();
    }

    @SuppressWarnings("unchecked")
    @Test
    void pullOllamaModelStreaming_shouldDelegateToPort() {
        Consumer<PullProgress> callback = mock(Consumer.class);
        doNothing().when(ollamaModelPort).pullModelStreaming(
                eq("http://localhost:11434"), eq("llama3"), any(Consumer.class));

        service.pullOllamaModelStreaming("http://localhost:11434", "llama3", callback);

        verify(ollamaModelPort).pullModelStreaming("http://localhost:11434", "llama3", callback);
    }

    @Test
    void updateDefaultModel_shouldSaveWithNewModel() {
        LlmProviderConfig config = LlmProviderConfig.builder()
                .id("ollama-1")
                .name("Ollama")
                .type(LlmProvider.OLLAMA)
                .defaultModel("llama2")
                .build();

        LlmProviderConfig updatedConfig = LlmProviderConfig.builder()
                .id("ollama-1")
                .name("Ollama")
                .type(LlmProvider.OLLAMA)
                .defaultModel("llama3:latest")
                .build();

        when(repository.findById("ollama-1")).thenReturn(Optional.of(config));
        when(repository.save(any(LlmProviderConfig.class))).thenReturn(updatedConfig);

        LlmProviderConfig result = service.updateDefaultModel("ollama-1", "llama3:latest");

        assertThat(result.getDefaultModel()).isEqualTo("llama3:latest");
        verify(repository).findById("ollama-1");
        verify(repository).save(config);
        assertThat(config.getDefaultModel()).isEqualTo("llama3:latest");
    }
}
