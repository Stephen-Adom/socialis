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
import com.alaska.socialis.model.CommentImages;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.CommentDto;
import com.alaska.socialis.model.dto.LikeDto;
import com.alaska.socialis.model.dto.PostDto;
import com.alaska.socialis.model.dto.SimpleUserDto;
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

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private PostService postService;

    @Override
    public List<CommentDto> getAllComments(Long postId) {
        // Optional<Post> post = this.postRepository.findById(postId);
        List<Comment> allComments = this.commentRepository.findByPostIdOrderByCreatedAtDesc(postId);

        List<CommentDto> parsedComments = allComments.stream()
                .map((comment) -> this.buildCommentDto(comment)).collect(Collectors.toList());
        return parsedComments;
    }

    @Override
    public Map<String, Object> createComment(Long userId, Long postId, String content, MultipartFile[] multipartFiles)
            throws EntityNotFoundException {
        List<CommentImages> allMedia = new ArrayList<CommentImages>();
        Optional<Post> existpost = this.postRepository.findByIdAndUserId(postId, userId);
        Optional<User> existuser = this.userRepository.findById(userId);
        Comment commentObj = new Comment();

        if (existuser.isEmpty()) {
            throw new EntityNotFoundException("User id " + userId + "not found", HttpStatus.NOT_FOUND);
        }

        if (existpost.isEmpty()) {
            throw new EntityNotFoundException(
                    "Post with user id " + userId + " and id" + postId + "not found",
                    HttpStatus.NOT_FOUND);
        }

        if (Objects.nonNull(multipartFiles) && Arrays.asList(multipartFiles).size() > 0) {
            Arrays.asList(multipartFiles).forEach((file) -> {
                Map<String, Object> result;
                try {
                    result = this.imageUploadService.uploadImageToCloud("socialis/comments/images", file);

                    CommentImages uploadedImage = CommentImages.builder().comment(commentObj)
                            .mediaType((String) result.get("resource_type"))
                            .mediaUrl((String) result.get("secure_url"))
                            .build();

                    allMedia.add(uploadedImage);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            });
        }

        commentObj.setContent(Objects.nonNull(content) ? content : "");
        commentObj.setUser(existuser.get());
        commentObj.setPost(existpost.get());
        commentObj.setCommentImages(allMedia);

        Comment savedComment = this.commentRepository.save(commentObj);

        existpost.get().setNumberOfComments(existpost.get().getNumberOfComments() +
                1);

        Post updatedPost = this.postRepository.save(existpost.get());

        CommentDto commentDto = this.buildCommentDto(savedComment);

        PostDto postDto = this.postService.buildPostDto(updatedPost);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("commentDto", commentDto);
        response.put("postDto", postDto);

        return response;
    }

    public CommentDto buildCommentDto(Comment comment) {
        List<LikeDto> likes = comment.getLikes().stream().map((like) -> {
            LikeDto currentLike = new LikeDto();
            currentLike.setImageUrl(like.getUser().getImageUrl());
            currentLike.setUsername(like.getUser().getUsername());
            currentLike.setFirstname(like.getUser().getFirstname());
            currentLike.setLastname(like.getUser().getLastname());

            return currentLike;
        }).collect(Collectors.toList());

        SimpleUserDto userInfo = SimpleUserDto.builder().id(comment.getUser().getId())
                .firstname(comment.getUser().getFirstname()).lastname(comment.getUser().getLastname())
                .username(comment.getUser().getUsername()).imageUrl(comment.getUser().getImageUrl()).build();

        return CommentDto.builder().id(comment.getId()).user(userInfo)
                .content(comment.getContent()).commentImages(comment.getCommentImages())
                .numberOfLikes(comment.getNumberOfLikes()).numberOfReplies(comment.getNumberOfReplies()).likes(likes)
                .createdAt(comment.getCreatedAt()).updatedAt(comment.getUpdatedAt())
                .build();
    }
}
