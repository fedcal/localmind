package com.localmind.sdk;

import java.util.Map;

/**
 * Health status of the LocalMind platform and its services.
 */
public class HealthStatus {

    private String status;
    private Map<String, String> services;

    public HealthStatus() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getServices() {
        return services;
    }

    public void setServices(Map<String, String> services) {
        this.services = services;
    }

    /**
     * Returns true if the overall status is "UP".
     */
    public boolean isHealthy() {
        return "UP".equals(status);
    }

    @Override
    public String toString() {
        return "HealthStatus{status='" + status + "', services=" + services + "}";
    }
}
