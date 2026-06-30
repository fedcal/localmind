package com.localmind.batch.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job folderScanJob;

    @InjectMocks
    private BatchScheduler batchScheduler;

    @Test
    void scheduleFolderScan_shouldLaunchJob() throws Exception {
        // given
        when(jobLauncher.run(eq(folderScanJob), any(JobParameters.class))).thenReturn(null);

        // when
        batchScheduler.scheduleFolderScan();

        // then
        verify(jobLauncher).run(eq(folderScanJob), any(JobParameters.class));
    }

    @Test
    void scheduleFolderScan_shouldHandleException() throws Exception {
        // given
        when(jobLauncher.run(eq(folderScanJob), any(JobParameters.class)))
                .thenThrow(new RuntimeException("Job execution failed"));

        // when / then - no exception should propagate
        assertThatCode(() -> batchScheduler.scheduleFolderScan())
                .doesNotThrowAnyException();
    }
}
