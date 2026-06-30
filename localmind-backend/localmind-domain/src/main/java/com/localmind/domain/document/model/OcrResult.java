package com.localmind.domain.document.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Risultato dell'estrazione OCR da un documento immagine o PDF scansionato.
 * Result of OCR extraction from an image document or scanned PDF.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrResult {
    private String text;
    private double confidence;  // 0-100
    private String language;
    private int pageCount;
}
