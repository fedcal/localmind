package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.GeneratedTest;

/**
 * Output port for persisting generated test results.
 */
public interface TestGeneratorRepository {

    /**
     * Saves a generated test result.
     *
     * @param generatedTest the generated test to persist
     * @return the saved generated test (with generated id if new)
     */
    GeneratedTest save(GeneratedTest generatedTest);
}
