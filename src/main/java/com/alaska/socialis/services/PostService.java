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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.PostImage;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.LikeDto;
import com.alaska.socialis.model.dto.PostDto;
import com.alaska.socialis.model.dto.SimpleUserDto;
import com.alaska.socialis.model.requestModel.UpdatePostRequest;
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
    private ImageUploadService imageUploadService;

    @Override
    public List<Post> fetchAllPost() {
        return this.postRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional
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
        }

        postObj.setUser(author.get());
        postObj.setContent(Objects.nonNull(content) ? content : "");
        postObj.setNumberOfComments(0);
        postObj.setNumberOfLikes(0);
        postObj.setPostImages(allMedia);

        return this.postRepository.save(postObj);

    }

    @Override
    public Post fetchPostById(Long postId) throws EntityNotFoundException {
        Optional<Post> post = this.postRepository.findById(postId);

        if (post.isEmpty()) {
            throw new EntityNotFoundException("Post with id " + postId + " does not exist", HttpStatus.NOT_FOUND);
        }

        return post.get();
    }

    @Override
    @Transactional
    public Post editPost(Long id, UpdatePostRequest post, BindingResult validationResult)
            throws ValidationErrorsException, EntityNotFoundException {
        if (validationResult.hasErrors()) {
            throw new ValidationErrorsException(validationResult.getFieldErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<User> author = this.userRepository.findById(post.getUser_id());

        if (author.isEmpty()) {
            throw new EntityNotFoundException("User with id " + post.getUser_id() + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        Optional<Post> currentPost = this.postRepository.findById(id);

        if (currentPost.isEmpty()) {
            throw new EntityNotFoundException("Post with id " + id + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        Post existingPost = currentPost.get();

        if (Objects.nonNull(post.getContent()) && !existingPost.getContent().equals(post.getContent())) {
            existingPost.setContent(post.getContent());
        }

        return this.postRepository.save(existingPost);

    }

    @Override
    @Transactional
    public void deletePost(Long userId, Long id) throws EntityNotFoundException {
        Optional<Post> existPost = this.postRepository.findByIdAndUserId(id, userId);

        if (existPost.isEmpty()) {
            throw new EntityNotFoundException("Post with user id " + userId + " and post id " + id + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        this.postRepository.deleteById(id);
    }

    public PostDto buildPostDto(Post post) {

        List<LikeDto> likes = post.getLikes().stream().map((like) -> {
            LikeDto currentLike = new LikeDto();
            currentLike.setImageUrl(like.getUser().getImageUrl());
            currentLike.setUsername(like.getUser().getUsername());

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
}
