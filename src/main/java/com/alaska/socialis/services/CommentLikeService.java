package com.alaska.socialis.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.alaska.socialis.event.CommentLikeEvent;
import com.alaska.socialis.model.Comment;
import com.alaska.socialis.model.CommentLike;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.CommentDto;
import com.alaska.socialis.repository.CommentLikeRepository;
import com.alaska.socialis.repository.CommentRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.CommentLikeServiceInterface;

@Service
public class CommentLikeService implements CommentLikeServiceInterface {
    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public CommentDto toggleCommentLike(Long userId, Long commentId) {

        Optional<CommentLike> commentLike = this.commentLikeRepository.findByUserIdAndCommentId(userId, commentId);
        Optional<User> user = this.userRepository.findById(userId);
        Optional<Comment> comment = this.commentRepository.findById(commentId);

        return this.checkLikeEntity(commentLike, user, comment);
    }

    private CommentDto checkLikeEntity(Optional<CommentLike> commentLike, Optional<User> user,
            Optional<Comment> comment) {
        if (commentLike.isEmpty()) {
            comment.get().setNumberOfLikes(comment.get().getNumberOfLikes() + 1);
            CommentLike newLike = CommentLike.builder().user(user.get()).comment(comment.get()).build();
            this.commentLikeRepository.save(newLike);

            this.eventPublisher.publishEvent(new CommentLikeEvent(newLike));

            return this.commentService.buildCommentDto(comment.get());
        } else {
            comment.get().setNumberOfLikes(comment.get().getNumberOfLikes() - 1);
            Comment updatedcomment = this.commentRepository.save(comment.get());
            this.commentLikeRepository.delete(commentLike.get());

            return this.commentService.buildCommentDto(updatedcomment);
        }
    }
}
