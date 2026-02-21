package com.localmind.infrastructure.document.adapter;

import com.localmind.domain.common.exception.DocumentProcessingException;
import com.localmind.domain.document.port.out.TextExtractorPort;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class TikaTextExtractor implements TextExtractorPort {

    private final Tika tika = new Tika();

    @Override
    public String extract(InputStream content, String mimeType) {
        try {
            return tika.parseToString(content);
        } catch (Exception e) {
            throw new DocumentProcessingException("Failed to extract text: " + e.getMessage(), e);
        }
    }
}
