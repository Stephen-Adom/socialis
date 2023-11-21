package com.alaska.socialis.event;

import org.springframework.context.ApplicationEvent;

import com.alaska.socialis.model.Reply;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewReplyEvent extends ApplicationEvent {
    private Reply reply;

    public NewReplyEvent(Reply reply) {
        super(reply);
        this.reply = reply;
    }
}
