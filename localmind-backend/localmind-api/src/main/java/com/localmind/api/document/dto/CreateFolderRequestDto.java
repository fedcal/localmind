package com.localmind.api.document.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFolderRequestDto {
    @NotBlank(message = "Path is required")
    private String path;
    @Builder.Default
    private boolean recursive = true;
    @Builder.Default
    private boolean watchEnabled = false;
}
