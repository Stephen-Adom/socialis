package com.alaska.socialis.services;

import java.util.Optional;

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
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.BookmarkServiceInterface;

@Service
public class BookmarkService implements BookmarkServiceInterface {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private UserRepository userRepository;

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

}
