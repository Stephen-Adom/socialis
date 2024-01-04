package com.alaska.socialis.model.requestModel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RepostBody {

    @NotNull(message = "Post id is required")
    private Long postId;

    @NotBlank(message = "Content is required")
    private String content;
}
