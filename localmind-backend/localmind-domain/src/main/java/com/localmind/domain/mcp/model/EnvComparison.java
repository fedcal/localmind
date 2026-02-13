package com.localmind.domain.mcp.model;

import java.util.List;

/**
 * Domain model representing the result of comparing two environment files.
 * No persistence — purely a computed result.
 */
public class EnvComparison {

    private final String fileA;
    private final String fileB;
    private final List<String> onlyInA;
    private final List<String> onlyInB;
    private final List<String> common;
    private final List<String> different;

    private EnvComparison(Builder builder) {
        this.fileA = builder.fileA;
        this.fileB = builder.fileB;
        this.onlyInA = builder.onlyInA;
        this.onlyInB = builder.onlyInB;
        this.common = builder.common;
        this.different = builder.different;
    }

    public String getFileA() {
        return fileA;
    }

    public String getFileB() {
        return fileB;
    }

    public List<String> getOnlyInA() {
        return onlyInA;
    }

    public List<String> getOnlyInB() {
        return onlyInB;
    }

    public List<String> getCommon() {
        return common;
    }

    public List<String> getDifferent() {
        return different;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fileA;
        private String fileB;
        private List<String> onlyInA;
        private List<String> onlyInB;
        private List<String> common;
        private List<String> different;

        public Builder fileA(String fileA) {
            this.fileA = fileA;
            return this;
        }

        public Builder fileB(String fileB) {
            this.fileB = fileB;
            return this;
        }

        public Builder onlyInA(List<String> onlyInA) {
            this.onlyInA = onlyInA;
            return this;
        }

        public Builder onlyInB(List<String> onlyInB) {
            this.onlyInB = onlyInB;
            return this;
        }

        public Builder common(List<String> common) {
            this.common = common;
            return this;
        }

        public Builder different(List<String> different) {
            this.different = different;
            return this;
        }

        public EnvComparison build() {
            return new EnvComparison(this);
        }
    }
}
