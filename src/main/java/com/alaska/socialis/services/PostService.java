package com.alaska.socialis.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import com.alaska.socialis.exceptions.UserNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.PostImage;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.requestModel.NewPostRequest;
import com.alaska.socialis.repository.PostImageRepository;
import com.alaska.socialis.repository.PostRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.PostServiceInterface;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PostService implements PostServiceInterface {
    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Override
    @Transactional
    public Post createPost(NewPostRequest post, BindingResult validationResult)
            throws ValidationErrorsException, UserNotFoundException {
        List<PostImage> allMedia = new ArrayList<PostImage>();
        Post newPost = new Post();

        if (validationResult.hasErrors()) {
            throw new ValidationErrorsException(validationResult.getFieldErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<User> author = this.userRepository.findById(post.getUser_id());

        if (author.isEmpty()) {
            throw new UserNotFoundException("User with id " + post.getUser_id() + " does not exist",
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
    public List<Post> fetchAllPost(Long userId) throws UserNotFoundException {
        if (!this.userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with id " + userId + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        return this.postRepository.findByUserId(userId).get();
    }
}
