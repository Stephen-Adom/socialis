package com.alaska.socialis.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alaska.socialis.model.Comment;
import com.alaska.socialis.model.CommentLike;
import com.alaska.socialis.model.Reply;
import com.alaska.socialis.model.ReplyLike;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.CommentDto;
import com.alaska.socialis.model.dto.ReplyDto;
import com.alaska.socialis.repository.ReplyLikeRepository;
import com.alaska.socialis.repository.ReplyRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.ReplyLikeServiceInterface;

@Service
public class ReplyLikeService implements ReplyLikeServiceInterface {
    @Autowired
    private ReplyLikeRepository replyLikeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private ReplyService replyService;

    @Override
    public ReplyDto toggleReplyLike(Long userId, Long replyId) {
        Optional<ReplyLike> replyLike = this.replyLikeRepository.findByUserIdAndReplyId(userId, replyId);
        Optional<User> user = this.userRepository.findById(userId);
        Optional<Reply> reply = this.replyRepository.findById(replyId);

        return this.checkReplyLikeEntity(replyLike, user, reply);
    }

    private ReplyDto checkReplyLikeEntity(Optional<ReplyLike> replyLike, Optional<User> user, Optional<Reply> reply) {

        if (replyLike.isEmpty()) {
            System.out.println(reply.get().getNumberOfLikes());

            ReplyLike newLike = ReplyLike.builder().user(user.get()).reply(reply.get()).build();
            this.replyLikeRepository.save(newLike);
            reply.get().setNumberOfLikes(reply.get().getNumberOfLikes() + 1);
            Reply updatedReply = this.replyRepository.save(reply.get());

            return this.replyService.buildReplyDto(updatedReply);
        } else {

            reply.get().setNumberOfLikes(reply.get().getNumberOfLikes() - 1);
            Reply updatedreply = this.replyRepository.save(reply.get());
            this.replyLikeRepository.delete(replyLike.get());

            return this.replyService.buildReplyDto(updatedreply);
        }
    }

}
