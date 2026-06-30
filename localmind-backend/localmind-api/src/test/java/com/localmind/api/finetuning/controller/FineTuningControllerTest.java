package com.localmind.api.finetuning.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.api.common.advice.GlobalExceptionHandler;
import com.localmind.api.finetuning.dto.CreateDatasetRequest;
import com.localmind.api.finetuning.dto.StartTrainingRequest;
import com.localmind.domain.finetuning.model.FineTuningJob;
import com.localmind.domain.finetuning.model.FineTuningStatus;
import com.localmind.domain.finetuning.model.TrainingDataset;
import com.localmind.domain.finetuning.model.TrainingTechnique;
import com.localmind.domain.finetuning.port.in.FineTuningUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class FineTuningControllerTest {

    private MockMvc mockMvc;
    private FineTuningUseCase fineTuningUseCase;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        fineTuningUseCase = mock(FineTuningUseCase.class);
        FineTuningController controller = new FineTuningController(fineTuningUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void createDataset_shouldReturn200() throws Exception {
        // given
        TrainingDataset dataset = TrainingDataset.builder()
                .id("ds-1")
                .name("test-dataset")
                .documentIds(List.of("doc-1", "doc-2"))
                .sampleCount(2)
                .format("JSONL")
                .createdAt(Instant.now())
                .build();

        when(fineTuningUseCase.createDataset(eq("test-dataset"), eq(List.of("doc-1", "doc-2"))))
                .thenReturn(dataset);

        CreateDatasetRequest request = new CreateDatasetRequest("test-dataset", List.of("doc-1", "doc-2"));

        // when/then
        mockMvc.perform(post("/api/v1/finetuning/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ds-1"))
                .andExpect(jsonPath("$.name").value("test-dataset"))
                .andExpect(jsonPath("$.sampleCount").value(2))
                .andExpect(jsonPath("$.format").value("JSONL"));

        verify(fineTuningUseCase).createDataset("test-dataset", List.of("doc-1", "doc-2"));
    }

    @Test
    void listDatasets_shouldReturnAll() throws Exception {
        // given
        TrainingDataset ds = TrainingDataset.builder()
                .id("ds-1")
                .name("test")
                .documentIds(List.of("doc-1"))
                .sampleCount(1)
                .format("JSONL")
                .createdAt(Instant.now())
                .build();
        when(fineTuningUseCase.listDatasets()).thenReturn(List.of(ds));

        // when/then
        mockMvc.perform(get("/api/v1/finetuning/datasets")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("ds-1"));
    }

    @Test
    void startTraining_shouldReturn200() throws Exception {
        // given
        FineTuningJob job = FineTuningJob.builder()
                .id("job-1")
                .baseModel("llama3.2")
                .technique(TrainingTechnique.LORA)
                .status(FineTuningStatus.PENDING)
                .progress(0)
                .datasetId("ds-1")
                .hyperparameters(Map.of())
                .createdAt(Instant.now())
                .build();

        when(fineTuningUseCase.startTraining(eq("ds-1"), eq("llama3.2"), eq(TrainingTechnique.LORA), anyMap()))
                .thenReturn(job);

        StartTrainingRequest request = new StartTrainingRequest("ds-1", "llama3.2", "LORA", Map.of());

        // when/then
        mockMvc.perform(post("/api/v1/finetuning/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("job-1"))
                .andExpect(jsonPath("$.baseModel").value("llama3.2"))
                .andExpect(jsonPath("$.technique").value("LORA"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void listJobs_shouldReturnAll() throws Exception {
        // given
        FineTuningJob job = FineTuningJob.builder()
                .id("job-1")
                .baseModel("llama3.2")
                .technique(TrainingTechnique.LORA)
                .status(FineTuningStatus.COMPLETED)
                .progress(100)
                .createdAt(Instant.now())
                .build();
        when(fineTuningUseCase.listJobs()).thenReturn(List.of(job));

        // when/then
        mockMvc.perform(get("/api/v1/finetuning/jobs")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    void getJob_found_shouldReturn200() throws Exception {
        // given
        FineTuningJob job = FineTuningJob.builder()
                .id("job-1")
                .baseModel("llama3.2")
                .technique(TrainingTechnique.QLORA)
                .status(FineTuningStatus.TRAINING)
                .progress(50)
                .createdAt(Instant.now())
                .build();
        when(fineTuningUseCase.getJob("job-1")).thenReturn(Optional.of(job));

        // when/then
        mockMvc.perform(get("/api/v1/finetuning/jobs/job-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("job-1"))
                .andExpect(jsonPath("$.technique").value("QLORA"))
                .andExpect(jsonPath("$.progress").value(50));
    }

    @Test
    void getJob_notFound_shouldReturn404() throws Exception {
        // given
        when(fineTuningUseCase.getJob("nonexistent")).thenReturn(Optional.empty());

        // when/then
        mockMvc.perform(get("/api/v1/finetuning/jobs/nonexistent")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deployModel_shouldReturn200() throws Exception {
        // given
        doNothing().when(fineTuningUseCase).deployModel("job-1");

        // when/then
        mockMvc.perform(post("/api/v1/finetuning/jobs/job-1/deploy"))
                .andExpect(status().isOk());

        verify(fineTuningUseCase).deployModel("job-1");
    }

    @Test
    void createDataset_blankName_shouldReturn400() throws Exception {
        // given
        CreateDatasetRequest request = new CreateDatasetRequest("", List.of("doc-1"));

        // when/then
        mockMvc.perform(post("/api/v1/finetuning/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
