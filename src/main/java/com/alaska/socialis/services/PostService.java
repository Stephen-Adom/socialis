package com.alaska.socialis.services;

import java.util.Map;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

import com.alaska.socialis.event.NewPostEvent;
import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.CommentImages;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.PostImage;
import com.alaska.socialis.model.ReplyImage;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.CommentDto;
import com.alaska.socialis.model.dto.LikeDto;
import com.alaska.socialis.model.dto.PostDto;
import com.alaska.socialis.model.dto.SimpleUserDto;
import com.alaska.socialis.repository.BookmarkRepository;
import com.alaska.socialis.repository.CommentRepository;
import com.alaska.socialis.repository.PostImageRepository;
import com.alaska.socialis.repository.PostRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.PostServiceInterface;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PostService implements PostServiceInterface {

    private static final String NEW_LIVE_POST_FEED_URL = "/feed/post/new";

    private static final String UPDATE_LIVE_POST_FEED_URL = "/feed/post/update";

    private static final String UPDATE_LIVE_USER_PATH = "/feed/user/update";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public List<PostDto> fetchAllPost() {
        List<Post> allPost = this.postRepository.findAllByOrderByCreatedAtDesc();

        return this.buildPostDto(allPost);
    }

    @Override
    public List<PostDto> fetchAllPostsByUser(Long userId) throws EntityNotFoundException {
        Optional<User> user = this.userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        List<Post> allPosts = this.postRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        return this.buildPostDto(allPosts);
    }

    @Override
    public void createPost(Long userId, String content, MultipartFile[] multipartFiles) throws EntityNotFoundException {

        Optional<User> author = this.userRepository.findById(userId);
        Post postObj = new Post();

        if (author.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        if (Objects.nonNull(multipartFiles) && multipartFiles.length > 0) {

            List<PostImage> allMedia = Arrays.stream(multipartFiles).map((file) -> {
                try {
                    Map<String, Object> result = this.imageUploadService.uploadImageToCloud("socialis/post/images",
                            file);

                    return PostImage.builder().post(postObj)
                            .mediaType((String) result.get("resource_type"))
                            .mediaUrl((String) result.get("secure_url"))
                            .build();

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());

            postObj.setPostImages(allMedia);
        }

        String uid = "post-" + UUID.randomUUID().toString();
        author.get().setNoOfPosts(author.get().getNoOfPosts() + 1);
        postObj.setUid(uid);
        postObj.setUser(author.get());
        postObj.setContent(Objects.nonNull(content) ? content : "");

        Post updatedPost = this.postRepository.save(postObj);
        Optional<User> updatedUser = this.userRepository.findById(author.get().getId());

        this.eventPublisher.publishEvent(new NewPostEvent(updatedPost));

        messagingTemplate.convertAndSend(NEW_LIVE_POST_FEED_URL,
                this.buildPostDto(updatedPost));
        messagingTemplate.convertAndSend(UPDATE_LIVE_USER_PATH + "-" +
                updatedUser.get().getUsername(),
                this.userService.buildDto(updatedUser.get()));
    }

    @Override
    public PostDto fetchPostById(String postId) throws EntityNotFoundException {
        Optional<Post> post = this.postRepository.findByUid(postId);

        if (post.isEmpty()) {
            throw new EntityNotFoundException("Post with id " + postId + " does not exist", HttpStatus.NOT_FOUND);
        }

        return this.buildPostDto(post.get());
    }

    @Override
    public void editPost(Long id, String content, MultipartFile[] multipartFiles) throws EntityNotFoundException {
        Optional<Post> postExist = this.postRepository.findById(id);

        if (postExist.isEmpty()) {
            throw new EntityNotFoundException("Post with id " + postExist.get().getId() + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        Post existingPost = postExist.get();

        if (Objects.nonNull(multipartFiles) && multipartFiles.length > 0) {
            List<PostImage> allMedia = Arrays.stream(multipartFiles).map((file) -> {
                try {
                    Map<String, Object> result = this.imageUploadService.uploadImageToCloud("socialis/post/images",
                            file);

                    return PostImage.builder().post(existingPost)
                            .mediaType((String) result.get("resource_type"))
                            .mediaUrl((String) result.get("secure_url"))
                            .build();

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());

            existingPost.setPostImages(allMedia);
        }

        if (content != null) {
            existingPost.setContent(content);
        }

        this.postRepository.save(existingPost);

        Optional<Post> updatedPost = this.postRepository.findById(id);

        List<PostImage> updatedImages = this.postImageRepository.findAllByPostId(id);

        updatedPost.get().setPostImages(updatedImages);

        messagingTemplate.convertAndSend(UPDATE_LIVE_POST_FEED_URL, this.buildPostDto(updatedPost.get()));
    }

    @Override
    @Transactional
    public void deletePost(Long id) throws EntityNotFoundException {
        Optional<Post> existPost = this.postRepository.findById(id);

        if (existPost.isEmpty()) {
            throw new EntityNotFoundException("Post with id " + id + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        this.deleteAllPostImages(existPost.get());

        User user = existPost.get().getUser();
        user.setNoOfPosts(user.getNoOfPosts() - 1);

        User updatedUser = this.userRepository.save(user);

        this.postRepository.deleteById(id);

        messagingTemplate.convertAndSend(UPDATE_LIVE_USER_PATH + "-" + updatedUser.getUsername(),
                this.userService.buildDto(updatedUser));
    }

    private void deleteAllPostImages(Post post) {
        List<String> images = new ArrayList<String>();

        images.addAll(
                post.getPostImages().stream().map(PostImage::getMediaUrl).collect(Collectors.toList()));

        post.getComments().forEach((comment) -> {
            images.addAll(
                    comment.getCommentImages().stream().map(CommentImages::getMediaUrl).collect(Collectors.toList()));

            comment.getReplies().stream().forEach((reply) -> {
                images.addAll(
                        reply.getReplyImages().stream().map(ReplyImage::getMediaUrl).collect(Collectors.toList()));
            });
        });

        if (images.size() > 0) {
            images.stream().forEach((imageUrl) -> {
                this.imageUploadService.deleteUploadedImage("socialis/post/images/",
                        imageUrl);
            });
        }

    }

    public PostDto buildPostDto(Post post) {

        List<Long> userIds = bookmarkRepository.findAllByContentIdAndContentType(post.getId(), "post").stream()
                .map((bookmark) -> bookmark.getUser().getId()).filter(Objects::nonNull).collect(Collectors.toList());

        List<LikeDto> likes = post.getLikes().stream().map((like) -> {
            LikeDto currentLike = new LikeDto();
            currentLike.setImageUrl(like.getUser().getImageUrl());
            currentLike.setUsername(like.getUser().getUsername());
            currentLike.setFirstname(like.getUser().getFirstname());
            currentLike.setLastname(like.getUser().getLastname());

            return currentLike;
        }).collect(Collectors.toList());

        SimpleUserDto user = SimpleUserDto.builder().id(post.getUser().getId())
                .firstname(post.getUser().getFirstname()).lastname(post.getUser().getLastname())
                .username(post.getUser().getUsername()).imageUrl(post.getUser().getImageUrl())
                .bio(post.getUser().getBio()).build();

        PostDto buildPost = PostDto.builder().id(post.getId()).uid(post.getUid()).content(post.getContent())
                .numberOfComments(post.getNumberOfComments()).numberOfLikes(post.getNumberOfLikes())
                .numberOfBookmarks(post.getNumberOfBookmarks())
                .createdAt(post.getCreatedAt()).updatedAt(post.getUpdatedAt()).user(user).bookmarkedUsers(userIds)
                .postImages(post.getPostImages()).likes(likes).build();

        return buildPost;
    }

    public List<PostDto> buildPostDto(List<Post> allPost) {

        List<PostDto> allBuildPosts = allPost.stream().map((post) -> {
            List<Long> userIds = bookmarkRepository.findAllByContentIdAndContentType(post.getId(), "post").stream()
                    .map((bookmark) -> bookmark.getUser().getId()).filter(Objects::nonNull)
                    .collect(Collectors.toList());

            List<LikeDto> likes = post.getLikes().stream().map((like) -> {
                LikeDto currentLike = new LikeDto();
                currentLike.setImageUrl(like.getUser().getImageUrl());
                currentLike.setUsername(like.getUser().getUsername());
                currentLike.setFirstname(like.getUser().getFirstname());
                currentLike.setLastname(like.getUser().getLastname());

                return currentLike;
            }).collect(Collectors.toList());

            SimpleUserDto user = SimpleUserDto.builder().id(post.getUser().getId())
                    .firstname(post.getUser().getFirstname()).lastname(post.getUser().getLastname())
                    .username(post.getUser().getUsername()).imageUrl(post.getUser().getImageUrl())
                    .bio(post.getUser().getBio()).build();

            return PostDto.builder().id(post.getId()).uid(post.getUid()).content(post.getContent())
                    .numberOfComments(post.getNumberOfComments()).numberOfLikes(post.getNumberOfLikes())
                    .numberOfBookmarks(post.getNumberOfBookmarks())
                    .createdAt(post.getCreatedAt()).updatedAt(post.getUpdatedAt()).user(user).bookmarkedUsers(userIds)
                    .postImages(post.getPostImages()).likes(likes).build();
        }).collect(Collectors.toList());

        return allBuildPosts;
    }
}
