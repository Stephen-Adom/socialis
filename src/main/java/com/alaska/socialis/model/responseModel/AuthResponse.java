package com.alaska.socialis.model.responseModel;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private HttpStatus status;
    private Object data;
    private String accessToken;
    private String refreshToken;
}
