package com.alaska.socialis.event.listener;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.alaska.socialis.event.UserMentionInReplyEvent;
import com.alaska.socialis.model.Notification;
import com.alaska.socialis.model.Reply;
import com.alaska.socialis.model.User;
import com.alaska.socialis.repository.NotificationRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.utils.NotificationActivityType;
import com.alaska.socialis.utils.NotificationTargetType;

@Component
public class UserMentionInReplyEventListener implements ApplicationListener<UserMentionInReplyEvent> {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void onApplicationEvent(UserMentionInReplyEvent event) {
        List<String> mentions = event.getMentions();
        NotificationActivityType activityType = event.getActivityType();
        Reply reply = event.getReply();

        for (String mention : mentions) {
            User user = (User) userRepository.findByUsername(mention);

            if (Objects.nonNull(user)) {
                Notification notificationObj = new Notification();
                notificationObj.setUser(user);
                notificationObj.setActivityType(activityType);
                notificationObj.setSource(reply.getUser());
                notificationObj.setTargetId(reply.getId());
                notificationObj.setTargetType(NotificationTargetType.REPLY);
                notificationObj.setRead(false);

                Notification newNotification = this.notificationRepository.save(notificationObj);
            }
        }
    }
}
