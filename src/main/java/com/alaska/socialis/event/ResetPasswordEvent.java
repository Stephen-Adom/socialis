package com.alaska.socialis.event;

import org.springframework.context.ApplicationEvent;

import com.alaska.socialis.model.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordEvent extends ApplicationEvent {
    private User user;
    private String applicationUrl;

    public ResetPasswordEvent(User user, String applicationUrl) {
        super(user);
        this.user = user;
        this.applicationUrl = applicationUrl;
    }
}
