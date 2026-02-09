package com.localmind.batch.folder.job;

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
public class FolderScanJobConfig {

    @Bean
    public Job folderScanJob(JobRepository jobRepository, Step folderScanStep) {
        return new JobBuilder("folderScanJob", jobRepository)
                .start(folderScanStep)
                .build();
    }

    @Bean
    public Step folderScanStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            // Folder scan logic: compare filesystem with DB, queue new files
            return RepeatStatus.FINISHED;
        };

        return new StepBuilder("folderScanStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
