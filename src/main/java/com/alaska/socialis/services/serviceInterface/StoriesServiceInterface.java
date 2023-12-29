package com.alaska.socialis.services.serviceInterface;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.Story;
import com.alaska.socialis.model.dto.StoryDto;

public interface StoriesServiceInterface {

    public List<StoryDto> fetchAuthUserStories(Long userId) throws EntityNotFoundException;

    public void uploadStory(MultipartFile file, String caption, Long userId)
            throws IOException, EntityNotFoundException;

    public Map<String, Object> uploadStoryImage(MultipartFile file) throws IOException;

    public Map<String, Object> uploadAndSliceVideo(MultipartFile file) throws IOException;

    public List<StoryDto> buildUserStory(List<Story> userStories);
}
