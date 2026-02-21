package com.localmind.infrastructure.document.adapter;

import com.localmind.domain.common.exception.DocumentProcessingException;
import com.localmind.domain.document.model.OcrResult;
import com.localmind.infrastructure.document.config.OcrProperties;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TesseractOcrExtractorTest {

    private TesseractOcrExtractor extractor;
    private Tesseract mockTesseract;
    private OcrProperties ocrProperties;

    @BeforeEach
    void setUp() {
        ocrProperties = new OcrProperties();
        ocrProperties.setEnabled(true);
        ocrProperties.setDatapath("/tmp/tessdata");
        ocrProperties.setLanguages("ita+eng");
        ocrProperties.setDpi(300);
        ocrProperties.setMinConfidence(30.0);

        extractor = new TesseractOcrExtractor(ocrProperties);

        mockTesseract = mock(Tesseract.class);
        ReflectionTestUtils.setField(extractor, "tesseract", mockTesseract);
    }

    @Test
    void extract_image_shouldReturnOcrResult() throws TesseractException {
        // given
        String expectedText = "Testo estratto dalla immagine / Extracted text from image";
        when(mockTesseract.doOCR(any(BufferedImage.class))).thenReturn(expectedText);

        // Crea un'immagine PNG minima in memoria / Create a minimal PNG image in memory
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try {
            javax.imageio.ImageIO.write(testImage, "png", baos);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        InputStream imageStream = new ByteArrayInputStream(baos.toByteArray());

        // when
        OcrResult result = extractor.extract(imageStream, "image/png");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo(expectedText);
        assertThat(result.getConfidence()).isGreaterThan(0.0);
        assertThat(result.getLanguage()).isEqualTo("ita+eng");
        assertThat(result.getPageCount()).isEqualTo(1);
    }

    @Test
    void extract_image_shouldReturnEmptyTextWhenTesseractReturnsBlank() throws TesseractException {
        // given
        when(mockTesseract.doOCR(any(BufferedImage.class))).thenReturn("   ");

        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try {
            javax.imageio.ImageIO.write(testImage, "png", baos);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        InputStream imageStream = new ByteArrayInputStream(baos.toByteArray());

        // when
        OcrResult result = extractor.extract(imageStream, "image/png");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getText()).isEmpty();
        assertThat(result.getPageCount()).isEqualTo(1);
    }

    @Test
    void extract_shouldThrowOnNullContent() {
        assertThatThrownBy(() -> extractor.extract(null, "image/png"))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("null");
    }

    @Test
    void extract_shouldThrowOnNullMimeType() {
        InputStream content = new ByteArrayInputStream("test".getBytes());
        assertThatThrownBy(() -> extractor.extract(content, null))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("null");
    }

    @Test
    void extract_shouldThrowOnUnsupportedMimeType() {
        InputStream content = new ByteArrayInputStream("test".getBytes());
        assertThatThrownBy(() -> extractor.extract(content, "text/plain"))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("non supportato")
                .hasMessageContaining("text/plain");
    }

    @Test
    void extract_shouldThrowWhenTesseractFails() throws TesseractException {
        // given
        when(mockTesseract.doOCR(any(BufferedImage.class)))
                .thenThrow(new TesseractException("Tesseract error"));

        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try {
            javax.imageio.ImageIO.write(testImage, "png", baos);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        InputStream imageStream = new ByteArrayInputStream(baos.toByteArray());

        // when / then
        assertThatThrownBy(() -> extractor.extract(imageStream, "image/png"))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("OCR extraction failed");
    }

    @Test
    void isAvailable_shouldReturnTrueWhenDatapathExists() {
        // given - usa /tmp che esiste sempre / use /tmp which always exists
        ocrProperties.setDatapath("/tmp");
        TesseractOcrExtractor tempExtractor = new TesseractOcrExtractor(ocrProperties);

        // when / then
        assertThat(tempExtractor.isAvailable()).isTrue();
    }

    @Test
    void isAvailable_shouldReturnFalseWhenDatapathNotExists() {
        // given
        ocrProperties.setDatapath("/nonexistent/path/tessdata");
        TesseractOcrExtractor tempExtractor = new TesseractOcrExtractor(ocrProperties);

        // when / then
        assertThat(tempExtractor.isAvailable()).isFalse();
    }

    @Test
    void extract_image_shouldHandleNullTextFromTesseract() throws TesseractException {
        // given
        when(mockTesseract.doOCR(any(BufferedImage.class))).thenReturn(null);

        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try {
            javax.imageio.ImageIO.write(testImage, "png", baos);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        InputStream imageStream = new ByteArrayInputStream(baos.toByteArray());

        // when
        OcrResult result = extractor.extract(imageStream, "image/png");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getText()).isEmpty();
        assertThat(result.getConfidence()).isEqualTo(0.0);
    }

    @Test
    void extract_image_jpeg_shouldWork() throws TesseractException {
        // given
        String expectedText = "JPEG text content";
        when(mockTesseract.doOCR(any(BufferedImage.class))).thenReturn(expectedText);

        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try {
            javax.imageio.ImageIO.write(testImage, "jpg", baos);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        InputStream imageStream = new ByteArrayInputStream(baos.toByteArray());

        // when
        OcrResult result = extractor.extract(imageStream, "image/jpeg");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo(expectedText);
        assertThat(result.getLanguage()).isEqualTo("ita+eng");
    }
}
