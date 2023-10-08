package com.alaska.socialis.model.requestModel;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailValidationTokenRequest {

    @NotBlank(message = "Token is required")
    private String token;
}
