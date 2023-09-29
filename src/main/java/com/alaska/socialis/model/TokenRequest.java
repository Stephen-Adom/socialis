package com.alaska.socialis.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TokenRequest {

    @NotBlank(message = "Refresh Token is required")
    private String refreshToken;
}
