package com.alaska.socialis.model.requestModel;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class GoogleUserRequest {

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "firstname is required")
    private String firstName;

    @NotBlank(message = "lastname is required")
    private String lastName;

    private String photoUrl;
}
