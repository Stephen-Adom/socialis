package com.alaska.socialis.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.PostLike;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.LikeDto;
import com.alaska.socialis.model.dto.PostDto;
import com.alaska.socialis.model.dto.SimpleUserDto;
import com.alaska.socialis.repository.PostLikesRepository;
import com.alaska.socialis.repository.PostRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.PostLikeServiceInterface;

@Service
public class PostLikeService implements PostLikeServiceInterface {

    @Autowired
    private PostLikesRepository postLikeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    @Override
    public PostDto togglePostLike(Long userId, Long postId) {

        Optional<PostLike> postLike = this.postLikeRepository.findByUserIdAndPostId(userId, postId);
        Optional<User> user = this.userRepository.findById(userId);
        Optional<Post> post = this.postRepository.findById(postId);

        return this.checkLikeEntity(postLike, user, post);
    }

    private PostDto checkLikeEntity(Optional<PostLike> postLike, Optional<User> user, Optional<Post> post) {
        if (postLike.isEmpty()) {
            PostLike newLike = PostLike.builder().user(user.get()).post(post.get()).build();
            this.postLikeRepository.save(newLike);
            post.get().setNumberOfLikes(post.get().getNumberOfLikes() + 1);
            Post updatedPost = this.postRepository.save(post.get());

            return this.postService.buildPostDto(updatedPost);
        } else {
            this.postLikeRepository.delete(postLike.get());
            post.get().setNumberOfLikes(post.get().getNumberOfLikes() - 1);
            Post updatedPost = this.postRepository.save(post.get());

            return this.postService.buildPostDto(updatedPost);
        }
    }

}
