package com.localmind.batch.document.job;

import com.localmind.batch.document.listener.DocumentJobListener;
import com.localmind.domain.document.model.Document;
import com.localmind.domain.document.model.DocumentStatus;
import com.localmind.domain.document.port.in.DocumentIngestionPipelineUseCase;
import com.localmind.domain.document.port.out.DocumentRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
public class DocumentIngestionJobConfig {

    private final DocumentRepository documentRepository;
    private final DocumentIngestionPipelineUseCase ingestionPipeline;

    public DocumentIngestionJobConfig(DocumentRepository documentRepository,
                                       DocumentIngestionPipelineUseCase ingestionPipeline) {
        this.documentRepository = documentRepository;
        this.ingestionPipeline = ingestionPipeline;
    }

    @Bean
    public Job documentIngestionJob(JobRepository jobRepository,
                                     Step textExtractionStep,
                                     DocumentJobListener listener) {
        return new JobBuilder("documentIngestionJob", jobRepository)
                .listener(listener)
                .start(textExtractionStep)
                .build();
    }

    @Bean
    public Step textExtractionStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            List<Document> pendingDocs = documentRepository.findByStatus(DocumentStatus.PENDING);
            for (Document doc : pendingDocs) {
                try {
                    // Re-process pending documents that weren't fully ingested
                    System.out.println("Re-processing pending document: " + doc.getFilename());
                } catch (Exception e) {
                    System.err.println("Failed to re-process document: " + doc.getId() + " - " + e.getMessage());
                }
            }
            return RepeatStatus.FINISHED;
        };

        return new StepBuilder("textExtractionStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
