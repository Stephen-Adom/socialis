package com.alaska.socialis.event.listener;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.alaska.socialis.event.ResetPasswordEvent;
import com.alaska.socialis.model.ResetPasswordModel;
import com.alaska.socialis.model.User;
import com.alaska.socialis.repository.ResetPasswordRepository;
import com.alaska.socialis.services.EmailService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ResetPasswordEventListener implements ApplicationListener<ResetPasswordEvent> {
    @Autowired
    private ResetPasswordRepository resetPasswordRepository;

    @Autowired
    private EmailService emailservice;

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public void onApplicationEvent(ResetPasswordEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        String applicationUrl = event.getApplicationUrl() + "/reset-password?token=" + token;

        Optional<ResetPasswordModel> resetPasswordToken = this.resetPasswordRepository.findByUserId(user.getId());

        if (resetPasswordToken.isPresent()) {
            this.resetPasswordRepository.delete(resetPasswordToken.get());
        }

        this.resetPasswordRepository.save(new ResetPasswordModel(token, user));

        // send email link
        this.sendEmail(user, applicationUrl);
    }

    public void sendEmail(User user, String applicationUrl) {

        String to = user.getEmail();
        String subject = "Reset Password";

        Context context = new Context();
        context.setVariable("url", applicationUrl);
        context.setVariable("name", user.getFirstname());

        String emailContent = this.templateEngine.process("reset_password_email_template", context);

        this.emailservice.sendEmail(to, subject, emailContent);
    }

}
