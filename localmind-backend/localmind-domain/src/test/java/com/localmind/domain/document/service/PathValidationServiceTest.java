package com.localmind.domain.document.service;

import com.localmind.domain.common.exception.DocumentProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PathValidationServiceTest {

    private PathValidationService service;

    @BeforeEach
    void setUp() {
        service = new PathValidationService();
    }

    // --- validateAndNormalize ---

    @Test
    void validateAndNormalize_withNullPath_shouldThrow() {
        assertThatThrownBy(() -> service.validateAndNormalize(null))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("null or empty");
    }

    @Test
    void validateAndNormalize_withEmptyPath_shouldThrow() {
        assertThatThrownBy(() -> service.validateAndNormalize(""))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("null or empty");
    }

    @Test
    void validateAndNormalize_withBlankPath_shouldThrow() {
        assertThatThrownBy(() -> service.validateAndNormalize("   "))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("null or empty");
    }

    @Test
    void validateAndNormalize_withNonExistentPath_shouldThrow() {
        assertThatThrownBy(() -> service.validateAndNormalize("/nonexistent/path/xyz_" + System.nanoTime()))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    void validateAndNormalize_withValidDirectory_shouldReturnNormalized(@TempDir Path tempDir) {
        String result = service.validateAndNormalize(tempDir.toString());
        assertThat(result).isEqualTo(tempDir.toAbsolutePath().normalize().toString());
    }

    @Test
    void validateAndNormalize_withTrailingSpaces_shouldTrim(@TempDir Path tempDir) {
        String result = service.validateAndNormalize("  " + tempDir.toString() + "  ");
        assertThat(result).isEqualTo(tempDir.toAbsolutePath().normalize().toString());
    }

    @Test
    void validateAndNormalize_withFileInsteadOfDirectory_shouldThrow(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("testfile.txt");
        Files.createFile(file);

        assertThatThrownBy(() -> service.validateAndNormalize(file.toString()))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("not a directory");
    }

    @Test
    void validateAndNormalize_withSubdirectory_shouldReturnNormalized(@TempDir Path tempDir) throws IOException {
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectory(subDir);

        String result = service.validateAndNormalize(subDir.toString());
        assertThat(result).isEqualTo(subDir.toAbsolutePath().normalize().toString());
    }

    @Test
    void validateAndNormalize_withDotDotInPath_shouldNormalize(@TempDir Path tempDir) throws IOException {
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectory(subDir);

        String pathWithDotDot = subDir.toString() + "/../subdir";
        String result = service.validateAndNormalize(pathWithDotDot);
        assertThat(result).isEqualTo(subDir.toAbsolutePath().normalize().toString());
    }

    // --- isAbsolutePath ---

    @Test
    void isAbsolutePath_withNull_shouldReturnFalse() {
        assertThat(service.isAbsolutePath(null)).isFalse();
    }

    @Test
    void isAbsolutePath_withEmpty_shouldReturnFalse() {
        assertThat(service.isAbsolutePath("")).isFalse();
    }

    @Test
    void isAbsolutePath_withBlank_shouldReturnFalse() {
        assertThat(service.isAbsolutePath("   ")).isFalse();
    }

    @Test
    void isAbsolutePath_withAbsoluteLinuxPath_shouldReturnTrue() {
        assertThat(service.isAbsolutePath("/home/user/docs")).isTrue();
    }

    @Test
    void isAbsolutePath_withRelativePath_shouldReturnFalse() {
        assertThat(service.isAbsolutePath("relative/path")).isFalse();
    }

    @Test
    void isAbsolutePath_withTrailingSpaces_shouldTrimAndCheck() {
        assertThat(service.isAbsolutePath("  /home/user/docs  ")).isTrue();
    }

    @Test
    void isAbsolutePath_withDotRelativePath_shouldReturnFalse() {
        assertThat(service.isAbsolutePath("./relative/path")).isFalse();
    }
}
