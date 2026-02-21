package com.localmind.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetupPasswordRequestDto {

    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;
}
