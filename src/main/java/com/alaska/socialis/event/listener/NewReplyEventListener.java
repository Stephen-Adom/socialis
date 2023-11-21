package com.alaska.socialis.event.listener;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.alaska.socialis.event.NewReplyEvent;
import com.alaska.socialis.event.UserMentionInReplyEvent;
import com.alaska.socialis.model.Notification;
import com.alaska.socialis.model.Reply;
import com.alaska.socialis.model.User;
import com.alaska.socialis.repository.NotificationRepository;
import com.alaska.socialis.services.ActivityService;
import com.alaska.socialis.utils.NotificationActivityType;
import com.alaska.socialis.utils.NotificationTargetType;

@Component
public class NewReplyEventListener implements ApplicationListener<NewReplyEvent> {
    @Autowired
    private ActivityService activityService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void onApplicationEvent(NewReplyEvent event) {
        this.checkMentionsInPost(event.getReply());
        this.createAndSendNotification(event);
        // ! save an activity
    }

    public void checkMentionsInPost(Reply reply) {
        List<String> mentions = this.activityService.extractUsernameFromPost(reply.getContent());

        if (mentions.size() > 0) {
            this.eventPublisher
                    .publishEvent(new UserMentionInReplyEvent(mentions, NotificationActivityType.MENTION, reply));
        }
    }

    public void createAndSendNotification(NewReplyEvent event) {
        User user = event.getReply().getComment().getUser();
        User source = event.getReply().getUser();

        Notification notificationObj = new Notification();
        notificationObj.setUser(user);
        notificationObj.setActivityType(NotificationActivityType.REPLY);
        notificationObj.setSource(source);
        notificationObj.setTargetId(event.getReply().getId());
        notificationObj.setTargetType(NotificationTargetType.REPLY);
        notificationObj.setRead(false);

        Notification newNotification = this.notificationRepository.save(notificationObj);
    }
}
