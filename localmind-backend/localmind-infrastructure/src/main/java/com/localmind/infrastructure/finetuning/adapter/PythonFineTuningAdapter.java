package com.localmind.infrastructure.finetuning.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.localmind.domain.finetuning.model.FineTuningJob;
import com.localmind.domain.finetuning.model.FineTuningStatus;
import com.localmind.domain.finetuning.port.out.FineTuningPort;
import com.localmind.infrastructure.finetuning.config.FineTuningProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "localmind.finetuning.enabled", havingValue = "true")
@EnableConfigurationProperties(FineTuningProperties.class)
public class PythonFineTuningAdapter implements FineTuningPort {

    private static final Logger log = LoggerFactory.getLogger(PythonFineTuningAdapter.class);

    private final FineTuningProperties properties;
    private final ObjectMapper objectMapper;
    private final ExecutorService executor;

    public PythonFineTuningAdapter(FineTuningProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.executor = Executors.newCachedThreadPool();
    }

    @Override
    public String prepareDataset(String datasetId, List<String> documentIds) {
        try {
            String docIdsStr = String.join(",", documentIds);
            ProcessBuilder pb = new ProcessBuilder(
                    properties.getPythonPath(),
                    Paths.get(properties.getScriptsDir(), "prepare_dataset.py").toString(),
                    "--dataset-id", datasetId,
                    "--doc-ids", docIdsStr,
                    "--output-dir", properties.getOutputDir()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Dataset preparation failed with exit code " + exitCode + ": " + output);
            }

            JsonNode result = objectMapper.readTree(output);
            return result.has("path") ? result.get("path").asText() : "";
        } catch (Exception e) {
            throw new RuntimeException("Failed to prepare dataset: " + e.getMessage(), e);
        }
    }

    @Override
    public void startTraining(FineTuningJob job) {
        executor.submit(() -> {
            try {
                String datasetPath = Paths.get(properties.getOutputDir(), job.getDatasetId(), "dataset.jsonl").toString();
                ProcessBuilder pb = new ProcessBuilder(
                        properties.getPythonPath(),
                        Paths.get(properties.getScriptsDir(), "finetune.py").toString(),
                        "--job-id", job.getId(),
                        "--base-model", job.getBaseModel(),
                        "--technique", job.getTechnique().name(),
                        "--dataset-path", datasetPath,
                        "--output-dir", properties.getOutputDir()
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String output = reader.lines().collect(Collectors.joining("\n"));
                    log.info("Training output for job {}: {}", job.getId(), output);
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    log.error("Training failed for job {} with exit code {}", job.getId(), exitCode);
                }
            } catch (Exception e) {
                log.error("Error starting training for job {}: {}", job.getId(), e.getMessage(), e);
            }
        });
    }

    @Override
    public FineTuningStatus getTrainingStatus(String jobId) {
        try {
            Path statusFile = Paths.get(properties.getOutputDir(), jobId, "status.json");
            if (!Files.exists(statusFile)) {
                return FineTuningStatus.PENDING;
            }
            String content = Files.readString(statusFile);
            JsonNode node = objectMapper.readTree(content);
            String status = node.get("status").asText();
            return FineTuningStatus.valueOf(status);
        } catch (Exception e) {
            log.error("Error reading training status for job {}: {}", jobId, e.getMessage());
            return FineTuningStatus.PENDING;
        }
    }

    @Override
    public int getTrainingProgress(String jobId) {
        try {
            Path statusFile = Paths.get(properties.getOutputDir(), jobId, "status.json");
            if (!Files.exists(statusFile)) {
                return 0;
            }
            String content = Files.readString(statusFile);
            JsonNode node = objectMapper.readTree(content);
            return node.has("progress") ? node.get("progress").asInt() : 0;
        } catch (Exception e) {
            log.error("Error reading training progress for job {}: {}", jobId, e.getMessage());
            return 0;
        }
    }

    @Override
    public void deployModel(String jobId, String modelName) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    properties.getPythonPath(),
                    Paths.get(properties.getScriptsDir(), "deploy_ollama.py").toString(),
                    "--job-id", jobId,
                    "--model-name", modelName,
                    "--output-dir", properties.getOutputDir()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Model deployment failed with exit code " + exitCode + ": " + output);
            }

            log.info("Model deployed successfully: {}", modelName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deploy model: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder(properties.getPythonPath(), "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
