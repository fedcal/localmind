package com.localmind.domain.llm.service;

import com.localmind.domain.common.exception.ResourceNotFoundException;
import com.localmind.domain.llm.model.LlmModel;
import com.localmind.domain.llm.model.LlmProvider;
import com.localmind.domain.llm.port.out.LlmClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModelManagementServiceTest {

    @Mock
    private LlmClient ollamaClient;

    @Mock
    private LlmClient openaiClient;

    private ModelManagementService modelManagementService;

    @BeforeEach
    void setUp() {
        when(ollamaClient.getProvider()).thenReturn(LlmProvider.OLLAMA);
        when(openaiClient.getProvider()).thenReturn(LlmProvider.OPENAI);

        modelManagementService = new ModelManagementService(List.of(ollamaClient, openaiClient));
    }

    @Test
    void listModels_shouldReturnAllProviders() {
        when(ollamaClient.isAvailable()).thenReturn(true);
        when(openaiClient.isAvailable()).thenReturn(false);

        List<LlmModel> models = modelManagementService.listModels();

        assertThat(models).hasSize(2);
        assertThat(models)
                .extracting(LlmModel::getProvider)
                .containsExactlyInAnyOrder(LlmProvider.OLLAMA, LlmProvider.OPENAI);
    }

    @Test
    void listModelsByProvider_shouldReturnMatchingProvider() {
        when(ollamaClient.isAvailable()).thenReturn(true);

        List<LlmModel> models = modelManagementService.listModelsByProvider(LlmProvider.OLLAMA);

        assertThat(models).hasSize(1);
        LlmModel model = models.get(0);
        assertThat(model.getProvider()).isEqualTo(LlmProvider.OLLAMA);
        assertThat(model.getId()).isEqualTo("ollama");
        assertThat(model.getName()).isEqualTo("Ollama");
        assertThat(model.isAvailable()).isTrue();
    }

    @Test
    void listModelsByProvider_shouldReturnEmptyForUnknown() {
        List<LlmModel> models = modelManagementService.listModelsByProvider(LlmProvider.GOOGLE);

        assertThat(models).isEmpty();
    }

    @Test
    void getModel_shouldReturnModel() {
        when(ollamaClient.isAvailable()).thenReturn(true);

        LlmModel model = modelManagementService.getModel("ollama");

        assertThat(model).isNotNull();
        assertThat(model.getId()).isEqualTo("ollama");
        assertThat(model.getProvider()).isEqualTo(LlmProvider.OLLAMA);
        assertThat(model.isAvailable()).isTrue();
    }

    @Test
    void getModel_shouldThrowForUnknownId() {
        when(ollamaClient.isAvailable()).thenReturn(true);
        when(openaiClient.isAvailable()).thenReturn(true);

        assertThatThrownBy(() -> modelManagementService.getModel("unknown-model"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("unknown-model");
    }
}
