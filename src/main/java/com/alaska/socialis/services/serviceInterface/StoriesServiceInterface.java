package com.alaska.socialis.services.serviceInterface;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.Story;
import com.alaska.socialis.model.dto.StoryDto;
import com.alaska.socialis.model.dto.WatchedStoryDto;

public interface StoriesServiceInterface {

        public StoryDto fetchAuthUserStories(Long userId) throws EntityNotFoundException;

        public void uploadStory(MultipartFile file, String caption, Long userId)
                        throws IOException, EntityNotFoundException;

        public Map<String, Object> uploadStoryImage(MultipartFile file) throws IOException;

        public Map<String, Object> uploadAndSliceVideo(MultipartFile file) throws IOException;

        public StoryDto buildUserStory(Story userStories);

        public void recordStoryWatchedByUser(Long userId, Long mediaId)
                        throws EntityNotFoundException;

        public List<WatchedStoryDto> usersWatchedAMedia(Long mediaId);
}
