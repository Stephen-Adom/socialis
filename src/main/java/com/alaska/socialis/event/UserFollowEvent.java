package com.alaska.socialis.event;

import org.springframework.context.ApplicationEvent;

import com.alaska.socialis.model.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFollowEvent extends ApplicationEvent {
    private User follower;
    private User following;

    public UserFollowEvent(User follower, User following) {
        super(follower);
        this.follower = follower;
        this.following = following;
    }
}
