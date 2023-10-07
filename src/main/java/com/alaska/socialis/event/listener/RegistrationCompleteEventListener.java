package com.alaska.socialis.event.listener;

import java.util.UUID;

import org.springframework.context.ApplicationListener;

import com.alaska.socialis.event.RegistrationCompleteEvent;
import com.alaska.socialis.model.User;

public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        // create verification token for the user with link
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
    }
}
