package com.localmind.api.mcp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMcpServerRequestDto {
    @NotBlank(message = "Server name is required")
    private String name;
    private String description;
    @NotNull(message = "Server type is required")
    private String type;
    private String command;
    private List<String> args;
    private String url;
    private Integer timeoutSeconds;
    private Boolean autoReconnect;
}
