package com.alaska.socialis.model.requestModel;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequest {
    @NotNull(message = "User id is required")
    private Long user_id;

    @NotNull(message = "Post id is required")
    private Long post_id;

    @NotBlank(message = "Comment content is required")
    private String content;

    List<MultipartFile> commentImages = new ArrayList<MultipartFile>();
}
