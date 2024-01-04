package com.alaska.socialis.model.requestModel;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class RepostBody {

    @NotBlank(message = "Post id is required")
    private Long postId;

    @NotBlank(message = "Content is required")
    private String content;
}
