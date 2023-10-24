package com.alaska.socialis.model.requestModel;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneValidationRequeset {

    @NotBlank(message = "Phonenumber is required")
    private String phonenumber;
}
