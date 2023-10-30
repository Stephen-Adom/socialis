package com.alaska.socialis.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.Bookmark;
import com.alaska.socialis.model.Comment;
import com.alaska.socialis.model.Post;
import com.alaska.socialis.model.Reply;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.dto.CommentDto;
import com.alaska.socialis.model.dto.PostDto;
import com.alaska.socialis.model.dto.ReplyDto;
import com.alaska.socialis.model.requestModel.BookmarkRequest;
import com.alaska.socialis.repository.BookmarkRepository;
import com.alaska.socialis.repository.CommentRepository;
import com.alaska.socialis.repository.PostRepository;
import com.alaska.socialis.repository.ReplyRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.BookmarkServiceInterface;

@Service
public class BookmarkService implements BookmarkServiceInterface {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private ReplyService replyService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void toggleBookmark(BookmarkRequest bookmarkRequest, BindingResult validationResult)
            throws ValidationErrorsException, EntityNotFoundException {
        if (validationResult.hasErrors()) {
            throw new ValidationErrorsException(validationResult.getFieldErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<User> user = userRepository.findById(bookmarkRequest.getUserId());

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + bookmarkRequest.getUserId() + " not found",
                    HttpStatus.NOT_FOUND);
        }

        Optional<Bookmark> bookmarkExist = this.bookmarkRepository
                .findByUserIdAndContentIdAndContentType(bookmarkRequest.getUserId(), bookmarkRequest.getContentId(),
                        bookmarkRequest.getContentType());

        if (bookmarkExist.isEmpty()) {
            Bookmark bookmark = new Bookmark();
            bookmark.setContentId(bookmarkRequest.getContentId());
            bookmark.setContentType(bookmarkRequest.getContentType());
            bookmark.setUser(user.get());

            bookmarkRepository.save(bookmark);
            this.setNumberOfBookmarksOnEntity(bookmarkRequest, bookmarkExist);
        } else {
            bookmarkRepository.delete(bookmarkExist.get());
            this.setNumberOfBookmarksOnEntity(bookmarkRequest, bookmarkExist);
        }

    }

    @Override
    public List<Object> fetchUserBookmarks(Long userId) throws EntityNotFoundException {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " not found",
                    HttpStatus.NOT_FOUND);
        }

        List<Bookmark> allBookmarks = bookmarkRepository.findAllByUserId(userId);

        List<Object> formattedBookmark = new ArrayList<>();

        if (allBookmarks.size() > 0) {
            formattedBookmark.addAll(
                    allBookmarks.stream().map((bookmark) -> this.getContentByBookmark(bookmark))
                            .filter(Objects::nonNull).collect(Collectors.toList()));
        }

        return formattedBookmark;
    }

    private void setNumberOfBookmarksOnEntity(BookmarkRequest bookmarkRequest, Optional<Bookmark> bookmarkExist) {

        if (bookmarkRequest.getContentType().equals("post")) {
            Optional<Post> post = this.postRepository.findById(bookmarkRequest.getContentId());

            if (bookmarkExist.isEmpty()) {
                post.get().setNumberOfBookmarks(post.get().getNumberOfBookmarks() + 1);
            } else {
                post.get().setNumberOfBookmarks(post.get().getNumberOfBookmarks() - 1);
            }

            Post updatedPost = this.postRepository.save(post.get());

            PostDto postDto = this.postService.buildPostDto(updatedPost);

            messagingTemplate.convertAndSend("/feed/post/update", postDto);

        } else if (bookmarkRequest.getContentType().equals("comment")) {
            Optional<Comment> comment = this.commentRepository.findById(bookmarkRequest.getContentId());
            if (bookmarkExist.isEmpty()) {
                comment.get().setNumberOfBookmarks(comment.get().getNumberOfBookmarks() + 1);
            } else {
                comment.get().setNumberOfBookmarks(comment.get().getNumberOfBookmarks() - 1);
            }

            Comment updatedComment = this.commentRepository.save(comment.get());

            CommentDto commentDto = this.commentService.buildCommentDto(updatedComment);

            messagingTemplate.convertAndSend("/feed/comment/update", commentDto);

        } else if (bookmarkRequest.getContentType().equals("reply")) {
            Optional<Reply> reply = this.replyRepository.findById(bookmarkRequest.getContentId());

            if (bookmarkExist.isEmpty()) {
                reply.get().setNumberOfBookmarks(reply.get().getNumberOfBookmarks() + 1);
            } else {
                reply.get().setNumberOfBookmarks(reply.get().getNumberOfBookmarks() - 1);
            }

            Reply updatedReply = this.replyRepository.save(reply.get());

            ReplyDto replyDto = this.replyService.buildReplyDto(updatedReply);

            messagingTemplate.convertAndSend("/feed/reply/update", replyDto);
        }

    }

    public Object getContentByBookmark(Bookmark bookmark) {
        switch (bookmark.getContentType()) {
            case "post":
                return this.postService.buildPostDto(this.postRepository.findById(bookmark.getContentId()).get());

            case "comment":
                return this.commentService
                        .buildCommentDto(this.commentRepository.findById(bookmark.getContentId()).get());

            case "reply":
                return this.replyService
                        .buildReplyDto(this.replyRepository.findById(bookmark.getContentId()).get());

            default:
                return null;
        }
    }

}
