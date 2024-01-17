package com.alaska.socialis.event.listener;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.alaska.socialis.event.PostLikeEvent;
import com.alaska.socialis.model.Notification;
import com.alaska.socialis.repository.NotificationRepository;
import com.alaska.socialis.services.NotificationService;
import com.alaska.socialis.utils.NotificationActivityType;
import com.alaska.socialis.utils.NotificationTargetType;

@Component
public class PostLikeEventListener implements ApplicationListener<PostLikeEvent> {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void onApplicationEvent(PostLikeEvent event) {
        Optional<Notification> notificationExist = this.notificationRepository
                .findBySourceIdAndActivityTypeAndTargetIdAndTargetType(event.getUser().getId(),
                        NotificationActivityType.LIKED, event.getPost().getId(),
                        NotificationTargetType.POST);

        if (notificationExist.isEmpty()) {
            Notification notificationObj = new Notification();
            notificationObj.setUser(event.getPost().getUser());
            notificationObj.setActivityType(NotificationActivityType.LIKED);
            notificationObj.setSource(event.getUser());
            notificationObj.setTargetId(event.getPost().getId());
            notificationObj.setTargetType(NotificationTargetType.POST);
            notificationObj.setRead(false);

            Notification newNotification = this.notificationRepository.save(notificationObj);

            this.publishAlertToClient(newNotification);
        }

    }

    public void publishAlertToClient(Notification notification) {
        this.notificationService.publishAlertToClient(notification);
    }

}
