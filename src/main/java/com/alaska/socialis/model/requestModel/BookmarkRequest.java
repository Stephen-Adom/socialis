package com.alaska.socialis.model.requestModel;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookmarkRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Content id is required")
    private Long contentId;

    @NotNull(message = "Content type is required")
    private String contentType;
}
