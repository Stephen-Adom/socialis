package com.alaska.socialis.event.listener;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.alaska.socialis.event.RegistrationCompleteEvent;
import com.alaska.socialis.model.User;
import com.alaska.socialis.services.AuthenticationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        // create verification token for the user with link
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        this.authenticationService.saveEmailVerificationToken(user, token);

        // Send email to user to verify email
        String applicationUrl = event.getApplicationUrl() + "/verifyEmailAddress?token=" + token;
        log.info("click the link to verify your account----", applicationUrl);
        log.info(applicationUrl);
    }
}
