package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.ScaffoldedProject;

/**
 * Porta di uscita per la persistenza dei progetti scaffoldati.
 */
public interface ScaffoldingRepository {

    /**
     * Salva un record di progetto scaffoldato.
     */
    ScaffoldedProject save(ScaffoldedProject project);
}
