package com.localmind.plugin;

import org.pf4j.ExtensionPoint;

import java.util.Map;

/**
 * Extension point per provider LLM personalizzati.
 * Implementare questa interfaccia per aggiungere un nuovo provider LLM tramite plugin.
 *
 * Extension point for custom LLM providers.
 * Implement this interface to add a new LLM provider via plugin.
 */
public interface LlmProviderExtension extends ExtensionPoint {

    /**
     * Restituisce il nome univoco del provider.
     * Returns the unique provider name.
     *
     * @return nome del provider / provider name
     */
    String getProviderName();

    /**
     * Invia un prompt al provider e restituisce la risposta.
     * Sends a prompt to the provider and returns the response.
     *
     * @param prompt  il testo del prompt / the prompt text
     * @param options opzioni aggiuntive (temperatura, max tokens, ecc.) / additional options (temperature, max tokens, etc.)
     * @return la risposta del modello / the model response
     */
    String chat(String prompt, Map<String, Object> options);

    /**
     * Verifica se il provider e' disponibile e raggiungibile.
     * Checks whether the provider is available and reachable.
     *
     * @return true se il provider e' disponibile / true if the provider is available
     */
    boolean isAvailable();
}
