package com.alaska.socialis.services.serviceInterface;

import java.util.List;

import org.springframework.validation.BindingResult;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.requestModel.BookmarkRequest;

public interface BookmarkServiceInterface {
    public void toggleBookmark(BookmarkRequest bookmarkRequest, BindingResult validationResult)
            throws ValidationErrorsException, EntityNotFoundException;

    public List<Object> fetchUserBookmarks(Long userId) throws EntityNotFoundException;
}
