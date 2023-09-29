package com.alaska.socialis.model.requestModel;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdatePostRequest {
    @NotNull(message = "User id is required")
    private Long user_id;
    @NotNull(message = "Post content is required")
    private String content;
}
