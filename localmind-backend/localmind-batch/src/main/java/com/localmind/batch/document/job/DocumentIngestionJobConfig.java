package com.localmind.batch.document.job;

import com.localmind.batch.document.listener.DocumentJobListener;
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

@Configuration
public class DocumentIngestionJobConfig {

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
            // Text extraction logic will be implemented with Tika
            return RepeatStatus.FINISHED;
        };

        return new StepBuilder("textExtractionStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
