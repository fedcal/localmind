package com.localmind.infrastructure.document.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Proprieta' di configurazione per il motore OCR (Tesseract).
 * Configuration properties for the OCR engine (Tesseract).
 */
@ConfigurationProperties(prefix = "localmind.ocr")
public class OcrProperties {

    private boolean enabled = false;
    private String datapath = "/usr/share/tesseract-ocr/5/tessdata";
    private String languages = "ita+eng";
    private int dpi = 300;
    private double minConfidence = 30.0;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDatapath() {
        return datapath;
    }

    public void setDatapath(String datapath) {
        this.datapath = datapath;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public int getDpi() {
        return dpi;
    }

    public void setDpi(int dpi) {
        this.dpi = dpi;
    }

    public double getMinConfidence() {
        return minConfidence;
    }

    public void setMinConfidence(double minConfidence) {
        this.minConfidence = minConfidence;
    }
}
