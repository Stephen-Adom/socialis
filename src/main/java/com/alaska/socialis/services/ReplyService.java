package com.alaska.socialis.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.Comment;
import com.alaska.socialis.model.Reply;
import com.alaska.socialis.model.ReplyImage;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.CommentDto;
import com.alaska.socialis.model.dto.LikeDto;
import com.alaska.socialis.model.dto.ReplyDto;
import com.alaska.socialis.model.dto.SimpleUserDto;
import com.alaska.socialis.repository.BookmarkRepository;
import com.alaska.socialis.repository.CommentRepository;
import com.alaska.socialis.repository.ReplyImageRepository;
import com.alaska.socialis.repository.ReplyRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.ReplyServiceInterface;

@Service
public class ReplyService implements ReplyServiceInterface {
    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private ReplyImageRepository replyImageRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Override
    public Map<String, Object> createReply(Long userId, Long commentId, String content, MultipartFile[] multipartFiles)
            throws EntityNotFoundException {

        List<ReplyImage> allMedia = new ArrayList<ReplyImage>();
        Optional<User> author = this.userRepository.findById(userId);
        Optional<Comment> comment = this.commentRepository.findById(commentId);
        Reply replyObj = new Reply();

        if (author.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        if (comment.isEmpty()) {
            throw new EntityNotFoundException("Comment with id " + commentId + " does not exist", HttpStatus.NOT_FOUND);
        }

        if (Objects.nonNull(multipartFiles) && Arrays.asList(multipartFiles).size() > 0) {
            Arrays.asList(multipartFiles).forEach((file) -> {
                Map<String, Object> result;
                try {
                    result = this.imageUploadService.uploadImageToCloud("socialis/post/images", file);

                    ReplyImage uploadedImage = ReplyImage.builder().reply(replyObj)
                            .mediaType((String) result.get("resource_type"))
                            .mediaUrl((String) result.get("secure_url"))
                            .build();

                    allMedia.add(uploadedImage);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            });
        }

        String uid = "rpl-" + UUID.randomUUID().toString();
        comment.get().setNumberOfReplies(comment.get().getNumberOfReplies() + 1);

        replyObj.setUid(uid);
        replyObj.setUser(author.get());
        replyObj.setContent(Objects.nonNull(content) ? content : "");
        replyObj.setReplyImages(allMedia);
        replyObj.setComment(comment.get());

        Reply result = this.replyRepository.save(replyObj);

        ReplyDto replyDto = this.buildReplyDto(result);
        CommentDto commentDto = this.commentService.buildCommentDto(replyObj.getComment());

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("commentDto", commentDto);
        response.put("replyDto", replyDto);

        return response;
    }

    public List<ReplyDto> fetchAllReplies(Long commentId) {
        List<Reply> allReplies = this.replyRepository.findByCommentIdOrderByCreatedAtDesc(commentId);
        return allReplies.stream().map((reply) -> this.buildReplyDto(reply)).collect(Collectors.toList());
    }

    @Override
    public ReplyDto editReply(Long id, String content, MultipartFile[] multipartFiles) throws EntityNotFoundException {
        Optional<Reply> replyExist = this.replyRepository.findById(id);

        if (replyExist.isEmpty()) {
            throw new EntityNotFoundException("Reply with id " + replyExist.get().getId() + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        Reply existingReply = replyExist.get();

        if (Objects.nonNull(multipartFiles) && multipartFiles.length > 0) {
            List<ReplyImage> allMedia = Arrays.stream(multipartFiles).map((file) -> {
                try {
                    Map<String, Object> result = this.imageUploadService.uploadImageToCloud("socialis/post/images",
                            file);

                    return ReplyImage.builder().reply(existingReply)
                            .mediaType((String) result.get("resource_type"))
                            .mediaUrl((String) result.get("secure_url"))
                            .build();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());

            existingReply.setReplyImages(allMedia);
        }

        if (content != null) {
            existingReply.setContent(content);
        }

        this.replyRepository.save(existingReply);

        Optional<Reply> updatedReply = this.replyRepository.findById(id);

        List<ReplyImage> updatedImages = this.replyImageRepository.findAllByReplyId(id);
        updatedReply.get().setReplyImages(updatedImages);

        return this.buildReplyDto(updatedReply.get());
    }

    @Override
    @Transactional
    public void deleteReply(Long id) throws EntityNotFoundException {
        Optional<Reply> existReply = this.replyRepository.findById(id);

        if (existReply.isEmpty()) {
            throw new EntityNotFoundException("Reply with id " + id + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        this.deleteAllReplyImages(existReply.get());

        Comment comment = existReply.get().getComment();

        comment.setNumberOfReplies(comment.getNumberOfReplies() - 1);

        Comment updatedComment = this.commentRepository.save(comment);

        this.replyRepository.deleteById(id);

        this.messagingTemplate.convertAndSend("/feed/comment/update",
                this.commentService.buildCommentDto(updatedComment));
    }

    private void deleteAllReplyImages(Reply reply) {
        List<String> images = new ArrayList<String>();

        images.addAll(
                reply.getReplyImages().stream().map(ReplyImage::getMediaUrl).collect(Collectors.toList()));

        if (images.size() > 0) {
            images.stream().forEach((imageUrl) -> {
                this.imageUploadService.deleteUploadedImage("socialis/post/images/",
                        imageUrl);
            });
        }

    }

    public ReplyDto buildReplyDto(Reply reply) {

        List<Long> userIds = bookmarkRepository.findAllByContentIdAndContentType(reply.getId(), "reply").stream()
                .map((bookmark) -> bookmark.getUser().getId()).filter(Objects::nonNull).collect(Collectors.toList());

        List<LikeDto> likes = reply.getLikes().stream().map((like) -> {
            LikeDto currentLike = new LikeDto();
            currentLike.setImageUrl(like.getUser().getImageUrl());
            currentLike.setUsername(like.getUser().getUsername());
            currentLike.setFirstname(like.getUser().getFirstname());
            currentLike.setLastname(like.getUser().getLastname());

            return currentLike;
        }).collect(Collectors.toList());

        SimpleUserDto userInfo = SimpleUserDto.builder().id(reply.getUser().getId())
                .firstname(reply.getUser().getFirstname()).lastname(reply.getUser().getLastname())
                .username(reply.getUser().getUsername()).imageUrl(reply.getUser().getImageUrl())
                .bio(reply.getUser().getBio()).build();

        return ReplyDto.builder().id(reply.getId()).user(userInfo)
                .content(reply.getContent()).replyImages(reply.getReplyImages())
                .numberOfLikes(reply.getNumberOfLikes()).numberOfBookmarks(reply.getNumberOfBookmarks()).likes(likes)
                .bookmarkedUsers(userIds)
                .createdAt(reply.getCreatedAt()).updatedAt(reply.getUpdatedAt())
                .build();
    }

}
