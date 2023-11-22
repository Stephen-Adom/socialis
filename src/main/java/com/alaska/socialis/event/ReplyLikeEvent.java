package com.alaska.socialis.event;

import org.springframework.context.ApplicationEvent;

import com.alaska.socialis.model.Reply;
import com.alaska.socialis.model.ReplyLike;
import com.alaska.socialis.model.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplyLikeEvent extends ApplicationEvent {
    private Reply reply;
    private User user;

    public ReplyLikeEvent(ReplyLike newLike) {
        super(newLike);
        this.reply = newLike.getReply();
        this.user = newLike.getUser();
    }
}
