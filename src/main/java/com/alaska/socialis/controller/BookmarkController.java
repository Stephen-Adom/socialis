package com.alaska.socialis.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PostMapping("/bookmark/toggle")
    private void bookmarkPost(@RequestBody @Valid BookmarkRequest bookmarkRequest, BindingResult validationResult)
            throws ValidationErrorsException, EntityNotFoundException {
        this.bookmarkService.toggleBookmark(bookmarkRequest, validationResult);
    }

    @GetMapping("/bookmarks/{userId}/all")
    private ResponseEntity<Map<String, Object>> userBookmarks(@PathVariable("userId") Long userId)
            throws EntityNotFoundException {
        List<Object> allBookmarks = this.bookmarkService.fetchUserBookmarks(userId);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("status", HttpStatus.OK);
        response.put("data", allBookmarks);

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }
}
