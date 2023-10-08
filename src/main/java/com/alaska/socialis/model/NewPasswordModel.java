package com.alaska.socialis.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewPasswordModel {

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must have minimum of 6 characters")
    private String password;
}
