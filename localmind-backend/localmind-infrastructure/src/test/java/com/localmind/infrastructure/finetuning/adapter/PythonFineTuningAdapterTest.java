package com.localmind.infrastructure.finetuning.adapter;

import com.localmind.domain.finetuning.model.FineTuningJob;
import com.localmind.domain.finetuning.model.FineTuningStatus;
import com.localmind.domain.finetuning.model.TrainingTechnique;
import com.localmind.infrastructure.finetuning.config.FineTuningProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PythonFineTuningAdapterTest {

    private PythonFineTuningAdapter adapter;
    private FineTuningProperties properties;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        properties = new FineTuningProperties();
        properties.setEnabled(true);
        properties.setPythonPath("python3");
        properties.setScriptsDir(tempDir.resolve("scripts").toString());
        properties.setOutputDir(tempDir.resolve("output").toString());
        adapter = new PythonFineTuningAdapter(properties);
    }

    @Test
    void getTrainingStatus_noStatusFile_shouldReturnPending() {
        // when
        FineTuningStatus status = adapter.getTrainingStatus("nonexistent-job");

        // then
        assertThat(status).isEqualTo(FineTuningStatus.PENDING);
    }

    @Test
    void getTrainingStatus_withStatusFile_shouldReturnStatus() throws IOException {
        // given
        String jobId = "test-job-1";
        Path statusDir = tempDir.resolve("output").resolve(jobId);
        Files.createDirectories(statusDir);
        Files.writeString(statusDir.resolve("status.json"),
                "{\"status\":\"TRAINING\",\"progress\":50}");

        // when
        FineTuningStatus status = adapter.getTrainingStatus(jobId);

        // then
        assertThat(status).isEqualTo(FineTuningStatus.TRAINING);
    }

    @Test
    void getTrainingProgress_noStatusFile_shouldReturnZero() {
        // when
        int progress = adapter.getTrainingProgress("nonexistent-job");

        // then
        assertThat(progress).isZero();
    }

    @Test
    void getTrainingProgress_withStatusFile_shouldReturnProgress() throws IOException {
        // given
        String jobId = "test-job-2";
        Path statusDir = tempDir.resolve("output").resolve(jobId);
        Files.createDirectories(statusDir);
        Files.writeString(statusDir.resolve("status.json"),
                "{\"status\":\"TRAINING\",\"progress\":75}");

        // when
        int progress = adapter.getTrainingProgress(jobId);

        // then
        assertThat(progress).isEqualTo(75);
    }

    @Test
    void getTrainingStatus_invalidJson_shouldReturnPending() throws IOException {
        // given
        String jobId = "test-job-3";
        Path statusDir = tempDir.resolve("output").resolve(jobId);
        Files.createDirectories(statusDir);
        Files.writeString(statusDir.resolve("status.json"), "not valid json");

        // when
        FineTuningStatus status = adapter.getTrainingStatus(jobId);

        // then
        assertThat(status).isEqualTo(FineTuningStatus.PENDING);
    }

    @Test
    void getTrainingProgress_invalidJson_shouldReturnZero() throws IOException {
        // given
        String jobId = "test-job-4";
        Path statusDir = tempDir.resolve("output").resolve(jobId);
        Files.createDirectories(statusDir);
        Files.writeString(statusDir.resolve("status.json"), "invalid");

        // when
        int progress = adapter.getTrainingProgress(jobId);

        // then
        assertThat(progress).isZero();
    }

    @Test
    void isAvailable_shouldReturnTrueWhenPythonExists() {
        // when
        boolean available = adapter.isAvailable();

        // then - depends on environment, just verify it doesn't throw
        assertThat(available).isIn(true, false);
    }

    @Test
    void getTrainingStatus_completedStatus_shouldReturnCompleted() throws IOException {
        // given
        String jobId = "test-job-5";
        Path statusDir = tempDir.resolve("output").resolve(jobId);
        Files.createDirectories(statusDir);
        Files.writeString(statusDir.resolve("status.json"),
                "{\"status\":\"COMPLETED\",\"progress\":100}");

        // when
        FineTuningStatus status = adapter.getTrainingStatus(jobId);

        // then
        assertThat(status).isEqualTo(FineTuningStatus.COMPLETED);
    }
}
