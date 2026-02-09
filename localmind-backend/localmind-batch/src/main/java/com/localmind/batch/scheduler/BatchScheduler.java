package com.localmind.batch.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job folderScanJob;

    public BatchScheduler(JobLauncher jobLauncher,
                          @Qualifier("folderScanJob") Job folderScanJob) {
        this.jobLauncher = jobLauncher;
        this.folderScanJob = folderScanJob;
    }

    @Scheduled(cron = "${localmind.batch.cron-folder-scan:0 */15 * * * *}")
    public void scheduleFolderScan() {
        try {
            jobLauncher.run(folderScanJob, new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters());
            log.info("Folder scan job triggered");
        } catch (Exception e) {
            log.error("Failed to trigger folder scan job: {}", e.getMessage());
        }
    }
}
