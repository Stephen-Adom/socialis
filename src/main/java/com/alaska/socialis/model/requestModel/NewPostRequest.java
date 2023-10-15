package com.alaska.socialis.model.requestModel;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewPostRequest {

    @NotNull(message = "User id is required")
    private Long user_id;
    private String content;
    private List<MultipartFile> postImages = new ArrayList<MultipartFile>();
}
