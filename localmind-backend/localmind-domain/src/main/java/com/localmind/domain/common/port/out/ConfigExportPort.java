package com.localmind.domain.common.port.out;

/**
 * Output port for configuration export and import.
 * Handles serialization of provider configs and document metadata.
 */
public interface ConfigExportPort {

    /**
     * Exports all LLM provider configurations as JSON bytes.
     *
     * @return JSON-encoded provider configurations
     */
    byte[] exportProviderConfigs();

    /**
     * Imports LLM provider configurations from JSON bytes.
     *
     * @param data JSON-encoded provider configurations
     */
    void importProviderConfigs(byte[] data);

    /**
     * Exports all document metadata as JSON bytes.
     *
     * @return JSON-encoded document metadata
     */
    byte[] exportDocumentMetadata();
}
