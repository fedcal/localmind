package com.localmind.domain.llm.port.out;

import java.io.InputStream;

public interface AudioTranscriptionPort {
    String transcribe(InputStream audio, String mimeType);
    boolean isAvailable();
}
