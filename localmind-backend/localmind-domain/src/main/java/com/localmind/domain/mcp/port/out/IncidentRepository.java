package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.Incident;

import java.util.List;
import java.util.Optional;

/**
 * Output port for persisting incident data.
 */
public interface IncidentRepository {

    /**
     * Saves an incident.
     *
     * @param incident the incident to persist
     * @return the saved incident
     */
    Incident save(Incident incident);

    /**
     * Finds an incident by its identifier.
     *
     * @param id the incident id
     * @return an Optional containing the incident if found
     */
    Optional<Incident> findById(String id);

    /**
     * Returns all incidents.
     *
     * @return list of all incidents
     */
    List<Incident> findAll();
}
