package com.localmind.domain.mcp.port.in;

import java.util.List;
import java.util.Map;

/**
 * Input port for the docker-compose tool group.
 * Provides docker-compose parsing, Dockerfile analysis, service listing and compose generation.
 */
public interface DockerAnalysisUseCase {

    /**
     * Parses docker-compose YAML content (received as a string).
     * Extracts services, networks, volumes and performs validation checks.
     *
     * @param content the docker-compose YAML content as a string
     * @return analysis result with services, networks, volumes, validationIssues
     */
    Map<String, Object> parseCompose(String content);

    /**
     * Analyzes Dockerfile content (received as a string).
     * Parses instructions and checks for best-practice violations.
     *
     * @param content the Dockerfile content as a string
     * @return analysis result with baseImage, stages, issues, summary
     */
    Map<String, Object> analyzeDockerfile(String content);

    /**
     * Returns a static list of common Docker services with their metadata.
     *
     * @return list of common Docker services with name, image, defaultPorts, description
     */
    Map<String, Object> listDockerServices();

    /**
     * Generates docker-compose YAML from a list of service definitions.
     *
     * @param services list of service definitions, each containing name, image, ports, environment, volumes
     * @return result containing the generated YAML string and service count
     */
    Map<String, Object> generateCompose(List<Map<String, Object>> services);
}
