package com.alaska.socialis.event.listener;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.alaska.socialis.event.RegistrationCompleteEvent;
import com.alaska.socialis.model.User;
import com.alaska.socialis.services.AuthenticationService;
import com.alaska.socialis.services.EmailService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        // create verification token for the user with link
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        this.authenticationService.saveEmailVerificationToken(user, token);

        // Send email to user to verify email
        String applicationUrl = event.getApplicationUrl() + "/verifyEmailAddress?token=" + token;
        this.sendEmail(user, applicationUrl);
    }

    public void sendEmail(User user, String applicationUrl) {
        Context context = new Context();
        String to = user.getEmail();
        String subject = "Email Verification";
        context.setVariable("fullname", user.getFirstname() + " " + user.getLastname());
        context.setVariable("url", applicationUrl);
        String emailContent = this.templateEngine.process("email_verification_template", context);

        this.emailService.sendEmail(to, subject, emailContent);
    }
}
