package com.alaska.socialis.services;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @Override
    public List<NotificationDto> fetchAllUserNotification(Long userId) throws EntityNotFoundException {
        Optional<User> user = this.userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " not found", HttpStatus.NOT_FOUND);
        }

        List<Notification> notifications = this.notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        List<NotificationDto> notificationDtos = notifications.stream()
                .map(notification -> buildNotificationDto(notification)).collect(Collectors.toList());

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

                break;
            case COMMENT:
                Comment comment = this.commentRepository.findById(notification.getTargetId()).get();
                targetObj.put("targetUid", comment.getUid());
                targetObj.put("targetContent", comment.getContent());
                targetObj.put("targetImage",
                        comment.getCommentImages().size() > 0 ? comment.getCommentImages().get(0) : "");

                break;
            case REPLY:

                Reply reply = this.replyRepository.findById(notification.getTargetId()).get();
                targetObj.put("targetUid", reply.getUid());
                targetObj.put("targetContent", reply.getContent());
                targetObj.put("targetImage", reply.getReplyImages().size() > 0 ? reply.getReplyImages().get(0) : "");

                break;
            case USER:

                User user = this.userRepository.findById(notification.getTargetId()).get();
                targetObj.put("targetUid", user.getUid());
                targetObj.put("targetFirstname", user.getFirstname());
                targetObj.put("targetLastname", user.getLastname());
                targetObj.put("targetUsername", user.getUsername());
                targetObj.put("targetImage", user.getImageUrl());

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

}
