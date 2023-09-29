package com.alaska.socialis.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.swing.text.html.parser.Entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.PostImage;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.requestModel.NewPostRequest;
import com.alaska.socialis.model.requestModel.UpdatePostRequest;
import com.alaska.socialis.repository.PostRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.PostServiceInterface;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PostService implements PostServiceInterface {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Override
    @Transactional
    public Post createPost(NewPostRequest post, BindingResult validationResult)
            throws ValidationErrorsException, EntityNotFoundException {
        List<PostImage> allMedia = new ArrayList<PostImage>();
        Post newPost = new Post();

        if (validationResult.hasErrors()) {
            throw new ValidationErrorsException(validationResult.getFieldErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<User> author = this.userRepository.findById(post.getUser_id());

        if (author.isEmpty()) {
            throw new EntityNotFoundException("User with id " + post.getUser_id() + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        newPost.setUser(author.get());
        newPost.setContent(post.getContent());
        newPost.setNumberOfComments(0);
        newPost.setNumberOfLikes(0);
        newPost.setPostImages(allMedia);

        if (post.getPostImages().size() > 0) {
            for (PostImage image : post.getPostImages()) {
                PostImage postMedia = PostImage.builder().post(newPost).mediaType(image.getMediaType())
                        .mediaUrl(image.getMediaUrl())
                        .build();

                allMedia.add(postMedia);
            }
        }
        newPost.setPostImages(allMedia);

        return this.postRepository.save(newPost);
    }

    @Override
    @Transactional
    public List<Post> fetchAllPost(Long userId) throws EntityNotFoundException {
        if (!this.userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        return this.postRepository.findByUserId(userId).get();
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
}
