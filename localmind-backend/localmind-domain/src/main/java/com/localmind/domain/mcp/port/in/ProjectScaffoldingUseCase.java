package com.localmind.domain.mcp.port.in;

import java.util.List;
import java.util.Map;

/**
 * Porta per il scaffolding di progetti.
 * Permette di generare strutture di progetto a partire da template predefiniti.
 */
public interface ProjectScaffoldingUseCase {

    /**
     * Lista i template di progetto disponibili.
     * Ogni template contiene name, description e files[].
     */
    List<Map<String, Object>> listProjectTemplates();

    /**
     * Genera la struttura di un progetto a partire da un template.
     * NON scrive file su disco: restituisce il contenuto generato come output strutturato.
     *
     * @param template    nome del template (es. "spring-boot-api")
     * @param projectName nome del progetto
     * @param outputDir   directory di output
     * @param author      autore del progetto
     * @param description descrizione del progetto (null per default)
     * @param license     licenza (null per default "MIT")
     * @return mappa con template, projectName, files[], totalFiles
     */
    Map<String, Object> scaffoldProject(String template, String projectName, String outputDir,
                                         String author, String description, String license);

    /**
     * Genera il codice sorgente di un singolo componente.
     * NON scrive file su disco: restituisce il codice generato come stringa.
     *
     * @param name     nome del componente
     * @param type     tipo: "component", "service", "controller", "model"
     * @param language linguaggio: "java", "typescript", "javascript"
     * @param outputDir directory di output
     * @return mappa con name, type, language, fileName, code
     */
    Map<String, Object> scaffoldComponent(String name, String type, String language, String outputDir);
}
