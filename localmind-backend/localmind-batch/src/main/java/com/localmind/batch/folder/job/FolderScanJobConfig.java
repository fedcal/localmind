package com.localmind.batch.folder.job;

import com.localmind.domain.document.model.FolderConfig;
import com.localmind.domain.document.port.in.DocumentIngestionPipelineUseCase;
import com.localmind.domain.document.port.out.FolderConfigRepository;
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
public class FolderScanJobConfig {

    private final FolderConfigRepository folderConfigRepository;
    private final DocumentIngestionPipelineUseCase ingestionPipeline;

    public FolderScanJobConfig(FolderConfigRepository folderConfigRepository,
                               DocumentIngestionPipelineUseCase ingestionPipeline) {
        this.folderConfigRepository = folderConfigRepository;
        this.ingestionPipeline = ingestionPipeline;
    }

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
            List<FolderConfig> watchedFolders = folderConfigRepository.findByWatchEnabled(true);
            for (FolderConfig folder : watchedFolders) {
                try {
                    ingestionPipeline.ingestFromFolder(folder.getId());
                } catch (Exception e) {
                    System.err.println("Folder scan failed for: " + folder.getPath() + " - " + e.getMessage());
                }
            }
            return RepeatStatus.FINISHED;
        };

        return new StepBuilder("folderScanStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
