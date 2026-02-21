package com.localmind.domain.document.port.out;

import com.localmind.domain.document.model.OcrResult;

import java.io.InputStream;

/**
 * Port per l'estrazione di testo tramite OCR (Optical Character Recognition).
 * Port for text extraction via OCR (Optical Character Recognition).
 */
public interface OcrExtractorPort {

    /**
     * Estrae testo da un documento immagine o PDF scansionato.
     * Extracts text from an image document or scanned PDF.
     *
     * @param content   lo stream del contenuto del file / the file content stream
     * @param mimeType  il tipo MIME del file / the file MIME type
     * @return il risultato dell'estrazione OCR / the OCR extraction result
     */
    OcrResult extract(InputStream content, String mimeType);

    /**
     * Verifica se il motore OCR e' disponibile (es. se tessdata e' configurato).
     * Checks whether the OCR engine is available (e.g. if tessdata is configured).
     *
     * @return true se l'OCR e' disponibile / true if OCR is available
     */
    boolean isAvailable();
}
