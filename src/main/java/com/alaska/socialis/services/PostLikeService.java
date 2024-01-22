package com.alaska.socialis.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.alaska.socialis.event.PostLikeEvent;
import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.CommentLike;
import com.alaska.socialis.model.Like;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.PostLike;
import com.alaska.socialis.model.ReplyLike;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.PostDto;
import com.alaska.socialis.repository.CommentLikeRepository;
import com.alaska.socialis.repository.PostLikesRepository;
import com.alaska.socialis.repository.PostRepository;
import com.alaska.socialis.repository.ReplyLikeRepository;
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

    @Autowired
    private PostLikesRepository postLikesRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private ReplyLikeRepository replyLikeRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ReplyService replyService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public PostDto togglePostLike(Long userId, Long postId, String likeType) {

        Optional<PostLike> postLike = this.postLikeRepository.findByUserIdAndPostId(userId, postId);
        Optional<User> user = this.userRepository.findById(userId);
        Optional<Post> post = this.postRepository.findById(postId);

        return this.checkLikeEntity(postLike, user, post, likeType);
    }

    private PostDto checkLikeEntity(Optional<PostLike> postLike, Optional<User> user, Optional<Post> post,
            String likeType) {
        if (postLike.isEmpty()) {
            post.get().setNumberOfLikes(post.get().getNumberOfLikes() + 1);
            PostLike newLike = PostLike.builder().user(user.get()).post(post.get()).likeType(likeType).build();
            this.postLikeRepository.save(newLike);

            // this.eventPublisher.publishEvent(new PostLikeEvent(newLike));

            return this.postService.buildPostDto(post.get());
        } else {

            if (postLike.get().getLikeType() == likeType) {
                post.get().setNumberOfLikes(post.get().getNumberOfLikes() - 1);
                Post updatedPost = this.postRepository.save(post.get());
                this.postLikeRepository.delete(postLike.get());

                return this.postService.buildPostDto(updatedPost);
            } else {
                postLike.get().setLikeType(likeType);
                PostLike updatedLike = this.postLikeRepository.save(postLike.get());
                return this.postService.buildPostDto(updatedLike.getPost());
            }

        }
    }

    @Override
    public List<Object> fetchAllLikesByUser(Long userId) throws EntityNotFoundException {
        Optional<User> user = this.userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        List<Like> allLikes = new ArrayList<Like>();
        allLikes.addAll(this.postLikesRepository.findAllByUserId(userId));
        allLikes.addAll(this.commentLikeRepository.findAllByUserId(userId));
        allLikes.addAll(this.replyLikeRepository.findAllByUserId(userId));

        allLikes.sort(Comparator.comparing(Like::getCreatedAt).reversed());

        List<Object> allPostLikes = allLikes.stream().map(like -> {
            if (like instanceof PostLike) {
                return this.postService.buildPostDto(((PostLike) like).getPost());
            } else if (like instanceof CommentLike) {
                return this.commentService.buildCommentDto(((CommentLike) like).getComment());
            } else if (like instanceof ReplyLike) {
                return this.replyService.buildReplyDto(((ReplyLike) like).getReply());
            } else {
                return null;
            }

        }).filter(Objects::nonNull).collect(Collectors.toList());

        return allPostLikes;

    }

}
