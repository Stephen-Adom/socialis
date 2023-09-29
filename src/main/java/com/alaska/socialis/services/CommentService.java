package com.alaska.socialis.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.Comment;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.AllCommentDto;
import com.alaska.socialis.model.dto.CommentDto;
import com.alaska.socialis.model.requestModel.CommentRequest;
import com.alaska.socialis.repository.CommentRepository;
import com.alaska.socialis.repository.PostRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.CommentServiceInterface;

@Service
public class CommentService implements CommentServiceInterface {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public List<AllCommentDto> getAllComments(Long userId, Long postId) {
        List<Comment> allComments = this.commentRepository.findByUserIdAndPostId(userId, postId);

        List<AllCommentDto> parsedComments = allComments.stream().map(
                (comment) -> {
                    AllCommentDto commentDto = new AllCommentDto();
                    Map<String, String> userInfo = new HashMap<String, String>();
                    userInfo.put("username", comment.getUser().getUsername());
                    userInfo.put("image", comment.getUser().getImageUrl());
                    userInfo.put("id", comment.getUser().getId().toString());

                    commentDto.setId(comment.getId());
                    commentDto.setContent(comment.getContent());
                    commentDto.setCreatedAt(comment.getCreatedAt());
                    commentDto.setUpdatedAt(comment.getUpdatedAt());
                    commentDto.setUser(userInfo);

                    return commentDto;

                }).collect(Collectors.toList());

        return parsedComments;
    }

    @Override
    public CommentDto createComment(CommentRequest comment, BindingResult validationResult)
            throws ValidationErrorsException, EntityNotFoundException {
        if (validationResult.hasErrors()) {
            throw new ValidationErrorsException(validationResult.getFieldErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<Post> existpost = this.postRepository.findByIdAndUserId(comment.getPost_id(), comment.getUser_id());
        Optional<User> existuser = this.userRepository.findById(comment.getUser_id());

        if (existpost.isEmpty()) {
            throw new EntityNotFoundException(
                    "Post with user id " + comment.getUser_id() + " and id" + comment.getPost_id() + "not found",
                    HttpStatus.NOT_FOUND);
        }

        if (existuser.isEmpty()) {
            throw new EntityNotFoundException("User id " + comment.getUser_id() + "not found", HttpStatus.NOT_FOUND);
        }

        Comment newComment = Comment.builder().content(comment.getContent()).user(existuser.get()).post(existpost.get())
                .build();

        Comment savedComment = this.commentRepository.save(newComment);

        existpost.get().setNumberOfComments(existpost.get().getNumberOfComments() + 1);

        this.postRepository.save(existpost.get());

        return this.buildCommentDto(existuser.get(), existpost.get(), savedComment);
    }

    private CommentDto buildCommentDto(User user, Post post, Comment comment) {
        Map<String, String> userInfo = new HashMap<String, String>();
        Map<String, Object> postInfo = new HashMap<String, Object>();
        userInfo.put("username", user.getUsername());
        userInfo.put("image", user.getImageUrl());
        userInfo.put("id", user.getId().toString());

        postInfo.put("post_id", post.getId());
        postInfo.put("numberOfComments", post.getNumberOfComments());

        return CommentDto.builder().id(comment.getId()).user(userInfo).post(postInfo)
                .content(comment.getContent()).createdAt(comment.getCreatedAt()).updatedAt(comment.getUpdatedAt())
                .build();
    }
}
