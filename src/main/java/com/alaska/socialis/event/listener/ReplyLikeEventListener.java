package com.alaska.socialis.event.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.alaska.socialis.event.ReplyLikeEvent;
import com.alaska.socialis.model.Notification;
import com.alaska.socialis.repository.NotificationRepository;
import com.alaska.socialis.services.NotificationService;
import com.alaska.socialis.utils.NotificationActivityType;
import com.alaska.socialis.utils.NotificationTargetType;

@Component
public class ReplyLikeEventListener implements ApplicationListener<ReplyLikeEvent> {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void onApplicationEvent(ReplyLikeEvent event) {
        Notification notificationObj = new Notification();
        notificationObj.setUser(event.getReply().getUser());
        notificationObj.setActivityType(NotificationActivityType.LIKED);
        notificationObj.setSource(event.getUser());
        notificationObj.setTargetId(event.getReply().getId());
        notificationObj.setTargetType(NotificationTargetType.REPLY);
        notificationObj.setRead(false);

        Notification newNotification = this.notificationRepository.save(notificationObj);

        this.publishAlertToClient(newNotification);
    }

    public void publishAlertToClient(Notification notification) {
        this.notificationService.publishAlertToClient(notification);
    }
}
