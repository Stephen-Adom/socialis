package com.alaska.socialis.model.responseModel;

import org.springframework.http.HttpStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuccessResponse {
    private HttpStatus status;
    private Object data;
}
