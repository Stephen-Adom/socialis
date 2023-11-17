package com.alaska.socialis.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.dto.ActivityDto;
import com.alaska.socialis.model.dto.SuccessResponse;
import com.alaska.socialis.services.ActivityService;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @GetMapping("/{userId}/all")
    public ResponseEntity<SuccessResponse> fetchAllUserActivities(@PathVariable("userId") Long userId)
            throws EntityNotFoundException {
        List<ActivityDto> allActivities = this.activityService.fetchAllUserActivities(userId);

        SuccessResponse response = SuccessResponse.builder().data(allActivities).status(HttpStatus.OK).build();

        return new ResponseEntity<SuccessResponse>(response, HttpStatus.OK);
    }
}
