package com.alaska.socialis.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.Comment;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.PostImage;
import com.alaska.socialis.model.Reply;
import com.alaska.socialis.model.ReplyImage;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.CommentDto;
import com.alaska.socialis.model.dto.ReplyDto;
import com.alaska.socialis.model.dto.SimpleUserDto;
import com.alaska.socialis.repository.CommentRepository;
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
                    result = this.imageUploadService.uploadImageToCloud("socialis/replies/images", file);

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

        replyObj.setUser(author.get());
        replyObj.setContent(Objects.nonNull(content) ? content : "");
        replyObj.setReplyImages(allMedia);
        replyObj.setComment(comment.get());
        ;

        Reply result = this.replyRepository.save(replyObj);

        // update number of replies
        comment.get().setNumberOfReplies(comment.get().getNumberOfReplies() + 1);

        Comment updatedComment = this.commentRepository.save(comment.get());

        ReplyDto replyDto = this.buildReplyDto(result);
        CommentDto commentDto = this.buildCommentDto(updatedComment);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("commentDto", commentDto);
        response.put("replyDto", replyDto);

        return response;
    }

    public List<ReplyDto> fetchAllReplies(Long commentId) {
        List<Reply> allReplies = this.replyRepository.findByCommentIdOrderByCreatedAtDesc(commentId);
        return allReplies.stream().map((reply) -> this.buildReplyDto(reply)).collect(Collectors.toList());
    }

    private ReplyDto buildReplyDto(Reply reply) {
        SimpleUserDto userInfo = SimpleUserDto.builder().id(reply.getUser().getId())
                .firstname(reply.getUser().getFirstname()).lastname(reply.getUser().getLastname())
                .username(reply.getUser().getUsername()).imageUrl(reply.getUser().getImageUrl()).build();

        return ReplyDto.builder().id(reply.getId()).user(userInfo)
                .content(reply.getContent()).replyImages(reply.getReplyImages())
                .numberOfLikes(reply.getNumberOfLikes())
                .createdAt(reply.getCreatedAt()).updatedAt(reply.getUpdatedAt())
                .build();
    }

    private CommentDto buildCommentDto(Comment comment) {
        SimpleUserDto userInfo = SimpleUserDto.builder().id(comment.getUser().getId())
                .firstname(comment.getUser().getFirstname()).lastname(comment.getUser().getLastname())
                .username(comment.getUser().getUsername()).imageUrl(comment.getUser().getImageUrl()).build();

        return CommentDto.builder().id(comment.getId()).user(userInfo)
                .content(comment.getContent()).commentImages(comment.getCommentImages())
                .numberOfLikes(comment.getNumberOfLikes()).numberOfReplies(comment.getNumberOfReplies())
                .createdAt(comment.getCreatedAt()).updatedAt(comment.getUpdatedAt())
                .build();
    }

}