package com.localmind.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthStatusResponseDto {

    private boolean configured;
    private boolean authenticated;
}
