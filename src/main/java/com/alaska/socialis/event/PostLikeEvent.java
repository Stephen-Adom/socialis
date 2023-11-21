package com.alaska.socialis.event;

import org.springframework.context.ApplicationEvent;

import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.PostLike;
import com.alaska.socialis.model.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostLikeEvent extends ApplicationEvent {
    private Post post;
    private User user;

    public PostLikeEvent(PostLike newLike) {
        super(newLike);
        this.post = newLike.getPost();
        this.user = newLike.getUser();
    }
}
