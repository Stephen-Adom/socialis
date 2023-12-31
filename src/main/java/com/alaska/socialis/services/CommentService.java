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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.event.NewCommentEvent;
import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.Comment;
import com.alaska.socialis.model.CommentImages;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.ReplyImage;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.CommentDto;
import com.alaska.socialis.model.dto.LikeDto;
import com.alaska.socialis.model.dto.PostDto;
import com.alaska.socialis.model.dto.SimpleUserDto;
import com.alaska.socialis.repository.BookmarkRepository;
import com.alaska.socialis.repository.CommentImageRepository;
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

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private CommentImageRepository commentImageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public List<CommentDto> getAllComments(Long postId) {
        List<Comment> allComments = this.commentRepository.findByPostIdOrderByCreatedAtDesc(postId);

        List<CommentDto> parsedComments = allComments.stream()
                .map((comment) -> this.buildCommentDto(comment)).collect(Collectors.toList());
        return parsedComments;
    }

    @Override
    public List<CommentDto> fetchAllCommentByUser(Long userId) throws EntityNotFoundException {
        Optional<User> user = this.userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        List<Comment> allComments = this.commentRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        List<CommentDto> allCommentDto = allComments.stream().map(comment -> this.buildCommentDto(comment))
                .collect(Collectors.toList());

        return allCommentDto;
    }

    @Override
    public Map<String, Object> createComment(Long userId, Long postId, String content, MultipartFile[] multipartFiles)
            throws EntityNotFoundException {
        Optional<Post> existpost = this.postRepository.findById(postId);
        Optional<User> existuser = this.userRepository.findById(userId);
        Comment commentObj = new Comment();

        if (existuser.isEmpty()) {
            throw new EntityNotFoundException("User id " + userId + "not found",
                    HttpStatus.NOT_FOUND);
        }

        if (existpost.isEmpty()) {
            throw new EntityNotFoundException(
                    "Post with id " + postId + " not found",
                    HttpStatus.NOT_FOUND);
        }

        if (Objects.nonNull(multipartFiles) && multipartFiles.length > 0) {
            List<CommentImages> allMedia = Arrays.stream(multipartFiles).map((file) -> {
                try {
                    String filePath = file.getContentType().contains("image") ? "socialis/post/images"
                            : "socialis/post/videos";

                    String resourceType = file.getContentType().contains("image") ? "image" : "video";

                    Map<String, Object> result = this.imageUploadService.uploadImageToCloud(filePath,
                            file, resourceType);

                    return CommentImages.builder().comment(commentObj)
                            .mediaType((String) result.get("resource_type"))
                            .mediaUrl((String) result.get("secure_url"))
                            .build();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());

            commentObj.setCommentImages(allMedia);
        }

        String uid = "cmt-" + UUID.randomUUID().toString();

        if (content != null) {
            commentObj.setContent(content);
        }

        commentObj.setUid(uid);
        commentObj.setUser(existuser.get());
        existpost.get().setNumberOfComments(existpost.get().getNumberOfComments() + 1);
        commentObj.setPost(existpost.get());

        Comment savedComment = this.commentRepository.save(commentObj);

        CommentDto commentDto = this.buildCommentDto(savedComment);

        PostDto postDto = this.postService.buildPostDto(savedComment.getPost());

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("commentDto", commentDto);
        response.put("postDto", postDto);

        this.eventPublisher.publishEvent(new NewCommentEvent(savedComment));

        return response;
    }

    @Override
    public Comment editComment(Long id, String content, MultipartFile[] multipartFiles) throws EntityNotFoundException {
        Optional<Comment> commentExist = this.commentRepository.findById(id);

        if (commentExist.isEmpty()) {
            throw new EntityNotFoundException("Comment with id " + commentExist.get().getId() + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        Comment existingComment = commentExist.get();

        if (Objects.nonNull(multipartFiles) && multipartFiles.length > 0) {
            List<CommentImages> allMedia = Arrays.stream(multipartFiles).map((file) -> {
                try {
                    String filePath = file.getContentType().contains("image") ? "socialis/post/images"
                            : "socialis/post/videos";

                    String resourceType = file.getContentType().contains("image") ? "image" : "video";

                    Map<String, Object> result = this.imageUploadService.uploadImageToCloud(filePath,
                            file, resourceType);

                    return CommentImages.builder().comment(existingComment)
                            .mediaType((String) result.get("resource_type"))
                            .mediaUrl((String) result.get("secure_url"))
                            .build();

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());

            existingComment.setCommentImages(allMedia);
        }

        if (content != null) {
            existingComment.setContent(content);
        }

        this.commentRepository.save(existingComment);

        Optional<Comment> updatedComment = this.commentRepository.findById(id);

        List<CommentImages> updatedImages = this.commentImageRepository.findAllByCommentId(id);
        updatedComment.get().setCommentImages(updatedImages);

        return updatedComment.get();
    }

    @Override
    @Transactional
    public void deleteComment(Long id) throws EntityNotFoundException {
        Optional<Comment> existComment = this.commentRepository.findById(id);

        if (existComment.isEmpty()) {
            throw new EntityNotFoundException("Comment with id " + id + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        this.deleteAllCommentImages(existComment.get());

        Post post = existComment.get().getPost();

        post.setNumberOfComments(post.getNumberOfComments() - 1);

        Post updatedPost = this.postRepository.save(post);

        this.commentRepository.deleteById(id);

        this.messagingTemplate.convertAndSend("/feed/post/update", this.postService.buildPostDto(updatedPost));
    }

    private void deleteAllCommentImages(Comment comment) {
        List<String> images = new ArrayList<String>();

        images.addAll(
                comment.getCommentImages().stream().map(CommentImages::getMediaUrl).collect(Collectors.toList()));

        comment.getReplies().stream().forEach((reply) -> {
            images.addAll(reply.getReplyImages().stream().map(ReplyImage::getMediaUrl).collect(Collectors.toList()));
        });

        if (images.size() > 0) {
            images.stream().forEach((imageUrl) -> {
                this.imageUploadService.deleteUploadedImage("socialis/post/images/",
                        imageUrl);
            });
        }

    }

    @Override
    public CommentDto fetchCommentById(String commentId) throws EntityNotFoundException {
        Optional<Comment> comment = this.commentRepository.findByUid(commentId);

        if (comment.isEmpty()) {
            throw new EntityNotFoundException("Comment with id " + comment + " does not exist", HttpStatus.NOT_FOUND);
        }

        return this.buildCommentDto(comment.get());
    }

    public CommentDto buildCommentDto(Comment comment) {
        List<Long> userIds = bookmarkRepository.findAllByContentIdAndContentType(comment.getId(), "comment").stream()
                .map((bookmark) -> bookmark.getUser().getId()).filter(Objects::nonNull).collect(Collectors.toList());

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
                .username(comment.getUser().getUsername()).imageUrl(comment.getUser().getImageUrl())
                .bio(comment.getUser().getBio()).build();

        return CommentDto.builder().id(comment.getId()).uid(comment.getUid()).user(userInfo)
                .content(comment.getContent()).commentImages(comment.getCommentImages())
                .numberOfLikes(comment.getNumberOfLikes()).numberOfReplies(comment.getNumberOfReplies())
                .numberOfBookmarks(comment.getNumberOfBookmarks()).likes(likes).bookmarkedUsers(userIds)
                .createdAt(comment.getCreatedAt()).updatedAt(comment.getUpdatedAt())
                .build();
    }
}
