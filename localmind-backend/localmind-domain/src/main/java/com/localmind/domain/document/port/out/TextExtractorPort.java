package com.localmind.domain.document.port.out;

import java.io.InputStream;

public interface TextExtractorPort {
    String extract(InputStream content, String mimeType);
}
