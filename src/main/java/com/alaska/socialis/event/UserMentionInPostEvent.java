package com.alaska.socialis.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.alaska.socialis.model.Post;
import com.alaska.socialis.utils.NotificationActivityType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMentionInPostEvent extends ApplicationEvent {
    private List<String> mentions;
    private NotificationActivityType activityType;
    private Post post;

    public UserMentionInPostEvent(List<String> mentions, NotificationActivityType type, Post post) {
        super(mentions);
        this.mentions = mentions;
        this.activityType = type;
        this.post = post;
    }
}
