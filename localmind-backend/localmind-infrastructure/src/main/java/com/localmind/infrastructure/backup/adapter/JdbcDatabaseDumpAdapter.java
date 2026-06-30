package com.localmind.infrastructure.backup.adapter;

import com.localmind.domain.common.exception.LocalMindException;
import com.localmind.domain.common.port.out.DatabaseDumpPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JdbcDatabaseDumpAdapter implements DatabaseDumpPort {

    private static final Logger log = LoggerFactory.getLogger(JdbcDatabaseDumpAdapter.class);
    private static final Pattern JDBC_URL_PATTERN =
            Pattern.compile("jdbc:mysql://([^:/]+):(\\d+)/([^?]+)");

    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;

    public JdbcDatabaseDumpAdapter(
            @Value("${spring.datasource.url}") String jdbcUrl,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {
        this.username = username;
        this.password = password;

        Matcher matcher = JDBC_URL_PATTERN.matcher(jdbcUrl);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot parse JDBC URL: " + jdbcUrl);
        }
        this.host = matcher.group(1);
        this.port = matcher.group(2);
        this.database = matcher.group(3);
    }

    @Override
    public byte[] dumpDatabase() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "mysqldump",
                    "-h", host,
                    "-P", port,
                    "-u", username,
                    "-p" + password,
                    "--single-transaction",
                    "--routines",
                    "--triggers",
                    database
            );
            pb.redirectErrorStream(false);

            Process process = pb.start();

            byte[] output;
            try (InputStream is = process.getInputStream();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                output = baos.toByteArray();
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                try (InputStream err = process.getErrorStream()) {
                    String errorMsg = new String(err.readAllBytes());
                    log.error("mysqldump failed with exit code {}: {}", exitCode, errorMsg);
                }
                throw new LocalMindException("mysqldump failed with exit code: " + exitCode);
            }

            log.info("Database dump completed successfully, size: {} bytes", output.length);
            return output;

        } catch (LocalMindException e) {
            throw e;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new LocalMindException("Failed to dump database: " + e.getMessage(), e);
        }
    }

    @Override
    public void restoreDatabase(byte[] data) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "mysql",
                    "-h", host,
                    "-P", port,
                    "-u", username,
                    "-p" + password,
                    database
            );
            pb.redirectErrorStream(false);

            Process process = pb.start();

            try (OutputStream os = process.getOutputStream()) {
                os.write(data);
                os.flush();
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                try (InputStream err = process.getErrorStream()) {
                    String errorMsg = new String(err.readAllBytes());
                    log.error("mysql restore failed with exit code {}: {}", exitCode, errorMsg);
                }
                throw new LocalMindException("mysql restore failed with exit code: " + exitCode);
            }

            log.info("Database restore completed successfully from {} bytes", data.length);

        } catch (LocalMindException e) {
            throw e;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new LocalMindException("Failed to restore database: " + e.getMessage(), e);
        }
    }
}
