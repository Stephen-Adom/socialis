package com.alaska.socialis.event;

import org.springframework.context.ApplicationEvent;

import com.alaska.socialis.model.Comment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewCommentEvent extends ApplicationEvent {
    private Comment comment;

    public NewCommentEvent(Comment comment) {
        super(comment);
        this.comment = comment;
    }
}
