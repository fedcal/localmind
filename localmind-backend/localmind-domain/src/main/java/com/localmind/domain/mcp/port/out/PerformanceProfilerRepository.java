package com.localmind.domain.mcp.port.out;

import com.localmind.domain.mcp.model.BenchmarkResult;

/**
 * Output port for persisting performance profiler results.
 */
public interface PerformanceProfilerRepository {

    /**
     * Saves a benchmark/profiler result.
     *
     * @param result the result to persist
     * @return the saved result (with generated id if new)
     */
    BenchmarkResult save(BenchmarkResult result);
}
