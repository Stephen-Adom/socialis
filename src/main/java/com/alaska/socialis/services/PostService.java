package com.alaska.socialis.services;

import java.util.Map;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.CommentImages;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.PostImage;
import com.alaska.socialis.model.ReplyImage;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.LikeDto;
import com.alaska.socialis.model.dto.PostDto;
import com.alaska.socialis.model.dto.SimpleUserDto;
import com.alaska.socialis.repository.PostImageRepository;
import com.alaska.socialis.repository.PostRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.PostServiceInterface;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PostService implements PostServiceInterface {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    @Override
    public List<PostDto> fetchAllPost() {
        List<Post> allPost = this.postRepository.findAllByOrderByCreatedAtDesc();

        return this.buildPostDto(allPost);
    }

    @Override
    public Post createPost(Long userId, String content, MultipartFile[] multipartFiles) throws EntityNotFoundException {
        List<PostImage> allMedia = new ArrayList<PostImage>();
        Optional<User> author = this.userRepository.findById(userId);
        Post postObj = new Post();

        if (author.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        if (Objects.nonNull(multipartFiles) && Arrays.asList(multipartFiles).size() > 0) {
            Arrays.asList(multipartFiles).forEach((file) -> {
                Map<String, Object> result;
                try {
                    result = this.imageUploadService.uploadImageToCloud("socialis/post/images", file);

                    PostImage uploadedImage = PostImage.builder().post(postObj)
                            .mediaType((String) result.get("resource_type"))
                            .mediaUrl((String) result.get("secure_url"))
                            .build();

                    allMedia.add(uploadedImage);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            });

            postObj.setPostImages(allMedia);
        }

        postObj.setUser(author.get());
        postObj.setContent(Objects.nonNull(content) ? content : "");
        postObj.setNumberOfComments(0);
        postObj.setNumberOfLikes(0);

        return this.postRepository.save(postObj);

    }

    @Override
    public PostDto fetchPostById(Long postId) throws EntityNotFoundException {
        Optional<Post> post = this.postRepository.findById(postId);

        if (post.isEmpty()) {
            throw new EntityNotFoundException("Post with id " + postId + " does not exist", HttpStatus.NOT_FOUND);
        }

        return this.buildPostDto(post.get());
    }

    @Override
    public Post editPost(Long id, String content, MultipartFile[] multipartFiles) throws EntityNotFoundException {
        List<PostImage> allMedia = new ArrayList<PostImage>();
        Optional<Post> postExist = this.postRepository.findById(id);

        if (postExist.isEmpty()) {
            throw new EntityNotFoundException("Post with id " + postExist.get().getId() + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        Post existingPost = postExist.get();

        if (Objects.nonNull(multipartFiles) && Arrays.asList(multipartFiles).size() > 0) {
            Arrays.asList(multipartFiles).forEach((file) -> {
                Map<String, Object> result;
                try {
                    result = this.imageUploadService.uploadImageToCloud("socialis/post/images", file);

                    PostImage uploadedImage = PostImage.builder().post(existingPost)
                            .mediaType((String) result.get("resource_type"))
                            .mediaUrl((String) result.get("secure_url"))
                            .build();

                    allMedia.add(uploadedImage);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            });

            existingPost.setPostImages(allMedia);
        }

        if (content != null) {
            existingPost.setContent(content);
        }

        this.postRepository.save(existingPost);

        Optional<Post> updatedPost = this.postRepository.findById(id);

        List<PostImage> updatedImages = this.postImageRepository.findAllByPostId(id);
        updatedPost.get().setPostImages(updatedImages);

        return updatedPost.get();
    }

    @Override
    public void deletePost(Long id) throws EntityNotFoundException {
        Optional<Post> existPost = this.postRepository.findById(id);

        if (existPost.isEmpty()) {
            throw new EntityNotFoundException("Post with id " + id + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        this.deleteAllPostImages(existPost.get());

        this.postRepository.deleteById(id);
    }

    private void deleteAllPostImages(Post post) {
        List<String> images = new ArrayList<>();

        images.addAll(
                post.getPostImages().stream().map(PostImage::getMediaType).collect(Collectors.toList()));

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
                this.imageUploadService.deleteUploadedImage("socialis/post/images/", imageUrl);
            });
        }

    }

    public PostDto buildPostDto(Post post) {

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
                .username(post.getUser().getUsername()).imageUrl(post.getUser().getImageUrl()).build();

        PostDto buildPost = PostDto.builder().id(post.getId()).content(post.getContent())
                .numberOfComments(post.getNumberOfComments()).numberOfLikes(post.getNumberOfLikes())
                .createdAt(post.getCreatedAt()).updatedAt(post.getUpdatedAt()).user(user)
                .postImages(post.getPostImages()).likes(likes).build();

        return buildPost;
    }

    public List<PostDto> buildPostDto(List<Post> allPost) {

        List<PostDto> allBuildPosts = allPost.stream().map((post) -> {
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
                    .username(post.getUser().getUsername()).imageUrl(post.getUser().getImageUrl()).build();

            return PostDto.builder().id(post.getId()).content(post.getContent())
                    .numberOfComments(post.getNumberOfComments()).numberOfLikes(post.getNumberOfLikes())
                    .createdAt(post.getCreatedAt()).updatedAt(post.getUpdatedAt()).user(user)
                    .postImages(post.getPostImages()).likes(likes).build();
        }).collect(Collectors.toList());

        return allBuildPosts;
    }
}
