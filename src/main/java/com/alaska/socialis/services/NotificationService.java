package com.alaska.socialis.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.OffsetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.support.WindowIterator;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.Comment;
import com.alaska.socialis.model.Notification;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.Reply;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.NotificationDto;
import com.alaska.socialis.model.dto.UserSummary2Dto;
import com.alaska.socialis.repository.CommentRepository;
import com.alaska.socialis.repository.NotificationRepository;
import com.alaska.socialis.repository.PostRepository;
import com.alaska.socialis.repository.ReplyRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.NotificationServiceInterface;
import com.alaska.socialis.utils.NotificationActivityType;
import com.alaska.socialis.utils.NotificationTargetType;

@Service
public class NotificationService implements NotificationServiceInterface {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private static final String USER_NOTIFICATION_PATH = "/feed/notification/user";

    private static final String USER_UNREAD_NOTIFICATION_COUNT_PATH = "/feed/notification/count/user";

    @Override
    public Long getUserUnreadNotificationCount(Long userId) throws EntityNotFoundException {
        Optional<User> user = this.userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " not found", HttpStatus.NOT_FOUND);
        }

        return this.notificationRepository.countAllByUserIdUnreadTrue(userId);
    }

    @Override
    public List<NotificationDto> fetchAllUserNotification(Long userId) throws EntityNotFoundException {
        Optional<User> user = this.userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " not found", HttpStatus.NOT_FOUND);
        }

        WindowIterator<Notification> notifications = WindowIterator.of(position -> this.notificationRepository
                .findFirst30ByUserIdOrderByCreatedAtDesc(userId, (OffsetScrollPosition) position))
                .startingAt(ScrollPosition.offset());

        List<NotificationDto> notificationDtos = new ArrayList<>();

        notifications.forEachRemaining(notification -> {
            NotificationDto notificationDto = buildNotificationDto(notification);
            notificationDtos.add(notificationDto);
        });

        return notificationDtos;
    }

    public NotificationDto buildNotificationDto(Notification notification) {
        NotificationDto newNotificationDto = new NotificationDto();
        newNotificationDto.setId(notification.getId());
        // newNotificationDto.setUser(buildUserInfo(notification.getUser()));
        newNotificationDto.setSource(buildUserInfo(notification.getSource()));
        newNotificationDto.setRead(notification.getRead());
        newNotificationDto.setReadAt(notification.getReadAt());
        newNotificationDto.setCreatedAt(notification.getCreatedAt());
        newNotificationDto.setActivityType(getActivityType(notification));
        newNotificationDto.setTarget(getTargetObject(notification));
        newNotificationDto.setTargetType(getTargetType(notification));

        return newNotificationDto;

    }

    private String getActivityType(Notification notification) {

        switch (notification.getActivityType()) {
            case LIKED:
                return NotificationActivityType.LIKED.getValue();
            case COMMENTED:

                return NotificationActivityType.COMMENTED.getValue();
            case REPLY:

                return NotificationActivityType.REPLY.getValue();
            case FOLLOWS:

                return NotificationActivityType.FOLLOWS.getValue();

            case MENTION:

                return NotificationActivityType.MENTION.getValue();
            case MESSAGE:

                return NotificationActivityType.MESSAGE.getValue();

            default:
                return "";
        }
    }

    private Map<String, Object> getTargetObject(Notification notification) {
        Map<String, Object> targetObj = new HashMap<String, Object>();

        switch (notification.getTargetType()) {
            case POST:
                Post post = this.postRepository.findById(notification.getTargetId()).get();
                targetObj.put("targetUid", post.getUid());
                targetObj.put("targetContent", post.getContent());
                targetObj.put("targetImage", post.getPostImages().size() > 0 ? post.getPostImages().get(0) : "");
                targetObj.put("targetUrl", post.getUser().getUsername() + "/details/" + post.getUid());

                break;
            case COMMENT:
                Comment comment = this.commentRepository.findById(notification.getTargetId()).get();
                targetObj.put("targetUid", comment.getUid());
                targetObj.put("targetContent", comment.getContent());
                targetObj.put("targetImage",
                        comment.getCommentImages().size() > 0 ? comment.getCommentImages().get(0) : "");
                targetObj.put("targetUrl",
                        comment.getPost().getUser().getUsername() + "/details/" + comment.getPost().getUid() + "/"
                                + comment.getUser().getUsername() + "/details/" + comment.getUid());

                break;
            case REPLY:

                Reply reply = this.replyRepository.findById(notification.getTargetId()).get();
                targetObj.put("targetUid", reply.getUid());
                targetObj.put("targetContent", reply.getContent());
                targetObj.put("targetImage", reply.getReplyImages().size() > 0 ? reply.getReplyImages().get(0) : "");

                targetObj.put("targetUrl", reply.getComment().getPost().getUser().getUsername() + "/details/"
                        + reply.getComment().getPost().getUid() + "/" + reply.getComment().getUser().getUsername()
                        + "/details/" + reply.getComment().getUid());

                break;
            case USER:

                User user = this.userRepository.findById(notification.getTargetId()).get();
                targetObj.put("targetUid", user.getUid());
                targetObj.put("targetFirstname", user.getFirstname());
                targetObj.put("targetLastname", user.getLastname());
                targetObj.put("targetUsername", user.getUsername());
                targetObj.put("targetImage", user.getImageUrl());
                targetObj.put("targetUrl", "user/" + user.getUsername() + "/profile");

                break;

            default:
                break;
        }

        return targetObj;
    }

    private String getTargetType(Notification notification) {
        switch (notification.getTargetType()) {
            case POST:
                return NotificationTargetType.POST.getValue();
            case COMMENT:
                return NotificationTargetType.COMMENT.getValue();
            case REPLY:
                return NotificationTargetType.REPLY.getValue();
            case USER:
                return NotificationTargetType.USER.getValue();
            default:
                return "";
        }
    }

    private UserSummary2Dto buildUserInfo(User user) {
        UserSummary2Dto userInfo = new UserSummary2Dto();
        userInfo.setId(user.getId());
        userInfo.setUid(user.getUid());
        userInfo.setFirstname(user.getFirstname());
        userInfo.setLastname(user.getLastname());
        userInfo.setUsername(user.getUsername());
        userInfo.setImageUrl(user.getImageUrl());

        return userInfo;
    }

    @Override
    public void markNotificationAsRead(Long notificationId) throws EntityNotFoundException {
        Optional<Notification> notificationExist = this.notificationRepository.findById(notificationId);

        if (notificationExist.isEmpty()) {
            throw new EntityNotFoundException("Notification with id " + notificationId + " not found",
                    HttpStatus.NOT_FOUND);
        }

        notificationExist.get().setRead(true);

        this.notificationRepository.save(notificationExist.get());
    }

    public void publishAlertToClient(Notification notification) {
        Long unreadCount = this.notificationRepository.countAllByUserIdUnreadTrue(notification.getUser().getId());
        NotificationDto notificationDto = this.buildNotificationDto(notification);

        messagingTemplate.convertAndSend(USER_NOTIFICATION_PATH + "-" + notification.getUser().getUsername(),
                notificationDto);

        messagingTemplate.convertAndSend(
                USER_UNREAD_NOTIFICATION_COUNT_PATH + "-" + notification.getUser().getUsername(),
                unreadCount);
    }

    @Override
    public List<NotificationDto> markAllNotificationAsRead(Long userId) throws EntityNotFoundException {
        Optional<User> user = this.userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " not found", HttpStatus.NOT_FOUND);
        }

        List<Notification> allUnreadNotifications = this.notificationRepository.allUnreadNotifications(userId);

        if (allUnreadNotifications.size() > 0) {
            allUnreadNotifications.stream().forEach(notification -> {
                notification.setRead(true);
                this.notificationRepository.save(notification);
            });
        }

        return this.fetchAllUserNotification(userId);

    }

}
