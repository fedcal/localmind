package com.localmind.api.finetuning.controller;

import com.localmind.api.finetuning.dto.*;
import com.localmind.domain.finetuning.model.FineTuningJob;
import com.localmind.domain.finetuning.model.TrainingDataset;
import com.localmind.domain.finetuning.model.TrainingTechnique;
import com.localmind.domain.finetuning.port.in.FineTuningUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/finetuning")
@Tag(name = "Fine-Tuning", description = "Fine-tuning operations / Operazioni di fine-tuning")
@ConditionalOnProperty(name = "localmind.finetuning.enabled", havingValue = "true")
public class FineTuningController {

    private final FineTuningUseCase fineTuningUseCase;

    public FineTuningController(FineTuningUseCase fineTuningUseCase) {
        this.fineTuningUseCase = fineTuningUseCase;
    }

    @PostMapping("/datasets")
    @Operation(summary = "Crea dataset di addestramento / Create training dataset")
    @ApiResponse(responseCode = "200", description = "Dataset creato / Dataset created")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<TrainingDatasetDto> createDataset(@Valid @RequestBody CreateDatasetRequest request) {
        TrainingDataset dataset = fineTuningUseCase.createDataset(request.getName(), request.getDocumentIds());
        return ResponseEntity.ok(toDatasetDto(dataset));
    }

    @GetMapping("/datasets")
    @Operation(summary = "Lista dataset / List datasets")
    @ApiResponse(responseCode = "200", description = "Lista dataset / Dataset list")
    public ResponseEntity<List<TrainingDatasetDto>> listDatasets() {
        List<TrainingDatasetDto> datasets = fineTuningUseCase.listDatasets().stream()
                .map(this::toDatasetDto)
                .toList();
        return ResponseEntity.ok(datasets);
    }

    @PostMapping("/jobs")
    @Operation(summary = "Avvia addestramento / Start training job")
    @ApiResponse(responseCode = "200", description = "Job avviato / Job started")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida / Bad request")
    public ResponseEntity<FineTuningJobDto> startTraining(@Valid @RequestBody StartTrainingRequest request) {
        TrainingTechnique technique = TrainingTechnique.valueOf(request.getTechnique());
        Map<String, String> hyperparameters = request.getHyperparameters() != null
                ? request.getHyperparameters()
                : Map.of();

        FineTuningJob job = fineTuningUseCase.startTraining(
                request.getDatasetId(),
                request.getBaseModel(),
                technique,
                hyperparameters
        );
        return ResponseEntity.ok(toJobDto(job));
    }

    @GetMapping("/jobs")
    @Operation(summary = "Lista job di addestramento / List training jobs")
    @ApiResponse(responseCode = "200", description = "Lista job / Job list")
    public ResponseEntity<List<FineTuningJobDto>> listJobs() {
        List<FineTuningJobDto> jobs = fineTuningUseCase.listJobs().stream()
                .map(this::toJobDto)
                .toList();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/jobs/{jobId}")
    @Operation(summary = "Dettaglio job di addestramento / Get training job detail")
    @ApiResponse(responseCode = "200", description = "Job trovato / Job found")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<FineTuningJobDto> getJob(@PathVariable String jobId) {
        return fineTuningUseCase.getJob(jobId)
                .map(this::toJobDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/jobs/{jobId}/deploy")
    @Operation(summary = "Distribuisci modello addestrato / Deploy trained model")
    @ApiResponse(responseCode = "200", description = "Modello distribuito / Model deployed")
    @ApiResponse(responseCode = "404", description = "Non trovato / Not found")
    public ResponseEntity<Void> deployModel(@PathVariable String jobId) {
        fineTuningUseCase.deployModel(jobId);
        return ResponseEntity.ok().build();
    }

    private FineTuningJobDto toJobDto(FineTuningJob job) {
        return FineTuningJobDto.builder()
                .id(job.getId())
                .baseModel(job.getBaseModel())
                .technique(job.getTechnique().name())
                .status(job.getStatus().name())
                .progress(job.getProgress())
                .datasetId(job.getDatasetId())
                .outputModelName(job.getOutputModelName())
                .hyperparameters(job.getHyperparameters())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .error(job.getError())
                .createdAt(job.getCreatedAt())
                .build();
    }

    private TrainingDatasetDto toDatasetDto(TrainingDataset dataset) {
        return TrainingDatasetDto.builder()
                .id(dataset.getId())
                .name(dataset.getName())
                .documentIds(dataset.getDocumentIds())
                .sampleCount(dataset.getSampleCount())
                .format(dataset.getFormat())
                .createdAt(dataset.getCreatedAt())
                .build();
    }
}
