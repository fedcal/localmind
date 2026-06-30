package com.localmind.infrastructure.finetuning.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("localmind.finetuning")
public class FineTuningProperties {

    private boolean enabled = false;
    private String pythonPath = "python3";
    private String scriptsDir = "./scripts/finetuning";
    private String outputDir = "./finetuning-output";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPythonPath() {
        return pythonPath;
    }

    public void setPythonPath(String pythonPath) {
        this.pythonPath = pythonPath;
    }

    public String getScriptsDir() {
        return scriptsDir;
    }

    public void setScriptsDir(String scriptsDir) {
        this.scriptsDir = scriptsDir;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
}
