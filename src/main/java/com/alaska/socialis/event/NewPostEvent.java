package com.alaska.socialis.event;

import org.springframework.context.ApplicationEvent;

import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewPostEvent extends ApplicationEvent {
    private User user;
    private Post post;

    public NewPostEvent(Post post) {
        super(post);
        this.user = post.getUser();
        this.post = post;
    }
}
