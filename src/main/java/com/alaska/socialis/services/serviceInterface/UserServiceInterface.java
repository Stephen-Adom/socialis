package com.alaska.socialis.services.serviceInterface;

import java.io.IOException;
import java.util.Set;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.dto.UserSummaryFollowingDto;
import com.alaska.socialis.model.requestModel.UserInfoRequest;

public interface UserServiceInterface {
        public UserDetails fetchUserDetailsByUsername(String username) throws EntityNotFoundException;

        public void updateUserCoverImage(Long userId, MultipartFile multipartFile)
                        throws EntityNotFoundException, IOException;

        public void updateUserProfileImage(Long userId, MultipartFile multipartFile)
                        throws EntityNotFoundException, IOException;

        public void updateUserInfo(Long userId, UserInfoRequest requestBody, BindingResult validationResult)
                        throws EntityNotFoundException, ValidationErrorsException;

        public UserSummaryFollowingDto fetchUserInfoFullInformation(String username) throws EntityNotFoundException;

        public UserSummaryFollowingDto followUser(Long followerId, Long followingId);

        public UserSummaryFollowingDto unfollowUser(Long followerId, Long followingId) throws EntityNotFoundException;

        public Set<UserSummaryFollowingDto> fetchAllUserFollowers(String username) throws EntityNotFoundException;

        public Set<UserSummaryFollowingDto> fetchAllUserFollowing(String username) throws EntityNotFoundException;
}
