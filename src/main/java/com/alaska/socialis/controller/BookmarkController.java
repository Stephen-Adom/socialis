package com.alaska.socialis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.requestModel.BookmarkRequest;
import com.alaska.socialis.services.BookmarkService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    @PostMapping("/bookmark/save")
    private void bookmarkPost(@RequestBody @Valid BookmarkRequest bookmarkRequest, BindingResult validationResult)
            throws ValidationErrorsException, EntityNotFoundException {
        this.bookmarkService.saveBookmark(bookmarkRequest, validationResult);
    }
}
