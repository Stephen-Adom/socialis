package com.alaska.socialis.event.listener;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.alaska.socialis.event.ResetPasswordEvent;
import com.alaska.socialis.model.ResetPasswordModel;
import com.alaska.socialis.model.User;
import com.alaska.socialis.repository.ResetPasswordRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ResetPasswordEventListener implements ApplicationListener<ResetPasswordEvent> {
    @Autowired
    private ResetPasswordRepository resetPasswordRepository;

    @Override
    public void onApplicationEvent(ResetPasswordEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        String applicationUrl = event.getApplicationUrl() + "/resetPassword?token=" + token;

        this.resetPasswordRepository.save(new ResetPasswordModel(token, user));

        // send email link
        log.info(applicationUrl);
    }

}
