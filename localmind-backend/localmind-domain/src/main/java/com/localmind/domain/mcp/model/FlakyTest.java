package com.localmind.domain.mcp.model;

import java.time.Instant;

/**
 * Domain model representing a flaky test detection result.
 * Used by the cicd-monitor tool group (detectFlakyTests).
 */
public class FlakyTest {

    private final String id;
    private final String repo;
    private final String testName;
    private final String workflow;
    private final int passCount;
    private final int failCount;
    private final double flakinessRate;
    private final Instant detectedAt;

    private FlakyTest(Builder builder) {
        this.id = builder.id;
        this.repo = builder.repo;
        this.testName = builder.testName;
        this.workflow = builder.workflow;
        this.passCount = builder.passCount;
        this.failCount = builder.failCount;
        this.flakinessRate = builder.flakinessRate;
        this.detectedAt = builder.detectedAt;
    }

    public String getId() {
        return id;
    }

    public String getRepo() {
        return repo;
    }

    public String getTestName() {
        return testName;
    }

    public String getWorkflow() {
        return workflow;
    }

    public int getPassCount() {
        return passCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public double getFlakinessRate() {
        return flakinessRate;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String repo;
        private String testName;
        private String workflow;
        private int passCount;
        private int failCount;
        private double flakinessRate;
        private Instant detectedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder repo(String repo) {
            this.repo = repo;
            return this;
        }

        public Builder testName(String testName) {
            this.testName = testName;
            return this;
        }

        public Builder workflow(String workflow) {
            this.workflow = workflow;
            return this;
        }

        public Builder passCount(int passCount) {
            this.passCount = passCount;
            return this;
        }

        public Builder failCount(int failCount) {
            this.failCount = failCount;
            return this;
        }

        public Builder flakinessRate(double flakinessRate) {
            this.flakinessRate = flakinessRate;
            return this;
        }

        public Builder detectedAt(Instant detectedAt) {
            this.detectedAt = detectedAt;
            return this;
        }

        public FlakyTest build() {
            return new FlakyTest(this);
        }
    }
}
