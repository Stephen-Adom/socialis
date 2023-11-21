package com.alaska.socialis.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.alaska.socialis.model.Comment;
import com.alaska.socialis.utils.NotificationActivityType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMentionInCommentEvent extends ApplicationEvent {
    private List<String> mentions;
    private NotificationActivityType activityType;
    private Comment comment;

    public UserMentionInCommentEvent(List<String> mentions, NotificationActivityType type, Comment comment) {
        super(mentions);
        this.mentions = mentions;
        this.activityType = type;
        this.comment = comment;
    }
}
