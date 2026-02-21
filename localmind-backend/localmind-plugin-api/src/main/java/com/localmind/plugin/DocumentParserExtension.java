package com.localmind.plugin;

import org.pf4j.ExtensionPoint;

import java.io.InputStream;
import java.util.Set;

/**
 * Extension point per parser di documenti personalizzati.
 * Implementare questa interfaccia per aggiungere il supporto a nuovi formati di documento.
 *
 * Extension point for custom document parsers.
 * Implement this interface to add support for new document formats.
 */
public interface DocumentParserExtension extends ExtensionPoint {

    /**
     * Restituisce l'insieme dei tipi MIME supportati da questo parser.
     * Returns the set of MIME types supported by this parser.
     *
     * @return tipi MIME supportati / supported MIME types
     */
    Set<String> getSupportedMimeTypes();

    /**
     * Estrae il testo dal contenuto del documento.
     * Extracts text from the document content.
     *
     * @param content  lo stream del contenuto del documento / the document content stream
     * @param mimeType il tipo MIME del documento / the document MIME type
     * @return il testo estratto / the extracted text
     */
    String extractText(InputStream content, String mimeType);
}
