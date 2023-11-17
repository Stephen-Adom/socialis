package com.alaska.socialis.services.serviceInterface;

import java.util.List;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.model.dto.ActivityDto;

public interface ActivityServiceInterface {
    public List<ActivityDto> fetchAllUserActivities(Long userId) throws EntityNotFoundException;
}
