package com.alaska.socialis.event;

import org.springframework.context.ApplicationEvent;

import com.alaska.socialis.model.Comment;
import com.alaska.socialis.model.CommentLike;
import com.alaska.socialis.model.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentLikeEvent extends ApplicationEvent {
    private Comment comment;
    private User user;

    public CommentLikeEvent(CommentLike newLike) {
        super(newLike);
        this.comment = newLike.getComment();
        this.user = newLike.getUser();
    }
}
