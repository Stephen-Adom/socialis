package com.alaska.socialis.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.alaska.socialis.model.Reply;
import com.alaska.socialis.utils.NotificationActivityType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMentionInReplyEvent extends ApplicationEvent {
    private List<String> mentions;
    private NotificationActivityType activityType;
    private Reply reply;

    public UserMentionInReplyEvent(List<String> mentions, NotificationActivityType type, Reply reply) {
        super(mentions);
        this.mentions = mentions;
        this.activityType = type;
        this.reply = reply;
    }
}
