package com.alaska.socialis.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.Bookmark;
import com.alaska.socialis.model.User;
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

    @Override
    public void saveBookmark(BookmarkRequest bookmarkRequest, BindingResult validationResult)
            throws ValidationErrorsException, EntityNotFoundException {
        if (validationResult.hasErrors()) {
            throw new ValidationErrorsException(validationResult.getFieldErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<User> user = userRepository.findById(bookmarkRequest.getUserId());

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + bookmarkRequest.getUserId() + " not found",
                    HttpStatus.NOT_FOUND);
        }

        Bookmark bookmark = new Bookmark();
        bookmark.setContentId(bookmarkRequest.getContentId());
        bookmark.setContentType(bookmarkRequest.getContentType());
        bookmark.setUser(user.get());

        bookmarkRepository.save(bookmark);
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
