package com.localmind.infrastructure.document.adapter;

import com.localmind.domain.common.exception.DocumentProcessingException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TikaTextExtractorTest {

    private final TikaTextExtractor extractor = new TikaTextExtractor();

    @Test
    void extract_shouldReturnTextFromPlainText() {
        // given
        String inputText = "Hello World";
        InputStream content = new ByteArrayInputStream(inputText.getBytes(StandardCharsets.UTF_8));

        // when
        String result = extractor.extract(content, "text/plain");

        // then
        assertThat(result).isNotNull();
        assertThat(result.trim()).isEqualTo("Hello World");
    }

    @Test
    void extract_shouldThrowOnNullInput() {
        // when / then
        assertThatThrownBy(() -> extractor.extract(null, "text/plain"))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("Failed to extract text");
    }
}
