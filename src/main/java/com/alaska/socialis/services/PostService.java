package com.alaska.socialis.services;

import java.util.Map;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.OffsetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.event.NewPostEvent;
import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.UserAlreadyExistException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.PostImage;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.LikeDto;
import com.alaska.socialis.model.dto.PostDto;
import com.alaska.socialis.model.dto.ResharedUserDto;
import com.alaska.socialis.model.dto.SimpleUserDto;
import com.alaska.socialis.model.dto.SinglePostDto;
import com.alaska.socialis.model.requestModel.RepostBody;
import com.alaska.socialis.repository.BookmarkRepository;
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

    private static final String POST_IMAGE_CLOUD_PATH = "socialis/post/images";

    private static final String POST_VIDEO_CLOUD_PATH = "socialis/post/videos";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

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

        List<PostDto> allPostDto = allPost.stream().map(post -> this.buildPostDto(post)).collect(Collectors.toList());

        return allPostDto;
    }

    public List<PostDto> fetchAllPostUsingOffsetFilteringAndWindowIterator(int offsetCount) {
        OffsetScrollPosition offset = ScrollPosition.offset(offsetCount);

        System.out.println(
                "====================================== scroll position offset ================================");
        System.out.println(offset);
        List<Post> postList = this.postRepository.findFirst5ByScheduledAtIsNullOrderByCreatedAtDesc(offset);

        List<PostDto> allPostDto = postList.stream().map(post -> this.buildPostDto(post)).collect(Collectors.toList());

        return allPostDto;
    }

    @Override
    public List<PostDto> fetchAllPostsByUser(Long userId) throws EntityNotFoundException {
        Optional<User> user = this.userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        List<Post> allPosts = this.postRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        List<PostDto> allPostDto = allPosts.stream().map(post -> this.buildPostDto(post)).collect(Collectors.toList());

        return allPostDto;
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
                    String filePath = file.getContentType().contains("image") ? POST_IMAGE_CLOUD_PATH
                            : POST_VIDEO_CLOUD_PATH;

                    String resourceType = file.getContentType().contains("image") ? "image" : "video";

                    Map<String, Object> result = this.imageUploadService.uploadImageToCloud(filePath,
                            file, resourceType);

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

        messagingTemplate.convertAndSend(NEW_LIVE_POST_FEED_URL,
                this.buildPostDto(updatedPost));
        messagingTemplate.convertAndSend(UPDATE_LIVE_USER_PATH + "-" +
                updatedUser.get().getUsername(),
                this.userService.buildDto(updatedUser.get()));

        this.eventPublisher.publishEvent(new NewPostEvent(updatedPost));
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
                    String filePath = file.getContentType().contains("image") ? POST_IMAGE_CLOUD_PATH
                            : POST_VIDEO_CLOUD_PATH;

                    String resourceType = file.getContentType().contains("image") ? "image" : "video";

                    Map<String, Object> result = this.imageUploadService.uploadImageToCloud(filePath,
                            file, resourceType);

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

        messagingTemplate.convertAndSend(UPDATE_LIVE_USER_PATH + "-" +
                updatedUser.getUsername(),
                this.userService.buildDto(updatedUser));
    }

    private void deleteAllPostImages(Post post) {
        List<Map<String, String>> images = new ArrayList<Map<String, String>>();

        images.addAll(
                post.getPostImages().stream().map(postImage -> {
                    Map<String, String> postObject = new HashMap<>();
                    postObject.put("image", postImage.getMediaUrl());
                    postObject.put("mediaType", postImage.getMediaType());

                    return postObject;
                }).collect(Collectors.toList()));

        if (post.getComments().size() > 0) {
            post.getComments().forEach((comment) -> {
                images.addAll(
                        comment.getCommentImages().stream().map(commentImage -> {
                            Map<String, String> commentObject = new HashMap<>();
                            commentObject.put("image", commentImage.getMediaUrl());
                            commentObject.put("mediaType", commentImage.getMediaType());

                            return commentObject;
                        }).collect(Collectors.toList()));

                comment.getReplies().stream().forEach((reply) -> {
                    images.addAll(
                            reply.getReplyImages().stream().map(replyImage -> {
                                Map<String, String> replyObject = new HashMap<>();
                                replyObject.put("image", replyImage.getMediaUrl());
                                replyObject.put("mediaType", replyImage.getMediaType());

                                return replyObject;
                            }).collect(Collectors.toList()));
                });
            });
        }

        if (images.size() > 0) {
            images.stream().forEach((imageUrl) -> {
                if (imageUrl.get("mediaType").equalsIgnoreCase("image")) {
                    this.imageUploadService.deleteUploadedImage(POST_IMAGE_CLOUD_PATH, imageUrl.get("image"));
                } else {
                    this.imageUploadService.deleteUploadedImage(POST_VIDEO_CLOUD_PATH, imageUrl.get("image"));
                }

            });
        }

    }

    @Override
    public void repostWithNoContent(Long userId, Long postId)
            throws EntityNotFoundException, UserAlreadyExistException {
        Optional<User> user = this.userRepository.findById(userId);
        Optional<Post> post = this.postRepository.findById(postId);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        if (post.isEmpty()) {
            throw new EntityNotFoundException("Post with id " + postId + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        String uid = "post-" + UUID.randomUUID().toString();

        // check if original_post_id is null => reposting an original post
        Optional<Post> repostExist = this.postRepository.findByUserIdAndOriginalPostIdWithNoContent(userId, postId);

        if (repostExist.isEmpty()) {
            Post postObj = new Post();
            user.get().setNoOfPosts(user.get().getNoOfPosts() + 1);
            postObj.setUid(uid);
            postObj.setUser(user.get());
            post.get().setNumberOfRepost(post.get().getNumberOfRepost() + 1);
            postObj.setOriginalPost(post.get());

            Post newRepost = this.postRepository.save(postObj);

            messagingTemplate.convertAndSend(NEW_LIVE_POST_FEED_URL,
                    this.buildPostDto(newRepost));
            messagingTemplate.convertAndSend(UPDATE_LIVE_POST_FEED_URL,
                    this.buildPostDto(post.get()));
            messagingTemplate.convertAndSend(UPDATE_LIVE_USER_PATH + "-" +
                    user.get().getUsername(),
                    this.userService.buildDto(user.get()));

            // ! send notification about repost
        } else {
            throw new UserAlreadyExistException("Post already reposted", HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    public void undoRepost(Long postId) throws EntityNotFoundException {
        Optional<Post> post = this.postRepository.findById(postId);

        if (post.isEmpty()) {
            throw new EntityNotFoundException("Post with id " + postId + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        User user = post.get().getUser();
        user.setNoOfPosts(user.getNoOfPosts() - 1);
        post.get().getOriginalPost().setNumberOfRepost(post.get().getOriginalPost().getNumberOfRepost() - 1);

        User updatedUser = this.userRepository.save(user);

        this.postRepository.deleteById(postId);

        messagingTemplate.convertAndSend(UPDATE_LIVE_POST_FEED_URL,
                this.buildPostDto(post.get().getOriginalPost()));

        messagingTemplate.convertAndSend(UPDATE_LIVE_USER_PATH + "-" +
                updatedUser.getUsername(),
                this.userService.buildDto(updatedUser));
    }

    @Override
    public void repostWithContent(Long userId, RepostBody requestBody, BindingResult validationResult)
            throws EntityNotFoundException, ValidationErrorsException, UserAlreadyExistException {
        Optional<User> user = this.userRepository.findById(userId);
        Optional<Post> post = this.postRepository.findById(requestBody.getPostId());

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        if (post.isEmpty()) {
            throw new EntityNotFoundException("Post with id " + requestBody.getPostId() + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        if (validationResult.hasErrors()) {
            throw new ValidationErrorsException(validationResult.getFieldErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        String uid = "post-" + UUID.randomUUID().toString();

        // check if original_post_id is null => reposting an original post
        Optional<Post> repostExist = this.postRepository.findByUserIdAndOriginalPostIdWithContent(userId,
                requestBody.getPostId());

        if (repostExist.isEmpty()) {
            Post postObj = new Post();
            user.get().setNoOfPosts(user.get().getNoOfPosts() + 1);
            postObj.setUid(uid);
            postObj.setUser(user.get());
            post.get().setNumberOfRepost(post.get().getNumberOfRepost() + 1);
            postObj.setOriginalPost(post.get());
            postObj.setContent(requestBody.getContent());

            Post newRepost = this.postRepository.save(postObj);

            messagingTemplate.convertAndSend(NEW_LIVE_POST_FEED_URL,
                    this.buildPostDto(newRepost));
            messagingTemplate.convertAndSend(UPDATE_LIVE_POST_FEED_URL,
                    this.buildPostDto(post.get()));
            messagingTemplate.convertAndSend(UPDATE_LIVE_USER_PATH + "-" +
                    user.get().getUsername(),
                    this.userService.buildDto(user.get()));

            // ! send notification about repost
        } else {
            throw new UserAlreadyExistException("Post already reposted", HttpStatus.BAD_REQUEST);
        }
    }

    public PostDto buildPostDto(Post post) {
        List<Long> bookmarks = getBookmarkUsers(post);
        List<ResharedUserDto> resharedBy = postResharedBy(post);

        PostDto buildPost = PostDto.builder().id(post.getId()).uid(post.getUid()).content(post.getContent())
                .numberOfComments(post.getNumberOfComments()).numberOfLikes(post.getNumberOfLikes())
                .numberOfBookmarks(post.getNumberOfBookmarks())
                .createdAt(post.getCreatedAt()).updatedAt(post.getUpdatedAt()).user(buildSimpleUserDto(post))
                .bookmarkedUsers(bookmarks)
                .postImages(post.getPostImages()).likes(buildLikeDto(post)).numberOfRepost(post.getNumberOfRepost())
                .originalPost(buildSimplePostDto(post.getOriginalPost())).resharedBy(resharedBy).build();

        return buildPost;

    }

    public List<ResharedUserDto> postResharedBy(Post post) {
        List<ResharedUserDto> resharedBy = this.postRepository.findAllByOriginalPostId(post.getId()).stream()
                .map(resharedpost -> {
                    ResharedUserDto user = new ResharedUserDto();
                    user.setUserId(resharedpost.getUser().getId());
                    user.setResharedId(resharedpost.getId());
                    user.setWithContent(Objects.isNull(resharedpost.getContent()) ? false : true);

                    return user;
                }).collect(Collectors.toList());

        return resharedBy;
    }

    public SinglePostDto buildSimplePostDto(Post post) {
        if (Objects.nonNull(post)) {
            List<Long> bookmarks = getBookmarkUsers(post);

            SinglePostDto buildPost = SinglePostDto.builder().id(post.getId()).uid(post.getUid())
                    .content(post.getContent())
                    .numberOfComments(post.getNumberOfComments()).numberOfLikes(post.getNumberOfLikes())
                    .numberOfBookmarks(post.getNumberOfBookmarks())
                    .createdAt(post.getCreatedAt()).updatedAt(post.getUpdatedAt()).user(buildSimpleUserDto(post))
                    .bookmarkedUsers(bookmarks)
                    .postImages(post.getPostImages()).likes(buildLikeDto(post)).numberOfRepost(post.getNumberOfRepost())
                    .build();

            return buildPost;
        }

        return null;

    }

    private SimpleUserDto buildSimpleUserDto(Post post) {
        return SimpleUserDto.builder().id(post.getUser().getId())
                .firstname(post.getUser().getFirstname()).lastname(post.getUser().getLastname())
                .username(post.getUser().getUsername()).imageUrl(post.getUser().getImageUrl())
                .bio(post.getUser().getBio()).build();
    }

    private List<LikeDto> buildLikeDto(Post post) {
        List<LikeDto> likes = post.getLikes().stream().map((like) -> {
            LikeDto currentLike = new LikeDto();
            currentLike.setImageUrl(like.getUser().getImageUrl());
            currentLike.setUsername(like.getUser().getUsername());
            currentLike.setFirstname(like.getUser().getFirstname());
            currentLike.setLastname(like.getUser().getLastname());
            currentLike.setLikeType(like.getLikeType());

            return currentLike;
        }).collect(Collectors.toList());

        return likes;
    }

    private List<Long> getBookmarkUsers(Post post) {
        List<Long> userIds = bookmarkRepository.findAllByContentIdAndContentType(post.getId(), "post").stream()
                .map((bookmark) -> bookmark.getUser().getId()).filter(Objects::nonNull).collect(Collectors.toList());

        return userIds;
    }
}
