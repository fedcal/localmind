package com.localmind.infrastructure.backup.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "localmind.backup")
@Data
public class BackupProperties {
    private String directory = "./backups";
    private int retentionDays = 30;
    private String scheduleCron = "0 0 2 * * *";
}
