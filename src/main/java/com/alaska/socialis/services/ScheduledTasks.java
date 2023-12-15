package com.alaska.socialis.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alaska.socialis.model.Post;
import com.alaska.socialis.repository.PostRepository;

@Service
public class ScheduledTasks {

    @Autowired
    private PostRepository postRepository;

    // @Scheduled(fixedRate = 1000)
    public void scheduledPosts() {
        LocalDateTime currentTime = LocalDateTime.now();
        List<Post> allScheduledPosts = this.postRepository.findAllByScheduledAtNotNullAndScheduledAtBefore(currentTime);
        System.out.println(" scheduled posts " + allScheduledPosts.size());
    }
}
