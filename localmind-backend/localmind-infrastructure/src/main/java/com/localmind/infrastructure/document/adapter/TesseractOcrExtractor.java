package com.localmind.infrastructure.document.adapter;

import com.localmind.domain.common.exception.DocumentProcessingException;
import com.localmind.domain.document.model.OcrResult;
import com.localmind.domain.document.port.out.OcrExtractorPort;
import com.localmind.infrastructure.document.config.OcrProperties;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Adapter infrastrutturale che implementa OcrExtractorPort utilizzando Tesseract (tess4j).
 * Supporta immagini (image/*) e PDF scansionati (application/pdf).
 *
 * Infrastructure adapter implementing OcrExtractorPort using Tesseract (tess4j).
 * Supports images (image/*) and scanned PDFs (application/pdf).
 */
@Component
@ConditionalOnProperty(name = "localmind.ocr.enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties(OcrProperties.class)
public class TesseractOcrExtractor implements OcrExtractorPort {

    private static final Logger log = LoggerFactory.getLogger(TesseractOcrExtractor.class);

    private final Tesseract tesseract;
    private final OcrProperties ocrProperties;

    public TesseractOcrExtractor(OcrProperties ocrProperties) {
        this.ocrProperties = ocrProperties;
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath(ocrProperties.getDatapath());
        this.tesseract.setLanguage(ocrProperties.getLanguages());
        this.tesseract.setVariable("user_defined_dpi", String.valueOf(ocrProperties.getDpi()));
        log.info("OCR Tesseract configurato: datapath={}, lingue={}, DPI={}",
                ocrProperties.getDatapath(), ocrProperties.getLanguages(), ocrProperties.getDpi());
    }

    @Override
    public OcrResult extract(InputStream content, String mimeType) {
        if (content == null) {
            throw new DocumentProcessingException("Il contenuto del file e' null / File content is null");
        }
        if (mimeType == null) {
            throw new DocumentProcessingException("Il tipo MIME e' null / MIME type is null");
        }

        try {
            if (mimeType.startsWith("image/")) {
                return extractFromImage(content);
            } else if ("application/pdf".equals(mimeType)) {
                return extractFromPdf(content);
            } else {
                throw new DocumentProcessingException(
                        "Tipo MIME non supportato per OCR: " + mimeType +
                        " / Unsupported MIME type for OCR: " + mimeType);
            }
        } catch (DocumentProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new DocumentProcessingException(
                    "Errore durante l'estrazione OCR / OCR extraction failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        File dataDir = new File(ocrProperties.getDatapath());
        return dataDir.exists() && dataDir.isDirectory();
    }

    private OcrResult extractFromImage(InputStream content) throws IOException, TesseractException {
        BufferedImage image = ImageIO.read(content);
        if (image == null) {
            throw new DocumentProcessingException(
                    "Impossibile leggere l'immagine dallo stream / Unable to read image from stream");
        }

        String text = tesseract.doOCR(image);
        // Tess4j non espone direttamente la confidence a livello di pagina tramite doOCR(BufferedImage).
        // Si usa un valore stimato basato sulla lunghezza del testo estratto.
        // Tess4j does not directly expose page-level confidence via doOCR(BufferedImage).
        // We use a heuristic based on extracted text length.
        double confidence = estimateConfidence(text);

        return OcrResult.builder()
                .text(text != null ? text.trim() : "")
                .confidence(confidence)
                .language(ocrProperties.getLanguages())
                .pageCount(1)
                .build();
    }

    private OcrResult extractFromPdf(InputStream content) throws IOException, TesseractException {
        try (PDDocument document = Loader.loadPDF(content.readAllBytes())) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            StringBuilder fullText = new StringBuilder();
            List<Double> confidences = new ArrayList<>();

            for (int page = 0; page < pageCount; page++) {
                BufferedImage image = renderer.renderImageWithDPI(page, ocrProperties.getDpi(), ImageType.RGB);
                String pageText = tesseract.doOCR(image);
                if (pageText != null && !pageText.isBlank()) {
                    fullText.append(pageText.trim());
                    if (page < pageCount - 1) {
                        fullText.append("\n\n");
                    }
                    confidences.add(estimateConfidence(pageText));
                }
            }

            double avgConfidence = confidences.isEmpty() ? 0.0 :
                    confidences.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            return OcrResult.builder()
                    .text(fullText.toString().trim())
                    .confidence(avgConfidence)
                    .language(ocrProperties.getLanguages())
                    .pageCount(pageCount)
                    .build();
        }
    }

    /**
     * Stima euristica della confidence basata sulla qualita' del testo estratto.
     * Le parole con caratteri alfanumerici validi aumentano il punteggio.
     *
     * Heuristic confidence estimation based on extracted text quality.
     * Words with valid alphanumeric characters increase the score.
     */
    private double estimateConfidence(String text) {
        if (text == null || text.isBlank()) {
            return 0.0;
        }
        String trimmed = text.trim();
        long totalChars = trimmed.length();
        long alphanumericChars = trimmed.chars()
                .filter(c -> Character.isLetterOrDigit(c) || Character.isWhitespace(c) || c == '.' || c == ',')
                .count();

        if (totalChars == 0) {
            return 0.0;
        }

        // Rapporto tra caratteri "buoni" e totali, scalato a 0-100
        // Ratio of "good" chars to total, scaled to 0-100
        return Math.min(100.0, ((double) alphanumericChars / totalChars) * 100.0);
    }
}
