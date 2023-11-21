package com.alaska.socialis.event.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.alaska.socialis.event.CommentLikeEvent;
import com.alaska.socialis.model.Notification;
import com.alaska.socialis.repository.NotificationRepository;
import com.alaska.socialis.utils.NotificationActivityType;
import com.alaska.socialis.utils.NotificationTargetType;

@Component
public class CommentLikeEventListener implements ApplicationListener<CommentLikeEvent> {
    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void onApplicationEvent(CommentLikeEvent event) {
        Notification notificationObj = new Notification();
        notificationObj.setUser(event.getComment().getUser());
        notificationObj.setActivityType(NotificationActivityType.LIKED);
        notificationObj.setSource(event.getUser());
        notificationObj.setTargetId(event.getComment().getId());
        notificationObj.setTargetType(NotificationTargetType.COMMENT);
        notificationObj.setRead(false);

        Notification newNotification = this.notificationRepository.save(notificationObj);
    }

}
